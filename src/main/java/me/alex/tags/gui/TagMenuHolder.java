package me.alex.tags.gui;

import me.alex.tags.model.Tag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;
import java.util.UUID;

public final class TagMenuHolder implements InventoryHolder {
    private final UUID owner;
    private final PageLayout layout;
    private final List<Tag> displayed;
    private Inventory inventory;

    public TagMenuHolder(UUID owner, PageLayout layout, List<Tag> displayed) {
        this.owner = owner;
        this.layout = layout;
        this.displayed = List.copyOf(displayed);
    }

    public UUID owner() {
        return owner;
    }

    public PageLayout layout() {
        return layout;
    }

    public Tag tagAt(int slot) {
        return slot >= 0 && slot < displayed.size() ? displayed.get(slot) : null;
    }

    public void attach(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
