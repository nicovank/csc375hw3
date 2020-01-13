package com.nvankempen.csc375;

import com.nvankempen.csc375.utils.BitPackedArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static com.nvankempen.csc375.Constants.BITS;
import static com.nvankempen.csc375.Constants.N;

public final class Sudoku {
    private final BitPackedArray array;
    private final int count;

    public Sudoku() {
        array = new BitPackedArray(N * N * N * N, BITS);
        count = 0;
    }

    public Sudoku(long[][] grid) {
        array = new BitPackedArray(N * N * N * N, BITS);

        int count = 0;

        for (int i = 0; i < N * N; ++i) {
            for (int j = 0; j < N * N; ++j) {
                if (grid[i][j] != 0) {
                    array.set(N * N * i + j, grid[i][j]);
                    ++count;
                }
            }
        }

        this.count = count;
    }

    private Sudoku(BitPackedArray array) {
        this.array = array;

        int count = 0;

        for (int i = 0; i < N * N; ++i) {
            for (int j = 0; j < N * N; ++j) {
                if (isSet(i, j)) ++count;
            }
        }

        this.count = count;
    }

    private Sudoku(BitPackedArray array, int count) {
        this.array = array;
        this.count = count;
    }

    public final boolean isValid() {
        for (int i = 0; i < N * N; ++i) {
            final BitSet row = new BitSet(N * N);
            final BitSet column = new BitSet(N * N);

            for (int j = 0; j < N * N; ++j) {
                if (array.get(N * N * i + j) != 0 && row.get((int) array.get(N * N * i + j))) return false;
                else row.set((int) array.get(N * N * i + j));

                if (array.get(N * N * j + i) != 0 && column.get((int) array.get(N * N * j + i))) return false;
                else column.set((int) array.get(N * N * j + i));
            }
        }

        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; j++) {
                final BitSet block = new BitSet(N * N);

                for (int k = 0; k < N * N; k++) {
                    final int a = N * i + k % N;
                    final int b = N * j + k / N;

                    if (array.get(N * N * a + b) != 0 && block.get((int) array.get(N * N * a + b))) return false;
                    else block.set((int) array.get(N * N * a + b));
                }
            }
        }

        return true;
    }

    public final List<Long> whatCanWeSetAt(int x, int y) {
        final BitSet set = new BitSet(N * N);
        set.set(0, N * N);

        final int a = x / N;
        final int b = y / N;

        for (int i = 0; i < N * N; ++i) {
            if (i != x && array.get(N * N * i + y) != 0) set.clear((int) array.get(N * N * i + y) - 1);
            if (i != y && array.get(N * N * x + i) != 0) set.clear((int) array.get(N * N * x + i) - 1);

            final int c = N * a + i % N;
            final int d = N * b + i / N;
            if ((c != x || d != y) && array.get(N * N * c + d) != 0) set.clear((int) array.get(N * N * c + d) - 1);
        }

        final List<Long> possibilities = new ArrayList<>(set.cardinality());
        for (int i = 0; i < N * N; ++i) {
            if (set.get(i))
                possibilities.add((long) (i + 1));
        }

        return possibilities;
    }

    public final int remaining() {
        return N * N * N * N - count;
    }

    public final long get(int i, int j) {
        return array.get(N * N * i + j);
    }

    public Sudoku set(int i, int j, long v) {
        final BitPackedArray array = new BitPackedArray(N * N * N * N, BITS);

        int count = 0;

        for (int x = 0; x < N * N; ++x) {
            for (int y = 0; y < N * N; ++y) {
                final long value = (x == i && y == j) ? v : this.array.get(N * N * x + y);
                if (value != 0) {
                    array.set(N * N * x + y, value);
                    ++count;
                }
            }
        }

        return new Sudoku(array, count);
    }

    public boolean isSet(int i, int j) {
        return array.get(N * N * i + j) != 0;
    }

    public void serialize(OutputStream out) throws IOException {
        array.serialize(out);
    }

    public static Sudoku deserialize(ByteBuffer in) {
        return new Sudoku(BitPackedArray.deserialize(in));
    }

    @Override
    public String toString() {
        return array.toString();
    }
}
