package com.emarte.oojaba.gui;

import com.emarte.oojaba.data.EndPoint;
import com.emarte.oojaba.data.EndPointType;
import com.emarte.oojaba.data.Entity;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

class EditMouseMode extends MouseMode {
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
                    Entity entity = sprite.getEntity();
                    String newName = (String) JOptionPane.showInputDialog(canvasPanel, "Enter service name", "Edit Service", JOptionPane.INFORMATION_MESSAGE, null, null, entity.getName());

                    if(newName != null) {
                        entity.setName(newName);
                    }

                    sprite.setEntitySelected(false);
                    canvasPanel.repaint();
                    return;
                }

                int index;

                if((index = sprite.endPointClicked(e.getPoint())) >= 0) {
                    sprite.setEndPointSelected(index);
                    canvasPanel.repaint();

                    EndPoint endPoint = sprite.getEntity().getEndPoints().get(index);
                    EndPointType endPointType = endPoint.getType();
                    String newText = (String) JOptionPane.showInputDialog(canvasPanel, "Edit endpoint attributes - " + endPointType.getExplanationText(), "Edit  " + endPointType.name() + " EndPoint", JOptionPane.INFORMATION_MESSAGE, null, null, endPoint.getAttributeText());

                    if(newText != null) {
                        endPoint.applyAttributeText(newText);
                    }

                    sprite.resetEndPointsSelected();
                    canvasPanel.repaint();
                }
            }
        }
    };

    private MouseMotionListener mouseMotionListener = new MouseAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
            List<EntitySprite> entitySprites = EntitySprite.getEntitySprites();

            for (int i = entitySprites.size() - 1; i >= 0; i--) {
                EntitySprite sprite = entitySprites.get(i);
                sprite.resetEndPointView();
                int index;

                if ((index = sprite.endPointClicked(e.getPoint())) != -1) {
                    sprite.setEndpointViewAt(index, true, true);
                }

                canvasPanel.repaint();
            }
        }
    };

    EditMouseMode(CanvasPanel canvasPanel) {
        super("D - Editor");
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
        return "Edit the text of drawing entities (services, endpoints etc.)";
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
