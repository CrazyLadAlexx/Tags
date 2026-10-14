package me.alex.tags.listener;

import me.alex.tags.Tags;
import me.alex.tags.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NoteListener implements Listener {
    private final Tags plugin;
    private final Map<UUID, Integer> lastInteraction = new HashMap<>();

    public NoteListener(Tags plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        String tagId = plugin.notes().tagId(event.getItem());
        if (tagId == null || event.getHand() == null) {
            return;
        }
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        var player = event.getPlayer();
        int tick = Bukkit.getCurrentTick();
        if (Integer.valueOf(tick).equals(lastInteraction.put(player.getUniqueId(), tick))) {
            return;
        }
        var tag = plugin.catalog().find(tagId);
        if (tag == null) {
            player.sendMessage(Text.color("&cThis tag no longer exists. Your note has been kept."));
            return;
        }
        try {
            var profile = plugin.store().load(player.getUniqueId());
            if (profile.unlocked().contains(tag.id())) {
                player.sendMessage(Text.color("&cYou already own this tag. Your note has been kept."));
                return;
            }
            var inventory = player.getInventory();
            var held = event.getHand() == EquipmentSlot.HAND
                    ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
            if (!tag.id().equals(plugin.notes().tagId(held))) {
                return;
            }
            if (!plugin.getAPI().grantTag(player.getUniqueId(), tag.id())) {
                return;
            }
            var remaining = held.clone();
            remaining.setAmount(held.getAmount() - 1);
            if (event.getHand() == EquipmentSlot.HAND) {
                inventory.setItemInMainHand(remaining);
            } else {
                inventory.setItemInOffHand(remaining);
            }
            player.sendMessage(Text.color("&a&l(!) " + tag.display() + " &aSuccessfully redeemed!"));
        } catch (IOException exception) {
            plugin.reportDataError(player, exception);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastInteraction.remove(event.getPlayer().getUniqueId());
    }
}
