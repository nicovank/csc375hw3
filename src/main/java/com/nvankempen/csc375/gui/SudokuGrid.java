package com.nvankempen.csc375.gui;

import com.nvankempen.csc375.Sudoku;

import javax.swing.*;
import java.awt.*;

import static com.nvankempen.csc375.Constants.N;

public final class SudokuGrid extends JPanel {
    private Sudoku sudoku;

    public static final int SCALE = 40;

    public SudokuGrid() {
         this.sudoku = new Sudoku();
    }

    public SudokuGrid(Sudoku sudoku) {
        this.sudoku = sudoku;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D graphics = (Graphics2D) g.create();

        for (int i = 0; i < N * N; ++i) {
            for (int j = 0; j < N * N; ++j) {
                if (sudoku.get(i, j) != 0) {
                    graphics.drawString(
                            Long.toString(sudoku.get(i, j)),
                            i * SCALE + SCALE / 2 - 2,
                            (j + 1) * SCALE - SCALE / 2 + 5
                    );
                }
            }
        }

        graphics.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= N * N; ++i) {
            graphics.drawLine(0, i * SCALE, super.getWidth(), i * SCALE);
            graphics.drawLine(i * SCALE, 0, i * SCALE, super.getHeight());

            if (i % N == 0) {
                graphics.drawLine(0, i * SCALE + 1, super.getWidth(), i * SCALE + 1);
                graphics.drawLine(0, i * SCALE - 1, super.getWidth(), i * SCALE - 1);
                graphics.drawLine(i * SCALE + 1, 0, i * SCALE + 1, super.getHeight());
                graphics.drawLine(i * SCALE - 1, 0, i * SCALE - 1, super.getHeight());
            }
        }

        graphics.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(N * N * SCALE + 1, N * N * SCALE + 1);
    }

    public void updateSudoku(Sudoku sudoku) {
        this.sudoku = sudoku;
        repaint();
    }

    public void updateSudoku(int i, int j, byte x) {
        sudoku = sudoku.set(i, j, x);
        repaint();
    }

    public Sudoku getSudoku() {
        return sudoku;
    }

    public void clear() {
        sudoku = new Sudoku();
        repaint();
    }
}
