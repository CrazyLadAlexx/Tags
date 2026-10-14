package me.alex.tags.listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.alex.tags.Tags;
import me.alex.tags.chat.TagChatFormatter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener {
    private final Tags plugin;

    public ChatListener(Tags plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        var settings = plugin.settings();
        if (!settings.chat().enabled()) {
            return;
        }
        String name = event.getPlayer().getName();
        String equipped = plugin.store().snapshot(event.getPlayer().getUniqueId()).equipped();
        var tag = settings.catalog().find(equipped);
        String prefix = settings.chat().luckPermsPrefix()
                ? plugin.prefixes().prefix(event.getPlayer().getUniqueId()) : "";
        event.renderer(ChatRenderer.viewerUnaware((source, displayName, message) ->
                TagChatFormatter.format(settings.chat(), name, prefix, tag, message)));
    }
}
