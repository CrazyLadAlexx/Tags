package me.alex.tags.listener;

import me.alex.tags.Tags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

public final class PlayerListener implements Listener {
    private final Tags plugin;

    public PlayerListener(Tags plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            plugin.store().load(event.getPlayer().getUniqueId());
        } catch (IOException exception) {
            plugin.reportDataError(event.getPlayer(), exception);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.store().unload(event.getPlayer().getUniqueId());
    }
}
