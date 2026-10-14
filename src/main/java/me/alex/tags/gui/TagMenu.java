package me.alex.tags.gui;

import me.alex.tags.Tags;
import me.alex.tags.model.Tag;
import me.alex.tags.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

public final class TagMenu {
    private final Tags plugin;

    public TagMenu(Tags plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, int requestedPage) {
        try {
            var profile = plugin.store().load(player.getUniqueId());
            List<Tag> owned = plugin.catalog().all().stream()
                    .filter(tag -> profile.unlocked().contains(tag.id())).toList();
            PageLayout layout = PageLayout.of(owned.size(), requestedPage);
            List<Tag> displayed = owned.subList(layout.start(), layout.end());
            var holder = new TagMenuHolder(player.getUniqueId(), layout, displayed);
            var inventory = Bukkit.createInventory(holder, PageLayout.SIZE, Text.color("&8Tags"));
            holder.attach(inventory);
            for (int slot = 0; slot < displayed.size(); slot++) {
                Tag tag = displayed.get(slot);
                boolean equipped = tag.id().equals(profile.equipped());
                String label = "&8[" + tag.display() + "&8]";
                List<String> lore = equipped
                        ? List.of("", "&aEquipped!", "", "&cClick to un-equip!")
                        : List.of("", "&aClick to toggle on", "", "&7Hint: Left click me");
                inventory.setItem(slot, item(Material.NAME_TAG, Text.color(label),
                        lore.stream().map(Text::color).toList()));
            }
            ItemStack pane = item(Material.BLACK_STAINED_GLASS_PANE, Text.color(" "), List.of());
            for (int slot = PageLayout.TAGS_PER_PAGE; slot < PageLayout.SIZE; slot++) {
                inventory.setItem(slot, pane);
            }
            if (layout.hasPrevious()) {
                inventory.setItem(PageLayout.PREVIOUS_SLOT,
                        item(Material.ARROW, Text.color("&a&lPage " + layout.page()), List.of()));
            }
            if (layout.hasNext()) {
                inventory.setItem(PageLayout.NEXT_SLOT,
                        item(Material.ARROW, Text.color("&a&lPage " + (layout.page() + 2)), List.of()));
            }
            player.openInventory(inventory);
        } catch (IOException exception) {
            plugin.reportDataError(player, exception);
        }
    }

    private ItemStack item(Material material, Component name, List<Component> lore) {
        var item = new ItemStack(material);
        var meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
