package com.nvankempen.csc375;

import com.nvankempen.csc375.sockets.MainSocket;
import com.nvankempen.underscore.utils.tuples.Doublet;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.atomic.AtomicReference;

import static com.nvankempen.csc375.Constants.*;

public final class SudokuSolver extends CountedCompleter<Sudoku> {

    private final SudokuSolver parent;
    private final Sudoku sudoku;
    private final MainSocket socket;
    private final AtomicReference<Sudoku> solution;

    private int getLocalDepth() {
        if (parent == null) return 0;
        else return 1 + parent.getLocalDepth();
    }

    public Sudoku getSudoku() {
        return sudoku;
    }

    public SudokuSolver(Sudoku sudoku, MainSocket socket) {
        this(null, sudoku, new AtomicReference<>(), socket);
    }

    private SudokuSolver(SudokuSolver parent, Sudoku sudoku, AtomicReference<Sudoku> solution, MainSocket socket) {
        super(parent);
        this.parent = parent;
        this.sudoku = sudoku;
        this.solution = solution;
        this.socket = socket;
    }

    @Override
    public Sudoku getRawResult() {
        return solution.get();
    }

    @Override
    public void compute() {
        if (solution.get() != null) return;

        if (sudoku.remaining() < THRESHOLD) {
            Sudoku result = sequentialSolve(sudoku);
            if(result != null && solution.compareAndSet(null, result))
                quietlyCompleteRoot();
        } else {
            final Doublet<Integer, Integer> location = nextEmptyLocation(sudoku);
            final List<Long> possibilities = sudoku.whatCanWeSetAt(location.getA(), location.getB());

            setPendingCount(possibilities.size());
            for (long possibility : possibilities) {
                final Sudoku copy = sudoku.set(location.getA(), location.getB(), possibility);
                final SudokuSolver task = new SudokuSolver(this, copy, solution, socket);

                if (!(socket != null && sudoku.remaining() > CLUSTER_THRESHOLD && getLocalDepth() > 5 && socket.tryAskingForHelp(task)))
                    task.fork();
            }
        }

        tryComplete();
    }

    public static Sudoku sequentialSolve(Sudoku sudoku) {
        if (sudoku.remaining() == 0) {
            return sudoku.isValid() ? sudoku : null;
        }

        final Doublet<Integer, Integer> location = nextEmptyLocation(sudoku);
        final List<Long> possibilities = sudoku.whatCanWeSetAt(location.getA(), location.getB());

        for (long possibility : possibilities) {
            final Sudoku solution = sequentialSolve(sudoku.set(location.getA(), location.getB(), possibility));
            if (solution != null) return solution;
        }

        return null;
    }

    private static Doublet<Integer, Integer> nextEmptyLocation(Sudoku sudoku) {
        for (int i = 0; i < N * N; ++i) {
            for (int j = 0; j < N * N; ++j) {
                if (!sudoku.isSet(i, j)) return new Doublet<>(i, j);
            }
        }

        throw new NoSuchElementException();
    }

    public void markAsCompleted(Sudoku result) {
        if(result != null && solution.compareAndSet(null, result))
            quietlyCompleteRoot();
        else tryComplete();
    }
}
