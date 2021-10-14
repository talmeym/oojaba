package com.emarte.oojaba.gui;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

abstract class MouseMode {
    private String name;

    MouseMode(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }


    void activated() {

    }

    void deactivated() {

    }

    abstract String getTooltipText();

    abstract MouseListener getMouseListener();

    abstract MouseMotionListener getMouseMotionListener();
}
