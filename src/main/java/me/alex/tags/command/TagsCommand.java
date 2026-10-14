package me.alex.tags.command;

import me.alex.tags.Tags;
import me.alex.tags.text.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TagsCommand implements TabExecutor {
    private final Tags plugin;

    public TagsCommand(Tags plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                plugin.menu().open(player, 0);
            } else {
                sender.sendMessage(Text.color("&cPlayers can open /tags. Console can use /tags give or /tags reload."));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("tags.give")) {
                sender.sendMessage(Text.color("&cYou do not have permission to give tag notes."));
            } else if (args.length != 4) {
                sender.sendMessage(Text.color("&cUsage: /" + label + " give <player> <tag_name> <amount>"));
            } else {
                give(sender, args);
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("tags.reload")) {
                sender.sendMessage(Text.color("&cYou do not have permission to reload Tags."));
            } else if (args.length != 1) {
                sender.sendMessage(Text.color("&cUsage: /" + label + " reload"));
            } else if (plugin.reloadSettings()) {
                sender.sendMessage(Text.color("&aTags reloaded successfully."));
            } else {
                sender.sendMessage(Text.color("&cCould not reload config.yml or tag.yml. Check console. Previous settings were kept."));
            }
        } else {
            sender.sendMessage(Text.color("&cUsage: /" + label + " [give <player> <tag_name> <amount> | reload]"));
        }
        return true;
    }

    private void give(CommandSender sender, String[] args) {
        var target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Text.color("&cThat player is not online."));
            return;
        }
        var tag = plugin.catalog().find(args[2]);
        if (tag == null) {
            sender.sendMessage(Text.color("&cUnknown tag. Available: " + String.join(", ", plugin.catalog().ids())));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            sender.sendMessage(Text.color("&cAmount must be a positive whole number."));
            return;
        }
        ItemStack note = plugin.notes().create(tag);
        int stackSize = note.getMaxStackSize();
        int capacity = 0;
        for (ItemStack existing : target.getInventory().getStorageContents()) {
            if (existing == null || existing.getType().isAir()) {
                capacity += stackSize;
            } else if (existing.isSimilar(note)) {
                capacity += Math.max(0, stackSize - existing.getAmount());
            }
        }
        if (amount > capacity) {
            sender.sendMessage(Text.color("&c" + target.getName() + " does not have enough inventory space. No notes were given."));
            return;
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (int remaining = amount; remaining > 0; remaining -= stackSize) {
            ItemStack stack = note.clone();
            stack.setAmount(Math.min(stackSize, remaining));
            stacks.add(stack);
        }
        target.getInventory().addItem(stacks.toArray(ItemStack[]::new));
        sender.sendMessage(Text.color("&aGave " + amount + " " + tag.display() + " &atag note(s) to " + target.getName() + "."));
        target.sendMessage(Text.color("&aYou received " + amount + " " + tag.display() + " &atag note(s). Right click to redeem."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("tags.give")) {
                options.add("give");
            }
            if (sender.hasPermission("tags.reload")) {
                options.add("reload");
            }
        } else if (args[0].equalsIgnoreCase("give") && sender.hasPermission("tags.give")) {
            if (args.length == 2) {
                plugin.getServer().getOnlinePlayers().stream().filter(player ->
                        !(sender instanceof Player viewer) || viewer.canSee(player))
                        .map(Player::getName).forEach(options::add);
            } else if (args.length == 3) {
                options.addAll(plugin.catalog().ids());
            } else if (args.length == 4) {
                options.addAll(List.of("1", "16", "32", "64"));
            }
        }
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }
}
