package me.alex.tags.config;

import me.alex.tags.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;

public record ChatSettings(boolean enabled, boolean luckPermsPrefix, Component format,
                           Component prefixFormat, Component tagFormat) {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Za-z_]+)\\}");

    public static ChatSettings load(Path path) throws IOException, InvalidConfigurationException {
        var config = new YamlConfiguration();
        config.load(path.toFile());
        for (String key : Set.of("chat.enabled", "chat.luckperms-prefix")) {
            if (config.contains(key) && !config.isBoolean(key)) {
                throw new IllegalArgumentException(key + " must be true or false.");
            }
        }
        return new ChatSettings(config.getBoolean("chat.enabled", true),
                config.getBoolean("chat.luckperms-prefix", true),
                template(config, "chat.format", "{prefix}{tag}&f{player}&8: &f{message}",
                        Set.of("prefix", "tag", "player", "message"), "message"),
                template(config, "chat.prefix-format", "{prefix} ", Set.of("prefix"), "prefix"),
                template(config, "chat.tag-format", "&8[{tag}&8] ", Set.of("tag"), "tag"));
    }

    private static Component template(YamlConfiguration config, String key, String fallback,
                                      Set<String> allowed, String required) {
        if (config.contains(key) && !config.isString(key)) {
            throw new IllegalArgumentException(key + " must be a string.");
        }
        String value = config.getString(key, fallback);
        if (value.isBlank() || !value.contains("{" + required + "}")) {
            throw new IllegalArgumentException(key + " must contain {" + required + "}.");
        }
        var matcher = PLACEHOLDER.matcher(value);
        while (matcher.find()) {
            if (!allowed.contains(matcher.group(1))) {
                throw new IllegalArgumentException("Unknown placeholder in " + key + ": " + matcher.group());
            }
        }
        return Text.color(value);
    }
}
