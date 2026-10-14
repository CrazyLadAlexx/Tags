package me.alex.tags.config;

import me.alex.tags.model.Tag;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TagCatalog {
    private final Map<String, Tag> tags;

    private TagCatalog(Map<String, Tag> tags) {
        this.tags = Collections.unmodifiableMap(new LinkedHashMap<>(tags));
    }

    public static TagCatalog load(Path path) throws IOException, InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.load(path.toFile());
        ConfigurationSection section = config.getConfigurationSection("tags");
        if (section == null) {
            throw new IllegalArgumentException("tag.yml must contain a 'tags' section.");
        }
        String defaultMaterial = config.getString("note.material", "PAPER");
        String defaultName = config.getString("note.name", "&3&lTag Note &7(Right-click)");
        List<String> defaultLore = config.getStringList("note.lore");
        Map<String, Tag> tags = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            if (!key.matches("[A-Za-z0-9_-]+")) {
                throw new IllegalArgumentException("Invalid tag ID: " + key);
            }
            String id = key.toLowerCase(Locale.ROOT);
            ConfigurationSection definition = section.getConfigurationSection(key);
            if (definition == null || tags.containsKey(id)) {
                throw new IllegalArgumentException("Invalid or duplicate tag: " + key);
            }
            String display = definition.getString("display");
            if (display == null || display.isBlank()) {
                throw new IllegalArgumentException("Tag " + key + " requires a display name.");
            }
            String materialName = definition.getString("note.material", defaultMaterial);
            Material material = Material.matchMaterial(materialName);
            if (material == null || !material.isItem() || material.isAir()) {
                throw new IllegalArgumentException("Invalid note material for " + key + ": " + materialName);
            }
            String name = definition.getString("note.name", defaultName);
            List<String> lore = definition.contains("note.lore")
                    ? definition.getStringList("note.lore") : defaultLore;
            tags.put(id, new Tag(id, display, material, name, lore));
        }
        return new TagCatalog(tags);
    }

    public Tag find(String id) {
        return id == null ? null : tags.get(id.toLowerCase(Locale.ROOT));
    }

    public List<Tag> all() {
        return List.copyOf(tags.values());
    }

    public List<String> ids() {
        return List.copyOf(tags.keySet());
    }
}
