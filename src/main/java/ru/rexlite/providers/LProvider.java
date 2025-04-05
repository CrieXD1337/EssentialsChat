package ru.rexlite.providers;

import cn.nukkit.Player;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public class LProvider implements PrefixSuffixProvider {

    @Override
    public String getPrefix(Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null && user.getCachedData().getMetaData().getPrefix() != null
                ? user.getCachedData().getMetaData().getPrefix()
                : "";
    }

    @Override
    public String getSuffix(Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null && user.getCachedData().getMetaData().getSuffix() != null
                ? user.getCachedData().getMetaData().getSuffix()
                : "";
    }
}