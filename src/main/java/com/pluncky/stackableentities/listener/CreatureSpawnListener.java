package com.pluncky.stackableentities.listener;

import com.pluncky.stackableentities.StackableEntitiesPlugin;
import com.pluncky.stackableentities.cache.EntityStackCache;
import com.pluncky.stackableentities.model.EntityStack;
import com.pluncky.stackableentities.util.CreatureUtils;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.UUID;

@RequiredArgsConstructor
public class CreatureSpawnListener implements Listener {
    private final StackableEntitiesPlugin plugin;

    @EventHandler(priority = EventPriority.HIGH)
    private void onCreatureSpawn(CreatureSpawnEvent event) {
        final Entity entity = event.getEntity();

        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) {
            return;
        }

        final EntityStackCache cache = plugin.getEntityStackCache();
        final FileConfiguration config = plugin.getConfig();

        final int radius = Math.min(config.getInt("creatures.stacking-radius"), 16);
        // Possibly make the max stack size configurable according to SpawnReason -> Spawner upgradable to increase max stack size
        final long maxEntityStackSize = Math.min(config.getLong("creatures.max-stack-size"), Integer.MAX_VALUE);

        final EntityStack[] nearbyIncompleteStacks = entity.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof Creature)
                .filter(e -> e.getType() == entity.getType())
                .map(Entity::getUniqueId)
                .map(cache::get)
                .filter(stack -> stack != null && (stack.getAmount() > 0 && stack.getAmount() < maxEntityStackSize))
                .toArray(EntityStack[]::new);

        final boolean isThereNearbyStack = nearbyIncompleteStacks.length > 0;
        final UUID entityID = entity.getUniqueId();

        if (!isThereNearbyStack) {
            cache.add(entityID, new EntityStack(cache, entity, 1));
            return;
        }

        final EntityStack stack = nearbyIncompleteStacks[0];

        if (stack == null) {
            return;
        }

        final Entity baseEntity = stack.getBaseEntity();
        if (baseEntity == null) {
            return;
        }

        if (!(baseEntity instanceof Creature creature)) {
            return;
        }

        CreatureUtils.normalizeCreature(creature);

        stack.addAmount(cache, 1);
        event.setCancelled(true);
    }
}
