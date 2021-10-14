package com.emarte.oojaba.data;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.emarte.oojaba.data.EndPointType.*;

public class Interaction {
    private static final List<Interaction> INTERACTIONS = new ArrayList<>();

    @JsonIgnore
    private final EndPoint actor;
    @JsonIgnore
    private final EndPoint destination;
    private final InteractionType interactionType;

    public Interaction(EndPoint actor, EndPoint destination, InteractionType interactionType) {
        this.actor = actor;
        this.destination = destination;
        this.interactionType = interactionType;

        if(!interactionType.getCompatibleEndPointTypes().contains(destination.getType())) {
            throw new IllegalArgumentException("Incompatible interaction and endpoint type"); //TODO better error message
        }

        INTERACTIONS.add(this);
    }

    public EndPoint getActor() {
        return actor;
    }

    @JsonGetter("actor")
    public UUID getActorId() {
        return actor.getId();
    }

    public EndPoint getDestination() {
        return destination;
    }

    @JsonGetter("destination")
    public UUID getDestinationId() {
        return destination.getId();
    }

    public InteractionType getType() {
        return interactionType;
    }

    public boolean involves(Entity entity) {
        return actor.getEntity() == entity || destination.getEntity() == entity;
    }

    public boolean involves(EndPoint endPoint) {
        return actor == endPoint || destination == endPoint;
    }

    public enum InteractionType {
        HTTP_CALL(true, RESTFUL),
        CONSUME_MESSAGE(false, QUEUE, TOPIC),
        PRODUCE_MESSAGE(true, QUEUE, TOPIC),
        REMOTE_DISK_WRITE(true, REMOTE_STORAGE),
        REMOTE_DISK_READ(false, REMOTE_STORAGE);

        @JsonIgnore
        private final List<EndPointType> compatibleEndPointTypes;
        @JsonIgnore
        private final boolean outward;

        InteractionType(boolean outward, EndPointType... compatibleEndPointTypes) {
            this.outward = outward;
            this.compatibleEndPointTypes = Arrays.asList(compatibleEndPointTypes);
        }

        public List<EndPointType> getCompatibleEndPointTypes() {
            return compatibleEndPointTypes;
        }

        public boolean isOutward() {
            return outward;
        }

        public static List<InteractionType> compatibleWith(EndPointType endPointType) {
            List<InteractionType> result = new ArrayList<>();

            for(InteractionType interactionType: values()) {
                if(interactionType.compatibleEndPointTypes.contains(endPointType)) {
                    result.add(interactionType);
                }
            }

            return result;
        }
    }

    public static List<Interaction> getInteractions() {
        return INTERACTIONS;
    }

    public static Interaction existing(EndPoint actor, EndPoint destination, InteractionType interactionType) {
        for(Interaction interaction: getInteractions()) {
            if(interaction.actor == actor && interaction.destination == destination && interaction.interactionType == interactionType) {
                return interaction;
            }
        }

        return null;
    }
}
