package me.alex.tags.item;

import me.alex.tags.model.Tag;
import me.alex.tags.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class TagNoteFactory {
    private final NamespacedKey tagKey;

    public TagNoteFactory(Plugin plugin) {
        tagKey = new NamespacedKey(plugin, "tag_note");
    }

    public ItemStack create(Tag tag) {
        ItemStack item = new ItemStack(tag.noteMaterial());
        var meta = item.getItemMeta();
        meta.displayName(render(tag.noteName(), tag));
        meta.lore(tag.noteLore().stream().map(line -> render(line, tag)).toList());
        meta.getPersistentDataContainer().set(tagKey, PersistentDataType.STRING, tag.id());
        item.setItemMeta(meta);
        return item;
    }

    public String tagId(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(tagKey, PersistentDataType.STRING);
    }

    private Component render(String template, Tag tag) {
        Component display = Text.color(tag.display());
        return Text.noItalics(Text.color(template)
                .replaceText(builder -> builder.matchLiteral("{tag_bold}").replacement(Text.bold(display)))
                .replaceText(builder -> builder.matchLiteral("{tag}").replacement(display)));
    }
}
