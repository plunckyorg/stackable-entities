package com.pluncky.stackableentities.listener;

import com.pluncky.stackableentities.StackableEntitiesPlugin;
import com.pluncky.stackableentities.cache.EntityStackCache;
import com.pluncky.stackableentities.model.EntityStack;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ItemSpawnListener implements Listener {
    private final StackableEntitiesPlugin plugin;

    @EventHandler(priority = EventPriority.HIGH)
    private void onItemSpawn(ItemSpawnEvent event) {
        final Item entity = event.getEntity();

        final int initialAmount = entity.getItemStack().getAmount();

        final EntityStackCache cache = plugin.getEntityStackCache();
        final FileConfiguration config = plugin.getConfig();

        final int configRadius = config.getInt("drops.stacking-radius", 8);
        final long configMaxStackSize = config.getLong("drops.max-stack-size", 64);

        final int radius = configRadius >= 1 ? Math.min(configRadius, 8) : 1;
        // TODO: The minimum stack size is currently 64 to avoid bugs, but the plugin is useless if the max stack size is less than 64.
        final long maxEntityStackSize = Math.max(configMaxStackSize, 64);

        final List<Entity> nearbyEntities = entity.getNearbyEntities(radius, radius, radius);

        final EntityStack[] nearbyStacks = nearbyEntities.stream()
                .filter(e -> e.getType() == EntityType.ITEM)
                .filter(i -> ((Item) i).getItemStack().isSimilar(entity.getItemStack()))
                .map(Entity::getUniqueId)
                .map(cache::get)
                .filter(stack -> stack != null && (stack.getAmount() > 0 && stack.getAmount() < maxEntityStackSize))
                .toArray(EntityStack[]::new);

        final boolean isThereNearbyStack = nearbyStacks.length > 0;

        final UUID entityID = entity.getUniqueId();

        if (!isThereNearbyStack) {
            entity.getItemStack().setAmount(1);
            cache.add(entityID, new EntityStack(cache, entity, initialAmount));
            return;
        }

        final EntityStack stack = nearbyStacks[0];
        if (stack == null) return;

        final Entity baseEntity = stack.getBaseEntity();

        if (baseEntity == null) return;

        final long stackAmount = stack.getAmount();
        if (stackAmount + initialAmount <= maxEntityStackSize) {
            stack.addAmount(cache, initialAmount);
        } else {
            final long amountToAdd = maxEntityStackSize - stackAmount;
            final long remainingAmount = initialAmount - amountToAdd;
            stack.addAmount(cache, amountToAdd);

            final Item newItem = (Item) baseEntity.getWorld().spawnEntity(baseEntity.getLocation(), EntityType.ITEM);
            final ItemStack newItemStack = ((Item) baseEntity).getItemStack().clone();

            newItem.setItemStack(newItemStack);

            EntityStack newStack = cache.get(newItem.getUniqueId());

            if (newStack == null) {
                cache.add(newItem.getUniqueId(), new EntityStack(cache, newItem, remainingAmount));
                newStack = cache.get(newItem.getUniqueId());
            }

            newStack.setAmount(cache, remainingAmount);
        }

        event.setCancelled(true);
    }
}
