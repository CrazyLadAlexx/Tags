package me.alex.tags.chat;

import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.ServicesManager;

public final class LuckPermsPrefixProvider {
    private LuckPermsPrefixProvider() { }

    public static PrefixProvider create(ServicesManager services) {
        var registration = services.getRegistration(LuckPerms.class);
        if (registration == null) {
            return playerId -> "";
        }
        var luckPerms = registration.getProvider();
        return playerId -> {
            var user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) {
                return "";
            }
            String prefix = user.getCachedData().getMetaData().getPrefix();
            return prefix == null ? "" : prefix.strip();
        };
    }
}
