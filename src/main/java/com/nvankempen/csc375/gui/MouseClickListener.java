package com.nvankempen.csc375.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

public final class MouseClickListener implements MouseListener {

    private final Consumer<MouseEvent> handler;

    public MouseClickListener(Consumer<MouseEvent> handler) {
        this.handler = handler;
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        handler.accept(event);
    }

    @Override
    public void mousePressed(MouseEvent event) {

    }

    @Override
    public void mouseReleased(MouseEvent event) {

    }

    @Override
    public void mouseEntered(MouseEvent event) {

    }

    @Override
    public void mouseExited(MouseEvent event) {

    }
}
