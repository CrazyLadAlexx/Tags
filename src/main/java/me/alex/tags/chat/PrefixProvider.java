package me.alex.tags.chat;

import java.util.UUID;

@FunctionalInterface
public interface PrefixProvider {
    String prefix(UUID playerId);
}
