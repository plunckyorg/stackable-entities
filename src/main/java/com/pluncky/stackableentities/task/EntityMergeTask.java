package com.pluncky.stackableentities.task; // Use your actual package name

import com.pluncky.stackableentities.StackableEntitiesPlugin;
import com.pluncky.stackableentities.model.EntityStack;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;

public class EntityMergeTask extends BukkitRunnable {
    private final StackableEntitiesPlugin plugin;
    private final int dropsRadius, creaturesRadius;
    private final long dropsMaxStackSize, creaturesMaxStackSize;

    public EntityMergeTask(StackableEntitiesPlugin plugin) {
        this.plugin = plugin;
        this.dropsRadius = plugin.getConfig().getInt("drops.stacking-radius", 8);
        this.dropsMaxStackSize = plugin.getConfig().getLong("drops.max-stack-size", 2304);
        this.creaturesRadius = plugin.getConfig().getInt("creatures.stacking-radius", 8);
        this.creaturesMaxStackSize = plugin.getConfig().getLong("creatures.max-stack-size", Integer.MAX_VALUE);
    }

    @Override
    public void run() {
        // We copy the collection to avoid concurrent modification exceptions
        final Collection<EntityStack> stacks = new ArrayList<>(this.plugin.getEntityStackCache().getAllEntityStacks());

        for (EntityStack stack : stacks) {
            if (stack.getBaseEntity() == null) {
                continue;
            }

            int radius;
            long maxStackSize;
            if (stack.getBaseEntity().getType().equals(EntityType.ITEM)) {
                radius = this.dropsRadius;
                maxStackSize = this.dropsMaxStackSize;
            } else {
                radius = this.creaturesRadius;
                maxStackSize = this.creaturesMaxStackSize;
            }

            stack.merge(this.plugin.getEntityStackCache(), radius, maxStackSize);
        }
    }
}