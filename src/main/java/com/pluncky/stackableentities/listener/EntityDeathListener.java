package com.pluncky.stackableentities.listener;

import com.pluncky.stackableentities.cache.EntityStackCache;
import com.pluncky.stackableentities.model.EntityStack;
import com.pluncky.stackableentities.util.CreatureUtils;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

@RequiredArgsConstructor
public class EntityDeathListener implements Listener {
    private final EntityStackCache cache;

    @EventHandler(priority = EventPriority.HIGH)
    private void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        final Entity entity = event.getEntity();
        final UUID entityId = entity.getUniqueId();

        if (!cache.has(entityId)) {
            return;
        }

        final EntityStack stack = cache.get(entityId);

        if (stack.getAmount() <= 1) {
            stack.deleteStack(cache);
            return;
        }

        final Location location = entity.getLocation();
        final EntityType type = entity.getType();

        // We don't force kill the entity since we don't cancel this event
        cache.remove(entityId);

        final Entity newBaseEntity = entity.getWorld().spawnEntity(location, type);
        if (!(newBaseEntity instanceof Creature newCreature)) {
            return;
        }

        CreatureUtils.normalizeCreature(newCreature);

        final EntityStack newStack = new EntityStack(cache, newCreature, stack.getAmount() - 1);

        if (!cache.has(newCreature.getUniqueId())) {
            cache.add(newCreature.getUniqueId(), newStack);
        } else {
            cache.get(newCreature.getUniqueId()).setAmount(cache, newStack.getAmount());
        }
    }
}
