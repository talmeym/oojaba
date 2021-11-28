package com.emarte.oojaba.gui;

import com.emarte.oojaba.data.EndPoint;
import com.emarte.oojaba.data.Entity;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.awt.Color.black;
import static java.awt.Color.white;
import static java.awt.Font.BOLD;

public class EntitySprite {
    private static final int CIRC_RAD = 6;
    private static final int ENDPOINT_ARC = 6;
    private static final int ENTITY_ARC = 20;
    public static int ENTITY_BORDER_SIZE = 50;
    public static int ENDPOINT_BORDER_SIZE = 5;
    private static final Map<Entity, EntitySprite> ENTITY_LOOKUP = new LinkedHashMap<>();

    private final Entity entity;
    private Point position;
    private Dimension size;
    private Font font;
    private Color color;

    private boolean entitySelected;
    private int[] endPointsSelected = null;
    private int viewIndex = -1;
    private boolean viewAlternative = false;
    private boolean viewBold = false;

    public EntitySprite(Entity entity, Point position, Color color) {
        this.entity = entity;
        this.position = position;
        this.color = color;
    }

    @JsonGetter("entity")
    Entity getEntity() {
        return entity;
    }

    @JsonGetter("position")
    Point getPosition() {
        return position;
    }

    @JsonGetter("interactionSprites")
    List<InteractionSprite> getInteractions() {
        return entity.getInteractions().stream().map(InteractionSprite::forInteraction).distinct().collect(Collectors.toList());
    }

    Point getCentreOfEndPoint(EndPoint endPoint) {
        if (!entity.getEndPoints().contains(endPoint)) {
            throw new IllegalArgumentException("boom"); //TODO proper error message
        }

        int index = entity.getEndPoints().indexOf(endPoint);
        String endPointText = endPoint.getDisplayText(false);
        FontRenderContext fontRenderContext = new FontRenderContext(null, false, false);
        Rectangle2D endPointBounds = font.getStringBounds(endPointText, fontRenderContext);
        int width = (int) endPointBounds.getWidth();
        int height = (int) endPointBounds.getHeight();

        return new Point(position.x + size.width + ENDPOINT_BORDER_SIZE - ((width + ENDPOINT_BORDER_SIZE * 2) / 2),
                position.y + size.height + (index * (height + ENDPOINT_BORDER_SIZE * 2)) + height);
    }

    boolean entityClicked(Point point) {
        return point.x > position.x && point.x < (position.x + size.width) && point.y > position.y && point.y < (position.y + size.height);
    }

    boolean addEndPointClicked(Point point) {
        return point.x > (position.x + size.width - 15) && point.x < (position.x + size.width - 5) &&
                point.y > (position.y + size.height - 15 - ENDPOINT_BORDER_SIZE) && point.y < (position.y + size.height - 5 - ENDPOINT_BORDER_SIZE);
    }

    public int endPointClicked(Point point) {
        int index = 0;

        for (EndPoint endPoint : getEntity().getEndPoints()) {
            boolean endPointWithView = viewIndex == entity.getEndPoints().indexOf(endPoint);
            String endPointText = endPoint.getDisplayText(endPointWithView && viewAlternative);
            FontRenderContext fontRenderContext = new FontRenderContext(null, false, false);
            Font fontToUse = endPointWithView && viewBold ? font.deriveFont(BOLD) : font;
            Rectangle2D endPointBounds = fontToUse.getStringBounds(endPointText, fontRenderContext);
            int width = (int) endPointBounds.getWidth();
            int height = (int) endPointBounds.getHeight();

            if (point.x > position.x + size.width - width - ENDPOINT_BORDER_SIZE && point.x < position.x + size.width + ENDPOINT_BORDER_SIZE &&
                    point.y > position.y + size.height - ENDPOINT_BORDER_SIZE + (index * (height + ENDPOINT_BORDER_SIZE * 2)) &&
                    point.y < position.y + size.height + height + ENDPOINT_BORDER_SIZE + (index * (height + ENDPOINT_BORDER_SIZE * 2))) {
                return index;
            }

            index++;
        }

        return -1;
    }

    Point getEntryPoint() {
        return new Point(position.x, position.y + size.height / 2);
    }

    Point getExitPoint() {
        return new Point(position.x + size.width, position.y + size.height / 2);
    }

    @JsonIgnore
    public boolean isEndPointSelected() {
        return endPointsSelected != null;
    }

    @JsonIgnore
    public int[] getEndPointsSelected() {
        return endPointsSelected;
    }

    @JsonIgnore
    public Color getColor() {
        return color;
    }

    public Integer getColorRgb() {
        return color != null ? color.getRGB() : null;
    }

    void setPosition(Point position) {
        this.position = position;
    }

    void initiate(Graphics g) {
        Rectangle2D bounds = g.getFont().getStringBounds(entity.getName(), new FontRenderContext(null, false, false));
        size = new Dimension(2 * ENTITY_BORDER_SIZE + (int) bounds.getWidth(), 2 * ENTITY_BORDER_SIZE + (int) bounds.getHeight());
    }

    void paint(Graphics g, boolean showEndPoints, boolean showAddEndPoint, boolean showRemoveEntity, Color selectedColor, boolean highlightUsedEndPoints) {
        font = g.getFont();
        FontRenderContext fontRenderContext = new FontRenderContext(null, false, false);

        Rectangle2D bounds = font.getStringBounds(entity.getName(), fontRenderContext);
        g.setColor(color != null ? color : white);
        g.fillRoundRect(position.x, position.y, size.width, size.height, ENTITY_ARC, ENTITY_ARC);

        g.setColor(entitySelected ? selectedColor : black);
        g.drawRoundRect(position.x, position.y, size.width, size.height, ENTITY_ARC, ENTITY_ARC);

        g.setColor(black);
        g.drawString(entity.getName(), position.x + ENTITY_BORDER_SIZE, position.y + ENTITY_BORDER_SIZE + (int) bounds.getHeight() - 3);

        g.setColor(white);
        fillOval(g, getEntryPoint());
        fillOval(g, getExitPoint());

        g.setColor(black);
        drawOval(g, getEntryPoint());
        drawOval(g, getExitPoint());

        if (showEndPoints) {
            Font boldFont = font.deriveFont(BOLD);
            int index = 0;

            for (EndPoint endPoint : getEntity().getEndPoints()) {
                boolean endPointWithView = viewIndex == entity.getEndPoints().indexOf(endPoint);
                String endPointText = endPoint.getDisplayText(endPointWithView && viewAlternative);
                boolean endPointHasConnections = InteractionSprite.getInteractionSprites().stream().flatMap(is -> is.getInteractions().stream()).anyMatch(i -> i.involves(endPoint));
                Font fontToUse = (highlightUsedEndPoints && endPointHasConnections) || (endPointWithView && viewBold) ? boldFont : font;
                Rectangle2D endPointBounds = fontToUse.getStringBounds(endPointText, fontRenderContext);
                int width = (int) endPointBounds.getWidth();
                int height = (int) endPointBounds.getHeight();

                g.setColor(white);
                g.fillRoundRect(position.x + size.width - width - ENDPOINT_BORDER_SIZE, position.y + size.height - ENDPOINT_BORDER_SIZE + (index * (height + ENDPOINT_BORDER_SIZE * 2)), width + ENDPOINT_BORDER_SIZE * 2, height + ENDPOINT_BORDER_SIZE * 2, ENDPOINT_ARC, ENDPOINT_ARC);

                g.setColor(black);
                g.drawRoundRect(position.x + size.width - width - ENDPOINT_BORDER_SIZE, position.y + size.height - ENDPOINT_BORDER_SIZE + (index * (height + ENDPOINT_BORDER_SIZE * 2)), width + ENDPOINT_BORDER_SIZE * 2, height + ENDPOINT_BORDER_SIZE * 2, ENDPOINT_ARC, ENDPOINT_ARC);

                g.setColor(black);
                g.setFont(fontToUse);
                g.drawString(endPointText, position.x + size.width - width, position.y + size.height + height - 2 + (index * (height + ENDPOINT_BORDER_SIZE * 2)));

                index++;
            }

            g.setFont(font);

            if (showAddEndPoint) {
                g.drawLine(position.x + size.width - 15, position.y + size.height - 10 - ENDPOINT_BORDER_SIZE, position.x + size.width - 5, position.y + size.height - 10 - ENDPOINT_BORDER_SIZE);
                g.drawLine(position.x + size.width - 10, position.y + size.height - 15 - ENDPOINT_BORDER_SIZE, position.x + size.width - 10, position.y + size.height - 5 - ENDPOINT_BORDER_SIZE);
            }

            if (showRemoveEntity) {
                g.drawLine(position.x + 5, position.y + 10, position.x + 15, position.y + 10);
            }

            if (endPointsSelected != null) {
                for(int endPointSelected: endPointsSelected) {
                    EndPoint endPoint = entity.getEndPoints().get(endPointSelected);
                    boolean endPointWithView = viewIndex == entity.getEndPoints().indexOf(endPoint);
                    String endPointText = endPoint.getDisplayText(endPointWithView && viewAlternative);
                    Font fontToUse = endPointWithView && viewBold ? boldFont : font;
                    Rectangle2D endPointBounds = fontToUse.getStringBounds(endPointText, fontRenderContext);
                    int width = (int) endPointBounds.getWidth();
                    int height = (int) endPointBounds.getHeight();

                    g.setColor(selectedColor);
                    g.drawRect(position.x + size.width - width - ENDPOINT_BORDER_SIZE, position.y + size.height - ENDPOINT_BORDER_SIZE + (endPointSelected * (height + ENDPOINT_BORDER_SIZE * 2)), width + ENDPOINT_BORDER_SIZE * 2, height + ENDPOINT_BORDER_SIZE * 2);
                }
            }
        }
    }

    private void drawOval(Graphics g, Point point) {
        g.drawOval(point.x - CIRC_RAD, point.y - CIRC_RAD, CIRC_RAD * 2, CIRC_RAD * 2);
    }

    private void fillOval(Graphics g, Point point) {
        g.fillOval(point.x - CIRC_RAD, point.y - CIRC_RAD, CIRC_RAD * 2, CIRC_RAD * 2);
    }

    void setEntitySelected(boolean entitySelected) {
        this.entitySelected = entitySelected;
    }

    void setAllEndPointsSelected() {
        endPointsSelected = new int[entity.getEndPoints().size()];
        AtomicInteger index = new AtomicInteger();
        entity.getEndPoints().forEach(e -> endPointsSelected[index.get()] = index.getAndIncrement());
    }

    void setEndPointSelected(int index) {
        endPointsSelected = new int[]{index};
    }

    void resetEndPointsSelected() {
        endPointsSelected = null;
    }

    public void setEndpointViewAt(int index, boolean alternative, boolean bold) {
        viewIndex = index;
        viewAlternative = alternative;
        viewBold = bold;
    }

    void resetEndPointView() {
        setEndpointViewAt(-1, false, false);
    }

    public void applyOffset(int x, int y) {
        position.translate(x, y);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    static EntitySprite forEntity(Entity entity) {
        if (!ENTITY_LOOKUP.containsKey(entity)) {
            ENTITY_LOOKUP.put(entity, new EntitySprite(entity, new Point(400, 400), null));
        }

        return ENTITY_LOOKUP.get(entity);
    }

    public static List<EntitySprite> getEntitySprites() {
        return Entity.getEntities().stream().map(EntitySprite::forEntity).collect(Collectors.toList());
    }

    public static void load(List<EntitySprite> entitySprites) {
        ENTITY_LOOKUP.clear();
        entitySprites.forEach(entitySprite -> ENTITY_LOOKUP.put(entitySprite.entity, entitySprite));
    }
}