package com.pluncky.stackableentities.listener;

import com.pluncky.bukkitutils.utils.inventory.InventoryUtils;
import com.pluncky.stackableentities.cache.EntityStackCache;
import com.pluncky.stackableentities.model.EntityStack;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

@RequiredArgsConstructor
public class ItemPickupListener implements Listener {
    private final EntityStackCache cache;

    @EventHandler(priority = EventPriority.HIGH)
    private void onEntityPickupItem(EntityPickupItemEvent event) {
        final Item item = event.getItem();
        final UUID itemUUID = item.getUniqueId();

        if (!cache.has(itemUUID)) {
            return;
        }

        final LivingEntity entity = event.getEntity();
        if (!(entity instanceof InventoryHolder inventoryHolder)) {
            return;
        }

        final Inventory inventory = inventoryHolder.getInventory();

        final EntityStack entityStack = cache.get(itemUUID);

        if (entityStack.getAmount() <= 0) {
            entityStack.deleteStack(cache);
            return;
        }

        final int amountAdded = this.fillInventory(inventory, entityStack);

        final long remainingAmount = entityStack.getAmount() - amountAdded;

        entityStack.setAmount(cache, remainingAmount);

        event.setCancelled(true);
    }

    private int fillInventory(Inventory inventory, EntityStack entityStack) {
        if (!(entityStack.getBaseEntity() instanceof Item item)) {
            return 0;
        }

        final ItemStack itemStack = item.getItemStack().clone();
        final int maxInventoryAmount = InventoryUtils.getMissingAmount(inventory, itemStack);

        final int amountToAdd = (int) Math.min(maxInventoryAmount, entityStack.getAmount());

        final ItemStack stack = itemStack.clone();
        stack.setAmount(amountToAdd);

        HashMap<Integer, ItemStack> remainingItems = inventory.addItem(stack);
        if (!remainingItems.isEmpty()) {
            // If there are remaining items, it is probably because of the off-hand slot
            final ItemStack offHandItem = inventory.getItem(40);
            if (offHandItem != null && offHandItem.isSimilar(stack)) {
                int offHandAmount = offHandItem.getAmount();
                int remainingAmount = remainingItems.get(0).getAmount();

                if (offHandAmount + remainingAmount <= offHandItem.getMaxStackSize()) {
                    offHandItem.setAmount(offHandAmount + remainingAmount);
                    inventory.setItem(40, offHandItem);
                }
            }
        }

        return amountToAdd;
    }
}
