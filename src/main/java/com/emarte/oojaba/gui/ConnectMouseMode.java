package com.emarte.oojaba.gui;

import com.emarte.oojaba.data.EndPoint;
import com.emarte.oojaba.data.EndPointType;
import com.emarte.oojaba.data.Entity;
import com.emarte.oojaba.data.Interaction;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import static com.emarte.oojaba.data.Interaction.InteractionType;
import static java.util.UUID.randomUUID;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;

public class ConnectMouseMode extends MouseMode {
    private final CanvasPanel canvasPanel;

    private EndPoint actorEndPoint;

    private MouseListener mouseListener = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            List<EntitySprite> entitySprites = EntitySprite.getEntitySprites();

            for (int i = entitySprites.size() - 1; i >= 0; i--) {
                EntitySprite sprite = entitySprites.get(i);

                if (sprite.addEndPointClicked(e.getPoint())) {
                    Entity entity = sprite.getEntity();

                    EndPointType[] endPointTypes = EndPointType.values();
                    int type = JOptionPane.showOptionDialog(canvasPanel, "Choose endpoint type", "Choose Type", OK_CANCEL_OPTION, INFORMATION_MESSAGE, null, endPointTypes, endPointTypes[0]);

                    if(type != -1) {
                        EndPointType endPointType = endPointTypes[type];
                        String text = JOptionPane.showInputDialog(canvasPanel, "Enter endpoint attributes - " + endPointType.getExplanationText(), "New " + endPointType.name() + " EndPoint", JOptionPane.INFORMATION_MESSAGE);

                        if(text != null) {
                            entity.addEndPoint(randomUUID(), text, endPointType);
                            canvasPanel.repaint();
                            return;
                        }
                    }
                }

                int index;

                if((index = sprite.endPointClicked(e.getPoint())) >= 0) {
                    if(actorEndPoint == null) {
                        actorEndPoint = sprite.getEntity().getEndPoints().get(index);
                        sprite.setEndPointSelected(index);
                        canvasPanel.repaint();
                    } else {
                        if(sprite.getEntity().getEndPoints().get(index) == actorEndPoint) {
                            sprite.resetEndPointsSelected();
                            actorEndPoint = null;
                            canvasPanel.repaint();
                            return;
                        } else if(sprite.getEntity() == actorEndPoint.getEntity()) {
                            actorEndPoint = sprite.getEntity().getEndPoints().get(index);
                            sprite.setEndPointSelected(index);
                            canvasPanel.repaint();
                        } else {
                            EndPoint destinationEndPoint = sprite.getEntity().getEndPoints().get(index);
                            sprite.setEndPointSelected(index);
                            canvasPanel.repaint();

                            List<InteractionType> interactionTypes = InteractionType.compatibleWith(destinationEndPoint.getType());
                            InteractionType interactionType = null;

                            if(interactionTypes.size() > 1) {
                                int type = JOptionPane.showOptionDialog(canvasPanel, "Choose interaction type", "Choose Type", OK_CANCEL_OPTION, INFORMATION_MESSAGE, null, interactionTypes.toArray(new InteractionType[0]), interactionTypes.get(0));

                                if(type != -1) {
                                    interactionType = interactionTypes.get(type);
                                }
                            } else if(interactionTypes.size() == 1){
                                interactionType = interactionTypes.get(0);
                            } else {
                                JOptionPane.showMessageDialog(canvasPanel, "No interaction is possible with that endpoint type (in that direction)", "No interaction possible", JOptionPane.WARNING_MESSAGE);
                            }

                            if(interactionType != null) {
                                Interaction existingInteraction = Interaction.existing(actorEndPoint, destinationEndPoint, interactionType);
                                Interaction oppositeInteraction = Interaction.existing(destinationEndPoint, actorEndPoint, interactionType);
                                boolean leaveActorSelected = false;

                                if(existingInteraction != null) {
                                    if(JOptionPane.showConfirmDialog(canvasPanel, "Connection already exists, did you intend to remove that connection ?", "Remove connection ?", OK_CANCEL_OPTION, INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {
                                        actorEndPoint.getEntity().getInteractions().remove(existingInteraction);
                                        Interaction.getInteractions().remove(existingInteraction);
                                        InteractionSprite.forInteraction(existingInteraction).getInteractions().remove(existingInteraction);
                                        leaveActorSelected = true;
                                    }
                                } else if (oppositeInteraction != null) {
                                    JOptionPane.showMessageDialog(canvasPanel, "Those endpoints are already connected in the opposite direction.", "Cyclic interaction", JOptionPane.WARNING_MESSAGE);
                                } else {
                                    if(JOptionPane.showConfirmDialog(canvasPanel, "Connect '" + actorEndPoint.getDisplayText(false) + "' to '" + destinationEndPoint.getDisplayText(false) + "' ?", "Add connection ?", OK_CANCEL_OPTION, INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {
                                        actorEndPoint.getEntity().addInteraction(actorEndPoint, destinationEndPoint, interactionType);
                                        leaveActorSelected = true;
                                    }
                                }

                                if(!leaveActorSelected) {
                                    EntitySprite.forEntity(actorEndPoint.getEntity()).resetEndPointsSelected();
                                    actorEndPoint = null;
                                }
                            }

                            EntitySprite.forEntity(destinationEndPoint.getEntity()).resetEndPointsSelected();
                            canvasPanel.repaint();
                        }
                    }
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

                if((index = sprite.endPointClicked(e.getPoint())) != -1) {
                    sprite.setEndpointViewAt(index, true, true);
                }

                canvasPanel.repaint();
            }
        }
    };

    ConnectMouseMode(CanvasPanel canvasPanel) {
        super("C - Connector");
        this.canvasPanel = canvasPanel;
    }

    @Override
    void activated() {
        canvasPanel.showEndPoints(true);
        canvasPanel.showAddEndPoint(true);
        canvasPanel.showConnections(true);
    }

    @Override
    void deactivated() {
        if(actorEndPoint != null) {
            EntitySprite.forEntity(actorEndPoint.getEntity()).resetEndPointsSelected();
            actorEndPoint = null;
        }

        canvasPanel.showEndPoints(false);
        canvasPanel.showAddEndPoint(false);
        canvasPanel.showConnections(false);
    }

    @Override
    String getTooltipText() {
        return "Add connections between endpoints to the drawing";
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
