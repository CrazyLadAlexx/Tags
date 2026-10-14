package me.alex.tags.config;

import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;
import java.nio.file.Path;

public record PluginSettings(TagCatalog catalog, ChatSettings chat) {
    public static PluginSettings load(Path directory) throws IOException, InvalidConfigurationException {
        return new PluginSettings(TagCatalog.load(directory.resolve("tag.yml")),
                ChatSettings.load(directory.resolve("config.yml")));
    }
}
