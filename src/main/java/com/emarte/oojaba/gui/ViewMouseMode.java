package com.emarte.oojaba.gui;

import com.emarte.oojaba.data.EndPoint;
import com.emarte.oojaba.data.Entity;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

public class ViewMouseMode extends MouseMode {
    private final CanvasPanel canvasPanel;

    private Entity actorEntity;
    private EndPoint actorEndPoint;

    private final MouseListener mouseListener = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            List<EntitySprite> entitySprites = EntitySprite.getEntitySprites();

            for (int i = entitySprites.size() - 1; i >= 0; i--) {
                EntitySprite sprite = entitySprites.get(i);
                int index;

                if ((index = sprite.endPointClicked(e.getPoint())) >= 0) {
                    if(actorEntity != null) {
                        EntitySprite.forEntity(actorEntity).setEntitySelected(false);
                        EntitySprite.forEntity(actorEntity).resetEndPointsSelected();
                        actorEntity = null;
                    }

                    if (sprite.getEntity().getEndPoints().get(index) == actorEndPoint) {
                        sprite.resetEndPointsSelected();
                        actorEndPoint = null;
                    } else {
                        if(actorEndPoint != null) {
                            EntitySprite.forEntity(actorEndPoint.getEntity()).resetEndPointsSelected();
                        }

                        actorEndPoint = sprite.getEntity().getEndPoints().get(index);
                        sprite.setEndPointSelected(index);
                    }

                    canvasPanel.repaint();
                    return;
                }
            }

            for (int i = entitySprites.size() - 1; i >= 0; i--) {
                EntitySprite sprite = entitySprites.get(i);

                if(sprite.entityClicked(e.getPoint())) {
                    if(actorEndPoint != null) {
                        EntitySprite.forEntity(actorEndPoint.getEntity()).resetEndPointsSelected();
                        actorEndPoint = null;
                    }

                    if(sprite.getEntity() == actorEntity) {
                        sprite.setEntitySelected(false);
                        sprite.resetEndPointsSelected();
                        actorEntity = null;
                    } else {
                        if(actorEntity != null) {
                            EntitySprite.forEntity(actorEntity).setEntitySelected(false);
                            EntitySprite.forEntity(actorEntity).resetEndPointsSelected();
                        }

                        actorEntity = sprite.getEntity();
                        sprite.setEntitySelected(true);
                        sprite.setAllEndPointsSelected();
                    }

                    canvasPanel.repaint();
                }
            }
        }
    };

    private final MouseMotionListener mouseMotionListener = new MouseAdapter() {
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

    ViewMouseMode(CanvasPanel canvasPanel) {
        super("V - Viewer");
        this.canvasPanel = canvasPanel;
    }

    @Override
    void activated() {
        canvasPanel.showEndPoints(true);
        canvasPanel.showAddEndPoint(false);
        canvasPanel.showConnections(true);
    }

    @Override
    void deactivated() {
        if (actorEndPoint != null) {
            EntitySprite.forEntity(actorEndPoint.getEntity()).resetEndPointsSelected();
            actorEndPoint = null;
        }

        if (actorEntity != null) {
            EntitySprite.forEntity(actorEntity).setEntitySelected(false);
            EntitySprite.forEntity(actorEntity).resetEndPointsSelected();
            actorEntity = null;
        }

        canvasPanel.showEndPoints(false);
        canvasPanel.showAddEndPoint(false);
        canvasPanel.showConnections(false);
    }

    @Override
    String getTooltipText() {
        return "select and view individual endpoint connections";
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
