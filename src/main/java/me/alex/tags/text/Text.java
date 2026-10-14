package me.alex.tags.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Text {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&').hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    private Text() { }

    public static Component color(String input) {
        return noItalics(LEGACY.deserialize(input.replace('\u00a7', '&')));
    }

    public static Component noItalics(Component input) {
        return input.decoration(TextDecoration.ITALIC, false)
                .children(input.children().stream().map(Text::noItalics).toList());
    }

    public static Component bold(Component input) {
        return input.decoration(TextDecoration.BOLD, true)
                .children(input.children().stream().map(Text::bold).toList());
    }
}
