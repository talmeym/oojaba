package com.emarte.oojaba.gui;

import com.emarte.oojaba.data.Entity;
import com.emarte.oojaba.data.Interaction;

import java.util.Objects;

public class InteractionKey { // TODO use this class
    private final Entity entity1;
    private final Entity entity2;
    private final boolean outward;

    public InteractionKey(Interaction interaction) {
        this(interaction.getActor().getEntity(), interaction.getDestination().getEntity(), interaction.getType().isOutward());
    }

    public InteractionKey(Entity entity1, Entity entity2, boolean outward) {
        this.entity1 = entity1;
        this.entity2 = entity2;
        this.outward = outward;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractionKey that = (InteractionKey) o;
        return this.entity1 == that.entity1 && this.entity2 == that.entity2 && this.outward == that.outward ||
                this.entity2 == that.entity1 && this.entity1 == that.entity2 && this.outward != that.outward;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity1, entity2) + Objects.hash(entity2, entity1);
    }
}
