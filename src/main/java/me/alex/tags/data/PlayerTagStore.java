package me.alex.tags.data;

import me.alex.tags.model.PlayerTags;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PlayerTagStore {
    private final Path directory;
    private final ConcurrentMap<UUID, PlayerTags> players = new ConcurrentHashMap<>();

    public PlayerTagStore(Path directory) throws IOException {
        this.directory = directory;
        Files.createDirectories(directory);
    }

    public synchronized PlayerTags load(UUID playerId) throws IOException {
        PlayerTags cached = players.get(playerId);
        if (cached != null) {
            return cached;
        }
        Path file = directory.resolve(playerId + ".yml");
        PlayerTags loaded = PlayerTags.EMPTY;
        if (Files.exists(file)) {
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.load(file.toFile());
                var unlocked = new HashSet<>(config.getStringList("unlocked"));
                String equipped = config.getString("equipped");
                loaded = new PlayerTags(unlocked, unlocked.contains(equipped) ? equipped : null);
            } catch (InvalidConfigurationException exception) {
                throw new IOException("Cannot read player tags: " + file, exception);
            }
        }
        players.put(playerId, loaded);
        return loaded;
    }

    public PlayerTags snapshot(UUID playerId) {
        return players.getOrDefault(playerId, PlayerTags.EMPTY);
    }

    public synchronized void save(UUID playerId, PlayerTags profile) throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        config.set("unlocked", profile.unlocked().stream().sorted().toList());
        config.set("equipped", profile.equipped());
        Path destination = directory.resolve(playerId + ".yml");
        Path temporary = Files.createTempFile(directory, playerId + "-", ".tmp");
        try {
            Files.writeString(temporary, config.saveToString(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            players.put(playerId, profile);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public void unload(UUID playerId) {
        players.remove(playerId);
    }
}
