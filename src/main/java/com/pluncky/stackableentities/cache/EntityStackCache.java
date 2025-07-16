package com.pluncky.stackableentities.cache;

import com.pluncky.stackableentities.model.EntityStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityStackCache {
    private final Map<UUID, EntityStack> entityStacks = new HashMap<>();

    public void add(UUID entityID, EntityStack entityStack) {
        entityStacks.putIfAbsent(entityID, entityStack);
    }

    public void remove(UUID entityID) {
        if (!this.has(entityID)) return;

        entityStacks.remove(entityID);
    }

    public EntityStack get(UUID entityID) {
        return entityStacks.get(entityID);
    }

    public boolean has(UUID entityID) {
        return entityStacks.containsKey(entityID);
    }

    public void clear() {
        entityStacks.clear();
    }

    public Collection<EntityStack> getAllEntityStacks() {
        return this.entityStacks.values();
    }
}
