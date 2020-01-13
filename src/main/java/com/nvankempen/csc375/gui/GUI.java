package com.nvankempen.csc375.gui;

import com.nvankempen.csc375.Sudoku;
import com.nvankempen.csc375.SudokuSolver;
import com.nvankempen.csc375.sockets.MainSocket;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static com.nvankempen.csc375.Constants.N;

public final class GUI extends JFrame {
    private static final int MARGIN = 20;

    private final SudokuGrid grid;
    private final JButton resetButton = new JButton("Reset");
    private final JButton solveButton = new JButton("Sequential Solve");
    private final JButton parallelSolveButton = new JButton("Parallel Solve");
    private final JButton clusterSolveButton = new JButton("Cluster Solve");

    public GUI(String title, String[] args) throws IOException {
        super(title);

        final MainSocket socket = new MainSocket();

        if (args.length > 0) {
            final String filename = args[0];
            final long[][] data = new long[N * N][N * N];

            try (final Scanner sc = new Scanner(new File(filename))) {
                for (int i = 0; i < N * N; ++i) {
                    for (int j = 0; j < N * N; j++) {
                        data[j][i] = sc.nextLong();
                    }
                }
            } catch(FileNotFoundException | NoSuchElementException ignored) {

            }

            grid = new SudokuGrid(new Sudoku(data));
        } else {
            grid = new SudokuGrid();
        }

        super.getContentPane().setBackground(Color.WHITE);
        super.setLayout(new GridBagLayout());

        super.getContentPane().add(grid, position(0, 0, 4));
        super.getContentPane().add(resetButton, position(0, 1));
        super.getContentPane().add(solveButton, position(1, 1));
        super.getContentPane().add(parallelSolveButton, position(2, 1));
        super.getContentPane().add(clusterSolveButton, position(3, 1));

        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.pack();
        super.setLocationRelativeTo(null);
        super.setVisible(true);

        resetButton.addActionListener(e -> grid.clear());

        solveButton.addActionListener(e -> {
            resetButton.setEnabled(false);
            solveButton.setEnabled(false);
            parallelSolveButton.setEnabled(false);
            clusterSolveButton.setEnabled(false);

            new Thread(() -> {
                final long start = System.nanoTime();
                final Sudoku solution = SudokuSolver.sequentialSolve(grid.getSudoku());
                if (solution == null) JOptionPane.showMessageDialog(
                        this,
                        "There are no solutions to this puzzle.",
                        "Sudoku",
                        JOptionPane.ERROR_MESSAGE
                ); else {
                    final long end = System.nanoTime();
                    final long elapsed = end - start;
                    System.out.printf("Computation completed in %.3f seconds. %n", ((double) elapsed) / Math.pow(10, 9));
                    grid.updateSudoku(solution);
                }

                resetButton.setEnabled(true);
                solveButton.setEnabled(true);
                parallelSolveButton.setEnabled(true);
                clusterSolveButton.setEnabled(true);
            }).start();
        });

        parallelSolveButton.addActionListener(e -> {
            resetButton.setEnabled(false);
            solveButton.setEnabled(false);
            parallelSolveButton.setEnabled(false);
            clusterSolveButton.setEnabled(false);

            new Thread(() -> {
                final long start = System.nanoTime();
                final SudokuSolver solver = new SudokuSolver(grid.getSudoku(), null);
                final Sudoku solution = solver.invoke();

                if (solution == null) JOptionPane.showMessageDialog(
                        this,
                        "There are no solutions to this puzzle.",
                        "Sudoku",
                        JOptionPane.ERROR_MESSAGE
                ); else {
                    final long end = System.nanoTime();
                    final long elapsed = end - start;
                    System.out.printf("Computation completed in %.3f seconds. %n", ((double) elapsed) / Math.pow(10, 9));
                    grid.updateSudoku(solution);
                }

                resetButton.setEnabled(true);
                solveButton.setEnabled(true);
                parallelSolveButton.setEnabled(true);
                clusterSolveButton.setEnabled(true);
            }).start();
        });

        clusterSolveButton.addActionListener(e -> {
            resetButton.setEnabled(false);
            solveButton.setEnabled(false);
            parallelSolveButton.setEnabled(false);
            clusterSolveButton.setEnabled(false);

            new Thread(() -> {
                final long start = System.nanoTime();
                final SudokuSolver solver = new SudokuSolver(grid.getSudoku(), socket);
                final Sudoku solution = solver.invoke();

                if (solution == null) JOptionPane.showMessageDialog(
                        this,
                        "There are no solutions to this puzzle.",
                        "Sudoku",
                        JOptionPane.ERROR_MESSAGE
                ); else {
                    final long end = System.nanoTime();
                    final long elapsed = end - start;
                    System.out.printf("Computation completed in %.3f seconds. %n", ((double) elapsed) / Math.pow(10, 9));
                    grid.updateSudoku(solution);
                }

                resetButton.setEnabled(true);
                solveButton.setEnabled(true);
                parallelSolveButton.setEnabled(true);
                clusterSolveButton.setEnabled(true);
            }).start();
        });

        grid.addMouseListener(new MouseClickListener(event -> {
            Point point = event.getPoint();
            final int i = point.x / SudokuGrid.SCALE;
            final int j = point.y / SudokuGrid.SCALE;

            final String input = JOptionPane.showInputDialog(
                    this,
                    "Please enter a number between 0 and " + (N * N) + ".",
                    "Sudoku",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (input != null) {
                try {
                    final byte x = Byte.parseByte(input);
                    grid.updateSudoku(i, j, x);
                } catch (NumberFormatException ignored) {

                }
            }
        }));
    }

    private static GridBagConstraints position(int x, int y) {
        return position(x, y, 1);
    }

    private static GridBagConstraints position(int x, int y, int width) {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.insets = new Insets(MARGIN, MARGIN, MARGIN, MARGIN);
        return constraints;
    }
}
