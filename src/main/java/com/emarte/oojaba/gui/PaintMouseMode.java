package com.emarte.oojaba.gui;

import com.emarte.oojaba.data.EndPoint;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

import static java.awt.Color.white;

class PaintMouseMode extends MouseMode {
    public static JColorChooser COLOR_CHOOSER = new JColorChooser();
    private CanvasPanel canvasPanel;

    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            List<EntitySprite> entitySprites = EntitySprite.getEntitySprites();

            for (int i = entitySprites.size() - 1; i >= 0; i--) {
                EntitySprite sprite = entitySprites.get(i);

                if (sprite.entityClicked(e.getPoint())) {
                    sprite.setEntitySelected(true);
                    canvasPanel.repaint();
                    COLOR_CHOOSER.setColor(sprite.getColor() != null ? sprite.getColor() : white);
                    JDialog dialog = JColorChooser.createDialog(canvasPanel, "Choose Colour", true, COLOR_CHOOSER, e1 -> sprite.setColor(COLOR_CHOOSER.getColor()), null);
                    dialog.setVisible(true);

                    sprite.setEntitySelected(false);
                    canvasPanel.repaint();
                    return;
                }

                int index;

                if((index = sprite.endPointClicked(e.getPoint())) >= 0) {
                    sprite.setEndPointSelected(index);
                    canvasPanel.repaint();

                    EndPoint endPoint = sprite.getEntity().getEndPoints().get(index);

                    // color chooser

                    sprite.resetEndPointsSelected();
                    canvasPanel.repaint();
                }
            }
        }
    };

    private MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            // do nothing
        }
    };

    PaintMouseMode(CanvasPanel canvasPanel) {
        super("A - Painter");
        this.canvasPanel = canvasPanel;
    }

    @Override
    void activated() {
        canvasPanel.showEndPoints(true);
    }

    @Override
    void deactivated() {
        canvasPanel.showEndPoints(false);
    }

    @Override
    String getTooltipText() {
        return "Choose the colour of drawing entities (Services, endpoints etc.)";
    }

    @Override
    MouseListener getMouseListener() {
        return mouseListener;
    }

    @Override
    MouseMotionListener getMouseMotionListener() {
        return mouseMotionListener;
    }
}
