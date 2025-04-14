package ru.rexlite.providers;

import cn.nukkit.Player;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public class LProvider implements PrefixSuffixProvider {

    private final LuckPerms luckPerms;

    public LProvider() {
        this.luckPerms = LuckPermsProvider.get();
    }

    @Override
    public String getPrefix(Player player) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null && user.getCachedData().getMetaData().getPrefix() != null
                ? user.getCachedData().getMetaData().getPrefix()
                : "";
    }

    @Override
    public String getSuffix(Player player) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null && user.getCachedData().getMetaData().getSuffix() != null
                ? user.getCachedData().getMetaData().getSuffix()
                : "";
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
}
