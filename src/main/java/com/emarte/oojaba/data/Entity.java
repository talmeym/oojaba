package com.emarte.oojaba.data;

import com.emarte.oojaba.data.Interaction.InteractionType;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

import static com.emarte.oojaba.data.Entity.StandardAttributes.NAME;

public class Entity {
    private static final List<Entity> ENTITIES = new ArrayList<>();

    private final Map<String, String> attributes;
    private final EntityType type;
    private final List<EndPoint> endPoints = new ArrayList<EndPoint>();
    private final List<Interaction> interactions = new ArrayList<>();

    public Entity(String name, EntityType type) {
        this.attributes = new HashMap<>();
        attributes.put(NAME.value, name);
        this.type = type;
        ENTITIES.add(this);
    }

    public Entity(Map<String, String> attributes, EntityType type) {
        this.attributes = attributes;
        this.type = type;
        ENTITIES.add(this);
    }

    @JsonIgnore
    public String getName() {
        return attributes.get(NAME.value);
    }

    @JsonGetter("attributes")
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @JsonGetter("type")
    EntityType getType() {
        return type;
    }

    public List<EndPoint> getEndPoints() {
        return endPoints;
    }

    @JsonIgnore
    public List<Interaction> getInteractions() {
        return interactions;
    }

    public void setName(String name) {
        attributes.put(NAME.value, name);
    }

    public void addEndPoint(UUID id, String text, EndPointType type) {
        endPoints.add(new EndPoint(id, this, text, type));
    }

    public void addEndPoint(UUID id, Map<String, String> attributes, EndPointType type) {
        endPoints.add(new EndPoint(id, this, attributes, type));
    }

    public void addInteraction(EndPoint actor, EndPoint destination, InteractionType type) {
        if(actor.getEntity() != this) {
            throw new IllegalArgumentException("boom"); // TODO proper error message
        }

        if(destination.getEntity() == this) {
            throw new IllegalArgumentException("boom"); // TODO proper error message
        }

        interactions.add(new Interaction(actor, destination, type));
    }

    public enum StandardAttributes {
        NAME("name");

        private final String value;

        StandardAttributes(String value) {
            this.value = value;
        }
    }

    public enum EntityType {
        SERVICE
    }

    public static List<Entity> getEntities() {
        return ENTITIES;
    }
}
