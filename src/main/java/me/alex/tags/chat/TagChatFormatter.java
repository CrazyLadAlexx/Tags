package me.alex.tags.chat;

import me.alex.tags.config.ChatSettings;
import me.alex.tags.model.Tag;
import me.alex.tags.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Pattern;

public final class TagChatFormatter {
    private static final Pattern PLACEHOLDERS = Pattern.compile("\\{(prefix|tag|player|message)\\}");

    private TagChatFormatter() { }

    public static Component format(ChatSettings settings, String playerName, String prefix,
                                   Tag tag, Component message) {
        Component prefixPart = prefix == null || prefix.isBlank() ? Component.empty()
                : settings.prefixFormat().replaceText(builder -> builder.matchLiteral("{prefix}")
                        .replacement(Text.color(prefix.strip())));
        Component tagPart = tag == null ? Component.empty()
                : settings.tagFormat().replaceText(builder -> builder.matchLiteral("{tag}")
                        .replacement(Text.color(tag.display())));
        Component plainMessage = Component.text(PlainTextComponentSerializer.plainText().serialize(message));
        return Text.noItalics(settings.format().replaceText(builder -> builder.match(PLACEHOLDERS)
                .replacement((match, component) -> switch (match.group(1)) {
                    case "prefix" -> prefixPart;
                    case "tag" -> tagPart;
                    case "player" -> Component.text(playerName);
                    case "message" -> plainMessage;
                    default -> component;
                })));
    }
}
