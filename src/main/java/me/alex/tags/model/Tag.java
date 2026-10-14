package me.alex.tags.model;

import org.bukkit.Material;

import java.util.List;

public record Tag(String id, String display, Material noteMaterial, String noteName, List<String> noteLore) {
    public Tag {
        noteLore = List.copyOf(noteLore);
    }
}
