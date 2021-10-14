package com.emarte.oojaba.gui;

import com.emarte.oojaba.data.EndPoint;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

class PointerMouseMode extends MouseMode {
    private CanvasPanel canvasPanel;

    private EntitySprite entityToMove;
    private Dimension entityClickOffset;
    private InteractionSprite elbowToMove;
    private int elbowIndex;
    private EndPoint endPointToMove;
    private int endPointIndex;

    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            entityToMove = null;
            elbowToMove = null;
            entityClickOffset = null;

            List<InteractionSprite> interactionSprites = InteractionSprite.getInteractionSprites();

            for (int i = interactionSprites.size() - 1; i >= 0; i--) {
                InteractionSprite sprite = interactionSprites.get(i);
                int index;

                if((index = sprite.linePointClicked(e.getPoint())) > -1) {
                    elbowToMove = sprite;
                    elbowIndex = index;
                    sprite.setElbowSelected(index);
                    canvasPanel.repaint();
                    return;
                }
            }

            List<EntitySprite> entitySprites = EntitySprite.getEntitySprites();

            for (int i = entitySprites.size() - 1; i >= 0; i--) {
                EntitySprite sprite = entitySprites.get(i);
                int index;

                if (sprite.entityClicked(e.getPoint())) {
                    entityToMove = sprite;
                    entityClickOffset = new Dimension(e.getX() - sprite.getPosition().x, e.getY() - sprite.getPosition().y);
                    sprite.setEntitySelected(true);
                    canvasPanel.repaint();
                    return;
                }

                if((index = sprite.endPointClicked(e.getPoint())) != -1) {
                    endPointToMove = sprite.getEntity().getEndPoints().get(index);
                    endPointIndex = index;
                    EntitySprite.forEntity(endPointToMove.getEntity()).setEndPointSelected(index);
                    canvasPanel.repaint();
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(entityToMove != null) {
                entityToMove.setEntitySelected(false);
            }

            if(elbowToMove != null) {
                elbowToMove.setElbowSelected(-1);
            }

            if(endPointToMove != null) {
                EntitySprite.forEntity(endPointToMove.getEntity()).resetEndPointsSelected();
            }

            canvasPanel.repaint();
        }
    };

    private MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
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

        @Override
        public void mouseDragged(MouseEvent e) {
            if(entityToMove != null) {
                entityToMove.setPosition(new Point(e.getX() - entityClickOffset.width, e.getY() - entityClickOffset.height));
                canvasPanel.repaint();
            }

            if(elbowToMove != null) {
                elbowToMove.setElbowPoint(elbowIndex, e.getPoint());
                canvasPanel.repaint();
            }

            if(endPointToMove != null) {
                List<EntitySprite> entitySprites = EntitySprite.getEntitySprites();

                for (int i = entitySprites.size() - 1; i >= 0; i--) {
                    EntitySprite sprite = entitySprites.get(i);

                    if(sprite.getEntity() == endPointToMove.getEntity()) {
                        int index;

                        if ((index = sprite.endPointClicked(e.getPoint())) != -1) {
                            if(index != endPointIndex) {
                                sprite.getEntity().getEndPoints().remove(endPointIndex);
                                sprite.getEntity().getEndPoints().add(index, endPointToMove);
                                endPointIndex = index;
                                sprite.setEndPointSelected(index);
                                sprite.setEndpointViewAt(index, true, true);
                                canvasPanel.repaint();
                            }
                        }
                    }
                }
            }
        }
    };

    PointerMouseMode(CanvasPanel canvasPanel) {
        super("P - Pointer");
        this.canvasPanel = canvasPanel;
    }

    @Override
    void activated() {
        canvasPanel.showElbows(true);
        canvasPanel.showPossibleElbows(true);
        canvasPanel.showEndPoints(true);
    }

    @Override
    void deactivated() {
        canvasPanel.showElbows(false);
        canvasPanel.showPossibleElbows(false);
        canvasPanel.showEndPoints(true);
    }

    @Override
    String getTooltipText() {
        return "Select and move drawing entities (Services, endpoints, elbow points etc.)";
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
