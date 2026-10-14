package me.alex.tags.listener;

import me.alex.tags.Tags;
import me.alex.tags.gui.PageLayout;
import me.alex.tags.gui.TagMenuHolder;
import me.alex.tags.text.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.io.IOException;

public final class MenuListener implements Listener {
    private final Tags plugin;

    public MenuListener(Tags plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof TagMenuHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !holder.owner().equals(player.getUniqueId()) || event.getClick() != ClickType.LEFT
                || event.getRawSlot() < 0
                || event.getRawSlot() >= PageLayout.SIZE) {
            return;
        }
        int slot = event.getRawSlot();
        int page = holder.layout().page();
        if (slot == PageLayout.PREVIOUS_SLOT && holder.layout().hasPrevious()) {
            refresh(player, holder, page - 1);
        } else if (slot == PageLayout.NEXT_SLOT && holder.layout().hasNext()) {
            refresh(player, holder, page + 1);
        } else {
            var shown = holder.tagAt(slot);
            var tag = shown == null ? null : plugin.catalog().find(shown.id());
            if (tag == null) {
                return;
            }
            try {
                var profile = plugin.store().load(player.getUniqueId());
                if (!profile.unlocked().contains(tag.id())) {
                    return;
                }
                boolean unequip = tag.id().equals(profile.equipped());
                if (unequip) {
                    plugin.getAPI().clearEquippedTag(player.getUniqueId());
                } else {
                    plugin.getAPI().equipTag(player.getUniqueId(), tag.id());
                }
                String action = unequip
                        ? " &aSuccessfully &cun-equipped!" : " &aSuccessfully equipped!";
                player.sendMessage(Text.color("&a&l(!) " + tag.display() + action));
            } catch (IOException exception) {
                plugin.reportDataError(player, exception);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof TagMenuHolder) {
            event.setCancelled(true);
        }
    }

    private void refresh(Player player, TagMenuHolder holder, int page) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            var inventory = player.getOpenInventory().getTopInventory();
            if (player.isOnline() && inventory != null && inventory.getHolder() == holder) {
                plugin.menu().open(player, page);
            }
        });
    }
}
