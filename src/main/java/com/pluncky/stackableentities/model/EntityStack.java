package com.pluncky.stackableentities.model;

import com.pluncky.stackableentities.cache.EntityStackCache;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class EntityStack {
    private final UUID baseEntityID;
    private long amount;

    public EntityStack(EntityStackCache cache, Entity baseEntity, long initialAmount) {
        baseEntity.setCustomNameVisible(true);

        long amount = Math.max(1, initialAmount);

        this.baseEntityID = baseEntity.getUniqueId();
        this.setAmount(cache, amount);

        baseEntity.customName(this.getDisplayName(baseEntity));
    }

    public void setAmount(EntityStackCache cache, long amount) {
        if (amount <= 0) {
            this.deleteStack(cache);

            return;
        }

        this.amount = amount;
        this.updateName();
    }

    public void addAmount(EntityStackCache cache, long amount) {
        if (this.amount < 0) {
            this.removeAmount(cache, -amount);
            return;
        }

        this.amount += amount;
        this.updateName();
    }

    public void removeAmount(EntityStackCache cache, long amount) {
        if (this.amount <= amount) {
            this.deleteStack(cache);
            return;
        }

        if (amount < 0) {
            this.addAmount(cache, -amount);
            return;
        }

        this.amount -= amount;
        this.updateName();
    }

    public Component getDisplayName(Entity entity) {
        Component name = entity instanceof Item item
                ? Component.translatable(item.getItemStack().getType().translationKey())
                : Component.translatable(entity.getType().translationKey());

        name = name.color(NamedTextColor.WHITE);

        return Component.text()
                .append(name)
                .append(Component.text(" - ", NamedTextColor.GREEN))
                .append(Component.text(String.valueOf(this.amount), NamedTextColor.WHITE))
                .build();
    }

    public Entity getBaseEntity() {
        return Bukkit.getEntity(this.baseEntityID);
    }

    public boolean isItemStack() {
        return this.getBaseEntity() instanceof Item;
    }

    // TODO: prioritize stacks with more entities
    public void merge(EntityStackCache cache, int radius, long maxStackSize) {
        final Entity entity = this.getBaseEntity();
        if (entity == null || !entity.isValid()) {
            cache.remove(this.baseEntityID);
            return;
        }

        final List<Entity> nearbyEntities = entity.getNearbyEntities(radius, radius, radius);

        Stream<Entity> _nearbyStacks = nearbyEntities.stream()
                .filter(e -> e != null && e.isValid())
                .filter(e -> !e.getUniqueId().equals(this.baseEntityID))
                .filter(e -> e.getType() == entity.getType());

        if (this.isItemStack()) {
            final Item item = (Item) entity;
            _nearbyStacks = _nearbyStacks.filter(i -> ((Item) i).getItemStack().isSimilar(item.getItemStack()));
        }

        final Set<EntityStack> nearbyStacks = _nearbyStacks.map(Entity::getUniqueId)
                .map(cache::get)
                .filter(stack -> stack != null && (stack.getAmount() > 0 && stack.getAmount() < maxStackSize))
                .sorted((stack1, stack2) -> Long.compare(stack2.getAmount(), stack1.getAmount()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (nearbyStacks.isEmpty()) return;

        Iterator<EntityStack> nearbyStacksIterator = nearbyStacks.iterator();

        while (this.amount > 0 && nearbyStacksIterator.hasNext()) {
            EntityStack targetStack = nearbyStacksIterator.next();

            final long missingAmount = maxStackSize - targetStack.getAmount();
            final long amountToAdd = Math.min(missingAmount, this.amount);

            targetStack.addAmount(cache, amountToAdd);
            this.removeAmount(cache, amountToAdd);
        }

        if (this.amount <= 0) {
            this.deleteStack(cache);
        } else {
            this.updateName();
        }
    }

    public void deleteStack(EntityStackCache cache) {
        final Entity entity = this.getBaseEntity();
        if (entity == null || !entity.isValid()) return;

        entity.remove();

        cache.remove(this.baseEntityID);
    }

    public void updateName() {
        final Entity entity = this.getBaseEntity();
        if (entity == null || !entity.isValid()) return;

        entity.customName(this.getDisplayName(entity));
    }

    @Override
    public String toString() {
        return String.format("%s - %d", this.baseEntityID, this.amount);
    }
}
