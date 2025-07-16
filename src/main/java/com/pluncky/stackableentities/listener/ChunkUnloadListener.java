package com.pluncky.stackableentities.listener;

import com.pluncky.stackableentities.StackableEntitiesPlugin;
import com.pluncky.stackableentities.cache.EntityStackCache;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

@RequiredArgsConstructor
public class ChunkUnloadListener implements Listener {
    private final EntityStackCache cache;

    @EventHandler(priority = EventPriority.MONITOR)
    private void onChunkUnload(ChunkUnloadEvent event) {
        final Chunk chunk = event.getChunk();

        for (Entity entity : chunk.getEntities()) {
            cache.remove(entity.getUniqueId());
        }
    }
}
