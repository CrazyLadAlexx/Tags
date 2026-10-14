package me.alex.tags.api;

import me.alex.tags.Tags;
import me.alex.tags.model.PlayerTags;
import me.alex.tags.model.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class TagsAPI {
    private final Tags plugin;

    public TagsAPI(Tags plugin) {
        this.plugin = plugin;
    }

    public static TagsAPI getInstance() {
        TagsAPI api = Bukkit.getServicesManager().load(TagsAPI.class);
        if (api == null) {
            throw new IllegalStateException("Tags is not enabled or its API is unavailable.");
        }
        return api;
    }

    public List<Tag> getTags() {
        return plugin.catalog().all();
    }

    public Optional<Tag> getTag(String tagId) {
        return Optional.ofNullable(plugin.catalog().find(tagId));
    }

    public PlayerTags getPlayerTags(UUID playerId) throws IOException {
        requireServerThread();
        return plugin.store().load(playerId);
    }

    public PlayerTags getCachedPlayerTags(UUID playerId) {
        return plugin.store().snapshot(playerId);
    }

    public List<Tag> getOwnedTags(UUID playerId) throws IOException {
        var profile = getPlayerTags(playerId);
        return getTags().stream().filter(tag -> profile.unlocked().contains(tag.id())).toList();
    }

    public Optional<Tag> getEquippedTag(UUID playerId) throws IOException {
        return getTag(getPlayerTags(playerId).equipped());
    }

    public boolean hasTag(UUID playerId, String tagId) throws IOException {
        return getPlayerTags(playerId).unlocked().contains(normalize(tagId));
    }

    public boolean grantTag(UUID playerId, String tagId) throws IOException {
        Tag tag = requireTag(tagId);
        var profile = getPlayerTags(playerId);
        if (profile.unlocked().contains(tag.id())) {
            return false;
        }
        save(playerId, profile.unlock(tag.id()));
        return true;
    }

    public boolean revokeTag(UUID playerId, String tagId) throws IOException {
        var profile = getPlayerTags(playerId);
        String id = normalize(tagId);
        if (!profile.unlocked().contains(id)) {
            return false;
        }
        var unlocked = new HashSet<>(profile.unlocked());
        unlocked.remove(id);
        save(playerId, new PlayerTags(unlocked, id.equals(profile.equipped()) ? null : profile.equipped()));
        return true;
    }

    public boolean equipTag(UUID playerId, String tagId) throws IOException {
        Tag tag = requireTag(tagId);
        var profile = getPlayerTags(playerId);
        if (!profile.unlocked().contains(tag.id())) {
            throw new IllegalArgumentException("Player does not own tag: " + tag.id());
        }
        if (tag.id().equals(profile.equipped())) {
            return false;
        }
        save(playerId, new PlayerTags(profile.unlocked(), tag.id()));
        return true;
    }

    public boolean clearEquippedTag(UUID playerId) throws IOException {
        var profile = getPlayerTags(playerId);
        if (profile.equipped() == null) {
            return false;
        }
        save(playerId, new PlayerTags(profile.unlocked(), null));
        return true;
    }

    public ItemStack createTagNote(String tagId) {
        requireServerThread();
        return plugin.notes().create(requireTag(tagId));
    }

    public void openMenu(Player player) {
        requireServerThread();
        plugin.menu().open(player, 0);
    }

    private void save(UUID playerId, PlayerTags profile) throws IOException {
        plugin.store().save(playerId, profile);
        plugin.refreshMenu(playerId);
    }

    private Tag requireTag(String tagId) {
        return getTag(tagId).orElseThrow(() -> new IllegalArgumentException("Unknown tag: " + tagId));
    }

    private static String normalize(String tagId) {
        return tagId.toLowerCase(Locale.ROOT);
    }

    private void requireServerThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("This Tags API operation requires the server thread.");
        }
    }
}
