package com.emarte.oojaba.gui;

import com.emarte.oojaba.data.EndPoint;
import com.emarte.oojaba.data.Entity;
import com.emarte.oojaba.data.Interaction;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

class EraserMouseMode extends MouseMode {
    private CanvasPanel canvasPanel;

    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            List<InteractionSprite> interactionSprites = InteractionSprite.getInteractionSprites();

            for (int i = interactionSprites.size() - 1; i >= 0; i--) {
                InteractionSprite sprite = interactionSprites.get(i);
                int index;

                if ((index = sprite.linePointClicked(e.getPoint())) > -1) {
                    if (JOptionPane.showConfirmDialog(canvasPanel, "Delete Elbow Point ?", "Delete Elbow ?", OK_CANCEL_OPTION, WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                        sprite.deleteElbowPoint(index);
                        canvasPanel.repaint();
                        return;
                    }
                }
            }

            List<EntitySprite> entitySprites = EntitySprite.getEntitySprites();

            for (int i = entitySprites.size() - 1; i >= 0; i--) {
                EntitySprite sprite = entitySprites.get(i);

                if (sprite.entityClicked(e.getPoint())) {
                    sprite.setEntitySelected(true);
                    canvasPanel.repaint();
                    Entity entity = sprite.getEntity();

                    if (JOptionPane.showConfirmDialog(canvasPanel, "Delete Service '" + entity.getName() + "'?", "Delete Service ?", OK_CANCEL_OPTION, WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                        List<Interaction> interactions = Interaction.getInteractions().stream().filter(in -> in.involves(entity)).collect(Collectors.toList());
                        Interaction.getInteractions().removeAll(interactions);
                        interactions.forEach(interaction -> InteractionSprite.forInteraction(interaction).getInteractions().remove(interaction));
                        Entity.getEntities().remove(entity);
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

                    if (JOptionPane.showConfirmDialog(canvasPanel, "Delete end point '" + endPoint.getDisplayText(false) + "'?", "Delete EndPoint ?", OK_CANCEL_OPTION, WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                        List<Interaction> interactions = Interaction.getInteractions().stream().filter(in -> in.involves(endPoint)).collect(Collectors.toList());
                        interactions.forEach(interaction -> {
                            Interaction.getInteractions().remove(interaction);
                            InteractionSprite.forInteraction(interaction).getInteractions().remove(interaction);
                            interaction.getActor().getEntity().getInteractions().remove(interaction);
                        });
                        sprite.getEntity().getEndPoints().remove(endPoint);
                    }

                    sprite.resetEndPointsSelected();
                    canvasPanel.repaint();
                    return;
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

    EraserMouseMode(CanvasPanel canvasPanel) {
        super("E - Eraser");
        this.canvasPanel = canvasPanel;
    }

    @Override
    void activated() {
        canvasPanel.showEndPoints(true);
        canvasPanel.showElbows(true);
        canvasPanel.showRemoveEntity(true);
    }

    @Override
    void deactivated() {
        canvasPanel.showEndPoints(false);
        canvasPanel.showElbows(false);
        canvasPanel.showRemoveEntity(false);
    }

    @Override
    String getTooltipText() {
        return "Remove drawing entities (services, endpoints, elbow points etc.)";
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
