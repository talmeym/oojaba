package com.emarte.oojaba.gui;

import com.emarte.oojaba.data.EndPoint;
import com.emarte.oojaba.data.Interaction;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.emarte.oojaba.gui.EntitySprite.forEntity;
import static java.awt.Color.black;
import static java.awt.Color.white;

public class CanvasPanel extends JComponent {

    private final Dimension canvasSize;
    private final CanvasChangeListener canvasChangeListener;
    private Font font;

    private final List<MouseMode> mouseModes = new ArrayList<>();
    private MouseMode currentMouseMode;
    private final List<MouseModeChangeListener> mouseModeListeners = new ArrayList<>();
    private boolean showGrid = true;
    private boolean showEndPoints;
    private boolean showAddEndPoint;
    private boolean showRemoveEntity;
    private boolean showEndPointConnections;
    private boolean showElbows;
    private boolean showPossibleElbows;
    private boolean showConnectionsOutwards = true;
    private boolean showUsedEndPoints;
    private Color selectedColor = new Color(255, 154, 28);

    CanvasPanel(Dimension startingSize, CanvasChangeListener canvasChangeListener) {
        this.canvasSize = startingSize;
        this.canvasChangeListener = canvasChangeListener;
        mouseModes.add(new PointerMouseMode(this));
        mouseModes.add(new EraserMouseMode(this));
        mouseModes.add(new ConnectMouseMode(this));
        mouseModes.add(new ViewMouseMode(this));
        mouseModes.add(new EditMouseMode(this));
        mouseModes.add(new PaintMouseMode(this));
        setMouseMode(mouseModes.get(0)); // default mouse mode
        font = (Font) UIManager.get("Button.font");
    }

    @Override
    public void paint(Graphics g) {
        List<EntitySprite> entitySprites = EntitySprite.getEntitySprites();

        g.setFont(font);

        for (EntitySprite sprite : entitySprites) {
            sprite.initiate(g);
        }

        g.setColor(white);
        g.fillRect(0, 0, canvasSize.width, canvasSize.height);

        if (showGrid) {
            g.setColor(new Color(230, 230, 230));
            int gridSize = 25;

            for (int i = 0; i * gridSize < canvasSize.height; i++) {
                g.drawLine(0, i * gridSize, canvasSize.width, i * gridSize);
            }

            for (int i = 0; i * gridSize < canvasSize.width; i++) {
                g.drawLine(i * gridSize, 0, i * gridSize, canvasSize.height);
            }
        }

        g.setColor(black);

        for (EntitySprite entitySprite : entitySprites) {
            entitySprite.paint(g, showEndPoints, showAddEndPoint, showRemoveEntity, selectedColor, showUsedEndPoints);
        }

        List<InteractionSprite> allInteractions = InteractionSprite.getInteractionSprites();

        for (InteractionSprite interactionSprite : allInteractions) {
            interactionSprite.paintLabels(g);
        }

        for (InteractionSprite interactionSprite : allInteractions) {
            interactionSprite.paintLines(g, showElbows, showPossibleElbows, selectedColor);
        }

        if (showEndPointConnections) {
            Set<Interaction> selectedInteractions = new HashSet<>();
            boolean foundEndPointSelected = false;

            for (EntitySprite entitySprite : entitySprites) {
                if (entitySprite.isEndPointSelected()) {
                    for(int endPointSelected: entitySprite.getEndPointsSelected()) {
                        EndPoint selectedEndPoint = entitySprite.getEntity().getEndPoints().get(endPointSelected);
                        if(showConnectionsOutwards) {
                            selectedInteractions.addAll(getOutwardConnectedInteractions(selectedEndPoint));
                            selectedInteractions.addAll(Interaction.getInteractions().stream().filter(interaction -> interaction.getActor() == selectedEndPoint).collect(Collectors.toSet()));
                        } else {
                            selectedInteractions.addAll(getInwardConnectedInteractions(selectedEndPoint));
                            selectedInteractions.addAll(Interaction.getInteractions().stream().filter(interaction -> interaction.getDestination() == selectedEndPoint).collect(Collectors.toSet()));
                        }
                        foundEndPointSelected = true;
                    }
                }
            }

            g.setColor(selectedColor);

            for (InteractionSprite interactionSprite : allInteractions) {
                for (Interaction interaction : interactionSprite.getInteractions()) {
                    if(!foundEndPointSelected || selectedInteractions.contains(interaction)) {
                        EndPoint actor = interaction.getActor();
                        Point centre1 = forEntity(actor.getEntity()).getCentreOfEndPoint(actor);
                        g.fillOval(centre1.x - 4, centre1.y - 4, 8, 8);
                        EndPoint destination = interaction.getDestination();
                        Point centre2 = forEntity(destination.getEntity()).getCentreOfEndPoint(destination);
                        g.fillOval(centre2.x - 4, centre2.y - 4, 8, 8);
                        g.drawLine(centre1.x, centre1.y, centre2.x, centre2.y);
                    }
                }
            }
        }
    }

    private Set<Interaction> getOutwardConnectedInteractions(EndPoint endPoint) {
        Set<Interaction> result = new HashSet<>();
        Set<Interaction> directInteractions = Interaction.getInteractions().stream().filter(interaction -> (interaction.getType().isOutward() && interaction.getActor() == endPoint) || (!interaction.getType().isOutward() && interaction.getDestination() == endPoint)).collect(Collectors.toSet());
        directInteractions.forEach(interaction -> {
            result.add(interaction);
            result.addAll(getOutwardConnectedInteractions(interaction.getType().isOutward() ? interaction.getDestination() : interaction.getActor()));
        });

        return result;
    }

    private Set<Interaction> getInwardConnectedInteractions(EndPoint endPoint) {
        Set<Interaction> result = new HashSet<>();
        Set<Interaction> directInteractions = Interaction.getInteractions().stream().filter(interaction -> (interaction.getType().isOutward() && interaction.getDestination() == endPoint) || (!interaction.getType().isOutward() && interaction.getActor() == endPoint)).collect(Collectors.toSet());
        directInteractions.forEach(interaction -> {
            result.add(interaction);
            result.addAll(getInwardConnectedInteractions(interaction.getType().isOutward() ? interaction.getActor() : interaction.getDestination()));
        });

        return result;
    }

    List<MouseMode> getMouseModes() {
        return mouseModes;
    }

    @Override
    public Dimension getMinimumSize() {
        return canvasSize;
    }

    @Override
    public Dimension getMaximumSize() {
        return canvasSize;
    }

    @Override
    public Dimension getPreferredSize() {
        return canvasSize;
    }

    public void makeWider(int amount) {
        canvasSize.width += amount;
    }

    public void makeTaller(int amount) {
        canvasSize.height += amount;
    }

    public void setCanvasSize(int width, int height) {
        canvasSize.width = width;
        canvasSize.height = height;
    }

    void setMouseMode(MouseMode mouseMode) {
        if (currentMouseMode != null) {
            removeMouseListener(currentMouseMode.getMouseListener());
            removeMouseMotionListener(currentMouseMode.getMouseMotionListener());
            currentMouseMode.deactivated();
        }

        addMouseListener(mouseMode.getMouseListener());
        addMouseMotionListener(mouseMode.getMouseMotionListener());
        mouseMode.activated();
        currentMouseMode = mouseMode;
        mouseModeListeners.forEach(listenerList -> listenerList.mouseModeChanged(currentMouseMode));
    }

    public boolean getShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
        canvasChangeListener.showGrid(showGrid);
    }

    public void showEndPoints(boolean showEndPoints) {
        this.showEndPoints = showEndPoints;
        repaint();
    }

    public void showAddEndPoint(boolean showAddEndPoint) {
        this.showAddEndPoint = showAddEndPoint;
        repaint();
    }

    public void showRemoveEntity(boolean showRemoveEntity) {
        this.showRemoveEntity = showRemoveEntity;
        repaint();
    }

    public void showConnections(boolean showConnections) {
        this.showEndPointConnections = showConnections;
        repaint();
    }

    public void showElbows(boolean showElbows) {
        this.showElbows = showElbows;
    }

    public void showPossibleElbows(boolean showPossibleElbows) {
        this.showPossibleElbows = showPossibleElbows;
    }

    public void increaseFontSize() {
        font = new Font(font.getName(), font.getStyle(), font.getSize() + 1);
    }

    public void decreaseFontSize() {
        font = new Font(font.getName(), font.getStyle(), font.getSize() - 1);
    }

    public int getFontSize() {
        return font.getSize();
    }

    public void setFontSize(int fontSize) {
        font = new Font(font.getName(), font.getStyle(), fontSize);
    }

    public void moveAllDown(int amount) {
        makeTaller(amount);
        EntitySprite.getEntitySprites().forEach(entitySprite -> entitySprite.applyOffset(0, amount));
        InteractionSprite.getInteractionSprites().forEach(interactionSprite -> interactionSprite.applyOffset(0, amount));
    }

    public void moveAllRight(int amount) {
        makeWider(amount);
        EntitySprite.getEntitySprites().forEach(entitySprite -> entitySprite.applyOffset(amount, 0));
        InteractionSprite.getInteractionSprites().forEach(interactionSprite -> interactionSprite.applyOffset(amount, 0));
    }

    public void setPointerMouseMode() {
        setMouseMode(mouseModes.get(0));
    }

    public void setEraserMouseMode() {
        setMouseMode(mouseModes.get(1));
    }

    public void setConnectorMouseMode() {
        setMouseMode(mouseModes.get(2));
    }

    public void setViewerMouseMode() {
        setMouseMode(mouseModes.get(3));
    }

    public void setEditorMouseMode() {
        setMouseMode(mouseModes.get(4));
    }

    public void setPainterMouseMode() {
        setMouseMode(mouseModes.get(5));
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
    }

    public Color getSelectedColor() {
        return selectedColor;
    }

    public boolean getShowConnectionsOutwards() {
        return showConnectionsOutwards;
    }

    public void setShowConnectionsOutwards(boolean showConnectionsOutwards) {
        this.showConnectionsOutwards = showConnectionsOutwards;
        canvasChangeListener.showConnectionsOutwardsChanged(showConnectionsOutwards);
    }

    public boolean getShowUsedEndPoints() {
        return showUsedEndPoints;
    }

    public void setShowUsedEndPoints(boolean showUsedEndPoints) {
        this.showUsedEndPoints = showUsedEndPoints;
        canvasChangeListener.showUsedEndPoints(showUsedEndPoints);
    }

    public void addMouseModeChangeListener(MouseModeChangeListener listener) {
        mouseModeListeners.add(listener);
    }

    interface MouseModeChangeListener {
        void mouseModeChanged(MouseMode mouseMode);
    }
}