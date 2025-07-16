package com.pluncky.stackableentities.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;

@RequiredArgsConstructor
public class ItemMergeListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    private void onItemMerge(ItemMergeEvent event) {
        event.setCancelled(true);
    }
}
