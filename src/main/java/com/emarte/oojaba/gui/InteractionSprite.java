package com.emarte.oojaba.gui;

import com.emarte.oojaba.data.Interaction;
import com.emarte.oojaba.data.Interaction.InteractionType;
import com.fasterxml.jackson.annotation.JsonGetter;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.emarte.oojaba.gui.EntitySprite.forEntity;
import static java.awt.Color.black;
import static java.awt.Color.white;
import static java.util.Collections.singletonList;

public class InteractionSprite {
    private static final int CIRC_DRAW_RAD = 3;
    private static final int CIRC_SELECT_RAD = 5;
    private static final Map<InteractionKey, InteractionSprite> INTERACTION_LOOKUP = new LinkedHashMap<>();

    private final List<Interaction> interactions;
    private final List<Point> elbowPoints;

    private int elbowSelected = -1;

    public InteractionSprite(List<Interaction> interactions, List<Point> elbowPoints) {
        this.interactions = interactions;
        this.elbowPoints = elbowPoints;
    }

    @JsonGetter("interactions")
    List<Interaction> getInteractions() {
        return interactions;
    }

    @JsonGetter("elbowPoints")
    List<Point> getElbowPoints() {
        return elbowPoints;
    }

    private Point getFromPoint() {
        EntitySprite fromSprite = forEntity(interactions.get(0).getActor().getEntity());
        EntitySprite toSprite = forEntity(interactions.get(0).getDestination().getEntity());
        return interactions.get(0).getType().isOutward() ? fromSprite.getExitPoint() : toSprite.getExitPoint();
    }

    private Point getToPoint() {
        EntitySprite fromSprite = forEntity(interactions.get(0).getActor().getEntity());
        EntitySprite toSprite = forEntity(interactions.get(0).getDestination().getEntity());
        return interactions.get(0).getType().isOutward() ? toSprite.getEntryPoint() : fromSprite.getEntryPoint();
    }

    Point getHalfwayPoint(Point fromPoint, Point toPoint) {
        return new Point(fromPoint.x + (toPoint.x - fromPoint.x) / 2, fromPoint.y + (toPoint.y - fromPoint.y) / 2);
    }

    int linePointClicked(Point point) {
        if (elbowPoints.size() == 0) {
            if (isNear(point, getHalfwayPoint(getFromPoint(), getToPoint()))) {
                elbowPoints.add(point);
                return elbowPoints.indexOf(point);
            }
        } else {
            if (isNear(point, getHalfwayPoint(getFromPoint(), elbowPoints.get(0)))) {
                elbowPoints.add(0, point);
                return elbowPoints.indexOf(point);
            }

            for (int i = 0; i < elbowPoints.size(); i++) {
                if (isNear(point, elbowPoints.get(i))) {
                    return i;
                }

                if (i < elbowPoints.size() - 1 && isNear(point, getHalfwayPoint(elbowPoints.get(i), elbowPoints.get(i + 1)))) {
                    elbowPoints.add(i + 1, point);
                    return elbowPoints.indexOf(point);
                }
            }

            if (isNear(point, getHalfwayPoint(elbowPoints.get(elbowPoints.size() - 1), getToPoint()))) {
                elbowPoints.add(point);
                return elbowPoints.indexOf(point);
            }
        }

        return -1;
    }

    private boolean isNear(Point point, Point near) {
        return point.x > near.x - CIRC_SELECT_RAD && point.y > near.y - CIRC_SELECT_RAD && point.x < near.x + CIRC_SELECT_RAD && point.y < near.y + CIRC_SELECT_RAD;
    }

    void setElbowPoint(int index, Point point) {
        elbowPoints.set(index, point);
    }

    void deleteElbowPoint(int index) {
        elbowPoints.remove(index);
    }

    void paintLabels(Graphics g) {
        Font font = g.getFont();
        FontRenderContext fontRenderContext = new FontRenderContext(null, false, false);

        List<Point> points = new ArrayList<>(Arrays.asList(getFromPoint(), getToPoint()));

        if (elbowPoints.size() > 0) {
            points.addAll(1, elbowPoints);
        }

        g.setColor(black);

        int gap = 25;

        Point outTextPoint = getOutTextPoint(points, gap);
        Point inTextPoint = getInTextPoint(points, gap);

        Set<InteractionType> outwardTypes = interactions.stream().map(Interaction::getType).filter(InteractionType::isOutward).collect(Collectors.toSet());
        Set<InteractionType> inwardTypes = interactions.stream().map(Interaction::getType).filter(interactionType -> !interactionType.isOutward()).collect(Collectors.toSet());
        int index = 0;

        for (InteractionType type : outwardTypes) {
            Rectangle2D bounds = font.getStringBounds(type.name(), fontRenderContext);
            int width = (int) bounds.getWidth();
            int height = (int) bounds.getHeight();
            g.setColor(white);
            g.fillRect(outTextPoint.x + 2, outTextPoint.y - height + 3 + (index * (height + 2)) + (outUp(points) ? height : -2), width, height);
            g.setColor(black);
            g.drawString(type.name(), outTextPoint.x + 2, outTextPoint.y + (index++ * (height + 2)) + (outUp(points) ? height : -2));
        }

        index = 0;

        for (InteractionType type : inwardTypes) {
            Rectangle2D bounds = font.getStringBounds(type.name(), fontRenderContext);
            int height = (int) bounds.getHeight();
            int width = (int) bounds.getWidth();
            g.setColor(white);
            g.fillRect(inTextPoint.x - width - 2, inTextPoint.y - height + 3 + (index * (height + 2)) + (inUp(points) ? height : -2), width, height);
            g.setColor(black);
            g.drawString(type.name(), inTextPoint.x - width - 2, inTextPoint.y + (index++ * (height + 2)) + (inUp(points) ? height : -2));
        }
    }

    void paintLines(Graphics g, boolean showElbows, boolean showPossibleElbows, Color selectedColor) {
        List<Point> points = new ArrayList<>(Arrays.asList(getFromPoint(), getToPoint()));

        if (elbowPoints.size() > 0) {
            points.addAll(1, elbowPoints);
        }

        g.setColor(black);

        for (int i = 0; i < points.size(); i++) {
            if(showElbows || !elbowPoints.contains(points.get(i))) {
                fillOval(g, points.get(i));
            }

            if (i < points.size() - 1) {
                drawLine(g, points.get(i), points.get(i + 1));

                if (showPossibleElbows) {
                    drawOval(g, getHalfwayPoint(points.get(i), points.get(i + 1)));
                }
            }
        }

        g.setColor(selectedColor);

        if (elbowPoints.size() > 0 && elbowSelected != -1) {
            fillOval(g, elbowPoints.get(elbowSelected));
        }
    }

    boolean outUp(List<Point> points) {
        return up(points.get(0), points.get(1));
    }

    boolean inUp(List<Point> points) {
        return up(points.get(points.size() - 1), points.get(points.size() - 2));
    }

    private boolean up(Point point1, Point point2) {
        return point2.y < point1.y;
    }

    private Point getOutTextPoint(List<Point> points, int gap) {
        Point point0 = points.get(0);
        Point point1 = points.get(1);
        int x0 = point0.x;
        int x1 = point1.x;
        int xDiff = x1 - x0;
        int y0 = point0.y;
        int y1 = point1.y;
        int yDiff = y1 - y0;

        double angle = Math.atan((double) yDiff / (double) xDiff);
        double opposite = (double) gap * Math.sin(angle);
        double adjacent = (double) gap * Math.cos(angle);

        return new Point(x0 + (int) adjacent, y0 + (int) opposite);
    }

    private Point getInTextPoint(List<Point> points, int gap) {
        Point point0 = points.get(points.size() - 1);
        Point point1 = points.get(points.size() - 2);
        int x0 = point0.x;
        int x1 = point1.x;
        int xDiff = x1 - x0;
        int y0 = point0.y;
        int y1 = point1.y;
        int yDiff = y1 - y0;

        double angle = Math.atan((double) yDiff / (double) xDiff);
        double opposite = (double) gap * Math.sin(angle);
        double adjacent = (double) gap * Math.cos(angle);

        return new Point(x0 - (int) adjacent, y0 - (int) opposite);
    }

    private void drawOval(Graphics g, Point point) {
        g.drawOval(point.x - CIRC_DRAW_RAD, point.y - CIRC_DRAW_RAD, CIRC_DRAW_RAD * 2, CIRC_DRAW_RAD * 2);
    }

    private void fillOval(Graphics g, Point point) {
        g.fillOval(point.x - CIRC_DRAW_RAD, point.y - CIRC_DRAW_RAD, CIRC_DRAW_RAD * 2, CIRC_DRAW_RAD * 2);
    }

    private void drawLine(Graphics g, Point from, Point to) {
        g.drawLine(from.x, from.y, to.x, to.y);
    }

    void setElbowSelected(int index) {
        this.elbowSelected = index;
    }

    void applyOffset(int x, int y) {
        elbowPoints.forEach(point -> point.translate(x, y));
    }

    static InteractionSprite forInteraction(Interaction interaction) {
        InteractionKey key = new InteractionKey(interaction);
        InteractionSprite sprite = INTERACTION_LOOKUP.get(key);

        if (sprite == null) {
            sprite = new InteractionSprite(new ArrayList<>(singletonList(interaction)), new ArrayList<>());
            INTERACTION_LOOKUP.put(key, sprite);
        } else {
            if(!sprite.getInteractions().contains(interaction)) {
                sprite.interactions.add(interaction);
            }
        }

        return sprite;
    }

    static List<InteractionSprite> getInteractionSprites() {
        return Interaction.getInteractions().stream().map(InteractionSprite::forInteraction).distinct().collect(Collectors.toList());
    }

    public static void load(List<InteractionSprite> interactionSprites) {
        INTERACTION_LOOKUP.clear();
        interactionSprites.forEach(sprite -> INTERACTION_LOOKUP.put(new InteractionKey(sprite.getInteractions().get(0)), sprite));
    }
}
