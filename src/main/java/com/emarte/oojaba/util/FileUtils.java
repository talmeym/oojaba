package com.emarte.oojaba.util;

import com.emarte.oojaba.data.EndPoint;
import com.emarte.oojaba.data.EndPointType;
import com.emarte.oojaba.data.Entity;
import com.emarte.oojaba.data.Interaction;
import com.emarte.oojaba.data.Interaction.InteractionType;
import com.emarte.oojaba.gui.CanvasPanel;
import com.emarte.oojaba.gui.EntitySprite;
import com.emarte.oojaba.gui.InteractionSprite;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class FileUtils {
    public static final String FILENAME_READ = "entities.json";
    public static final String FILENAME_WRITE = "entities.json";
    public static final String ENTITY_SPRITES = "entitySprites";
    public static final String ENTITY = "entity";
    public static final String INTERACTION_SPRITES = "interactionSprites";
    public static final String INTERACTIONS = "interactions";
    public static final String POSITION = "position";
    public static final String ELBOW_POINTS = "elbowPoints";
    public static final String ATTRIBUTES = "attributes";
    public static final String TYPE = "type";
    public static final String END_POINTS = "endPoints";
    public static final String ID = "id";
    public static final String ACTOR = "actor";
    public static final String DESTINATION = "destination";
    public static final String X = "x";
    public static final String Y = "y";
    private static final String FONT_SIZE = "fontSize";
    private static final String ENTITY_BORDER = "entityBorder";
    private static final String ENDPOINT_BORDER = "endPointBorder";
    private static final String COLOR_RGB = "colorRgb";
    private static final String SELECTED_COLOR_RGB = "selectedColorRgb";

    public static void save(CanvasPanel canvasPanel) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<EntitySprite> entitySprites = EntitySprite.getEntitySprites();
            HashMap<Object, Object> map = new HashMap<>();
            map.put(ENTITY_SPRITES, entitySprites);
            map.put(FONT_SIZE, canvasPanel.getFontSize());
            map.put(ENTITY_BORDER, EntitySprite.ENTITY_BORDER_SIZE);
            map.put(ENDPOINT_BORDER, EntitySprite.ENDPOINT_BORDER_SIZE);
            map.put(SELECTED_COLOR_RGB, canvasPanel.getSelectedColor().getRGB());
            mapper.writeValue(new FileOutputStream(FILENAME_WRITE), map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(CanvasPanel canvasPanel) {
        Entity.getEntities().clear();
        Interaction.getInteractions().clear();
        Set<Point> allPointInstances = new HashSet<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map map = objectMapper.readValue(new FileInputStream(FILENAME_READ), Map.class);

            canvasPanel.setFontSize((int) map.get(FONT_SIZE));
            EntitySprite.ENTITY_BORDER_SIZE = (int) map.get(ENTITY_BORDER);
            EntitySprite.ENDPOINT_BORDER_SIZE = (int) map.get(ENDPOINT_BORDER);
            Object selectedColorRgbObj = map.get(SELECTED_COLOR_RGB);

            if(selectedColorRgbObj != null) {
                canvasPanel.setSelectedColor(new Color((int) selectedColorRgbObj));
            }

            List<Object> objects = (List<Object>) map.get(ENTITY_SPRITES);
            HashMap<UUID, EndPoint> allEndPointMap = new HashMap<>();
            List<Entity> allEntities = new ArrayList<>();
            List<Interaction> allInteractions = new ArrayList<>();
            List<EntitySprite> allEntitySprites = new ArrayList<>();
            List<InteractionSprite> allInteractionSprites = new ArrayList<>();

            for (Object object : objects) {
                Entity entity = loadEntity((Map) ((Map) object).get(ENTITY));
                entity.getEndPoints().forEach(endPoint -> allEndPointMap.put(endPoint.getId(), endPoint));
                allEntities.add(entity);
            }

            for (Object object : objects) {
                for (Object interactionSpriteObj : (List<Object>) ((Map) object).get(INTERACTION_SPRITES)) {
                    for(Object interactionObj: (List) ((Map) interactionSpriteObj).get(INTERACTIONS)) {
                        Interaction interaction = loadInteraction((Map) interactionObj, allEndPointMap);
                        EndPoint actor = interaction.getActor();
                        Entity entity = actor.getEntity();
                        List<Interaction> interactions = entity.getInteractions();
                        interactions.add(interaction);
                        allInteractions.add(interaction);
                    }
                }
            }

            int entityIndex = 0;
            int interactionIndex = 0;

            for (Object entitySpriteObj : objects) {
                Map entitySpriteMap = (Map) entitySpriteObj;
                Entity entity = allEntities.get(entityIndex++);
                Point position = getPoint((Map) entitySpriteMap.get(POSITION), allPointInstances);
                Integer colorRgb = (Integer) entitySpriteMap.get(COLOR_RGB);
                allEntitySprites.add(new EntitySprite(entity, position, colorRgb != null ? new Color(colorRgb) : null));

                for(Object interactionSpriteObj: (List) entitySpriteMap.get(INTERACTION_SPRITES)) {
                    Map interactionSpriteMap = (Map) interactionSpriteObj;
                    List<Interaction> interactions = new ArrayList<>();
                    List<Point> elbowPoints = new ArrayList<>();

                    for(Object interactionObj: (List) interactionSpriteMap.get(INTERACTIONS)) {
                        interactions.add(allInteractions.get(interactionIndex++));
                    }

                    for(Object elbowObj: (List) interactionSpriteMap.get(ELBOW_POINTS)) {
                        elbowPoints.add(getPoint((Map) elbowObj, allPointInstances));
                    }

                    allInteractionSprites.add(new InteractionSprite(interactions, elbowPoints));
                }
            }


            EntitySprite.load(allEntitySprites);
            InteractionSprite.load(allInteractionSprites);

            int maxX = allPointInstances.stream().max(Comparator.comparing(point -> point.x)).get().x;
            int maxY = allPointInstances.stream().max(Comparator.comparing(point -> point.y)).get().y;

            canvasPanel.setCanvasSize(maxX + 300, maxY + 300);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Entity loadEntity(Map entityMap) {
        Entity entity = new Entity((Map) entityMap.get(ATTRIBUTES), Entity.EntityType.valueOf((String) entityMap.get(TYPE)));

        for (Object object : (List) entityMap.get(END_POINTS)) {
            Map endPointMap = (Map) object;
            UUID id = UUID.fromString((String) endPointMap.get(ID));
            EndPointType type = EndPointType.valueOf((String) endPointMap.get(TYPE));
            entity.addEndPoint(id, (Map<String, String>) endPointMap.get(ATTRIBUTES), type);
        }

        return entity;
    }

    private static Interaction loadInteraction(Map interactionMap, Map<UUID, EndPoint> allEndPointsMap) {
        UUID actorId = UUID.fromString((String) interactionMap.get(ACTOR));
        UUID destinationId = UUID.fromString((String) interactionMap.get(DESTINATION));
        InteractionType type = InteractionType.valueOf((String) interactionMap.get(TYPE));
        return new Interaction(allEndPointsMap.get(actorId), allEndPointsMap.get(destinationId), type);
    }

    private static Point getPoint(Map positionMap, Set<Point> allPointInstances) {
        Point point = new Point(((Double) positionMap.get(X)).intValue(), ((Double) positionMap.get(Y)).intValue());
        allPointInstances.add(point);
        return point;
    }
}
