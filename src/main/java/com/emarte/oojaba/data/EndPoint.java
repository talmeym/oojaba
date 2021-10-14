package com.emarte.oojaba.data;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;
import java.util.UUID;

import static com.emarte.oojaba.data.EndPoint.StandardAttributes.*;

public class EndPoint {
    private final UUID id;
    @JsonIgnore
    private final Entity entity;
    private final Map<String, String> attributes;
    private final EndPointType type;

    EndPoint(UUID id, Entity entity, String text, EndPointType type) {
        this(id, entity, type.deriveAttributes(text), type);
    }

    EndPoint(UUID id, Entity entity, Map<String, String> attributes, EndPointType type) {
        this.id = id;
        this.entity = entity;
        this.attributes = attributes;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public Entity getEntity() {
        return entity;
    }

    @JsonGetter("attributes")
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @JsonIgnore
    public String getDisplayText(boolean alternative) {
        StringBuilder builder = new StringBuilder();

        if(attributes.containsKey(METHOD.value)) {
            builder.append(attributes.get(METHOD.value)).append(" ");
        }

        if(alternative) {
            builder.append(attributes.get(attributes.containsKey(PATH.value) ? PATH.value : DESC.value));
        } else {
            builder.append(attributes.get(attributes.containsKey(DESC.value) ? DESC.value : PATH.value));
        }

        return builder.toString();
    }

    @JsonIgnore
    public String getAttributeText() {
        return type.deriveText(attributes);
    }

    public void applyAttributeText(String text) {
        this.attributes.clear();
        type.deriveAttributes(text).forEach(attributes::put);
    }

    public EndPointType getType() {
        return type;
    }

    public enum StandardAttributes {
        DESC("desc"),
        PATH("path"),
        METHOD("method");

        public final String value;

        StandardAttributes(String value) {
            this.value = value;
        }
    }
}
