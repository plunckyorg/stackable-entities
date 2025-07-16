package com.pluncky.stackableentities;

import com.pluncky.stackableentities.cache.EntityStackCache;
import com.pluncky.stackableentities.listener.*;
import com.pluncky.stackableentities.task.EntityMergeTask;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class StackableEntitiesPlugin extends JavaPlugin {
    private final EntityStackCache entityStackCache = new EntityStackCache();

    @Override
    public void onEnable() {
        getLogger().info("Stackable Entities plugin loaded!");
        this.init();
    }

    @Override
    public void onDisable() {
        getLogger().info("Stackable Entities plugin unloaded!");
    }

    private void init() {
        saveDefaultConfig();
        this.registerListeners(
                new ItemSpawnListener(this),
                new ItemMergeListener(),
                new ItemPickupListener(this.entityStackCache),
                new ItemDespawnListener(this.entityStackCache),
                new ChunkUnloadListener(this.entityStackCache),
                new CreatureSpawnListener(this),
                new EntityDeathListener(this.entityStackCache)
        );

        this.startTasks();
    }

    private void registerListeners(Listener... listeners) {
        PluginManager pluginManager = getServer().getPluginManager();

        for (Listener listener : listeners) {
            pluginManager.registerEvents(listener, this);
        }
    }

    private void startTasks() {
        new EntityMergeTask(this).runTaskTimer(this, 20L, 20L);
    }
}
