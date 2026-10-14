package me.alex.tags;

import me.alex.tags.api.TagsAPI;
import me.alex.tags.chat.LuckPermsPrefixProvider;
import me.alex.tags.chat.PrefixProvider;
import me.alex.tags.command.TagsCommand;
import me.alex.tags.config.PluginSettings;
import me.alex.tags.config.TagCatalog;
import me.alex.tags.data.PlayerTagStore;
import me.alex.tags.gui.TagMenu;
import me.alex.tags.gui.TagMenuHolder;
import me.alex.tags.item.TagNoteFactory;
import me.alex.tags.listener.ChatListener;
import me.alex.tags.listener.MenuListener;
import me.alex.tags.listener.NoteListener;
import me.alex.tags.listener.PlayerListener;
import me.alex.tags.text.Text;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class Tags extends JavaPlugin {
    private volatile PluginSettings settings;
    private PrefixProvider prefixes = playerId -> "";
    private PlayerTagStore store;
    private TagNoteFactory notes;
    private TagMenu menu;
    private TagsAPI api;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!tagFile().toFile().exists()) {
            saveResource("tag.yml", false);
        }
        try {
            settings = PluginSettings.load(getDataFolder().toPath());
            store = new PlayerTagStore(getDataFolder().toPath().resolve("players"));
            notes = new TagNoteFactory(this);
            menu = new TagMenu(this);
            api = new TagsAPI(this);
            var executor = new TagsCommand(this);
            var command = Objects.requireNonNull(getCommand("tags"), "tags command is missing from plugin.yml");
            command.setExecutor(executor);
            command.setTabCompleter(executor);
            var manager = getServer().getPluginManager();
            if (manager.isPluginEnabled("LuckPerms")) {
                prefixes = LuckPermsPrefixProvider.create(getServer().getServicesManager());
            }
            manager.registerEvents(new PlayerListener(this), this);
            manager.registerEvents(new NoteListener(this), this);
            manager.registerEvents(new MenuListener(this), this);
            manager.registerEvents(new ChatListener(this), this);
            for (Player player : getServer().getOnlinePlayers()) {
                try {
                    store.load(player.getUniqueId());
                } catch (IOException exception) {
                    reportDataError(player, exception);
                }
            }
            getServer().getServicesManager().register(TagsAPI.class, api, this, ServicePriority.Normal);
            getLogger().info("Tags by Alex enabled with " + catalog().ids().size() + " configured tags.");
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException exception) {
            getLogger().log(Level.SEVERE, "Cannot enable Tags. Check config.yml, tag.yml and the data directory.", exception);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
        api = null;
        closeMenus();
    }

    public boolean reloadSettings() {
        try {
            PluginSettings updated = PluginSettings.load(getDataFolder().toPath());
            settings = updated;
            reloadConfig();
            closeMenus();
            return true;
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException exception) {
            getLogger().log(Level.SEVERE, "Could not reload config.yml or tag.yml; keeping previous settings.", exception);
            return false;
        }
    }

    public boolean reloadCatalog() {
        return reloadSettings();
    }

    public void refreshMenu(UUID playerId) {
        Player player = getServer().getPlayer(playerId);
        if (player == null) {
            return;
        }
        var inventory = player.getOpenInventory().getTopInventory();
        if (inventory == null || !(inventory.getHolder() instanceof TagMenuHolder holder)) {
            return;
        }
        getServer().getScheduler().runTask(this, () -> {
            var current = player.getOpenInventory().getTopInventory();
            if (player.isOnline() && current != null && current.getHolder() == holder) {
                menu.open(player, holder.layout().page());
            }
        });
    }

    private void closeMenus() {
        for (Player player : getServer().getOnlinePlayers()) {
            var inventory = player.getOpenInventory().getTopInventory();
            if (inventory != null && inventory.getHolder() instanceof TagMenuHolder) {
                player.closeInventory();
            }
        }
    }

    public void reportDataError(Player player, IOException exception) {
        player.sendMessage(Text.color("&cYour tag data could not be saved or loaded. Please contact an administrator."));
        getLogger().log(Level.SEVERE, "Tag data error for " + player.getUniqueId(), exception);
    }

    private Path tagFile() {
        return getDataFolder().toPath().resolve("tag.yml");
    }

    public TagCatalog catalog() {
        return settings.catalog();
    }

    public PluginSettings settings() {
        return settings;
    }

    public PrefixProvider prefixes() {
        return prefixes;
    }

    public PlayerTagStore store() {
        return store;
    }

    public TagNoteFactory notes() {
        return notes;
    }

    public TagMenu menu() {
        return menu;
    }

    public TagsAPI getAPI() {
        if (api == null) {
            throw new IllegalStateException("Tags is not enabled.");
        }
        return api;
    }
}
