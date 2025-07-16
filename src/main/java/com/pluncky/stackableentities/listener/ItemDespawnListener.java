package com.pluncky.stackableentities.listener;

import com.pluncky.stackableentities.cache.EntityStackCache;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class ItemDespawnListener implements Listener {
    private final EntityStackCache cache;

    @EventHandler(priority = EventPriority.MONITOR)
    private void onItemDespawn(org.bukkit.event.entity.ItemDespawnEvent event) {
        cache.remove(event.getEntity().getUniqueId());
    }
}
