package me.alex.tags.model;

import java.util.HashSet;
import java.util.Set;

public record PlayerTags(Set<String> unlocked, String equipped) {
    public static final PlayerTags EMPTY = new PlayerTags(Set.of(), null);

    public PlayerTags {
        unlocked = Set.copyOf(unlocked);
        if (equipped != null && !unlocked.contains(equipped)) {
            throw new IllegalArgumentException("An equipped tag must be unlocked.");
        }
    }

    public PlayerTags unlock(String id) {
        Set<String> updated = new HashSet<>(unlocked);
        updated.add(id);
        return new PlayerTags(updated, equipped);
    }

    public PlayerTags toggle(String id) {
        if (!unlocked.contains(id)) {
            throw new IllegalArgumentException("Cannot equip a locked tag.");
        }
        return new PlayerTags(unlocked, id.equals(equipped) ? null : id);
    }
}
