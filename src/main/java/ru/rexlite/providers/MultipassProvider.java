package ru.rexlite.providers;

import cn.nukkit.Player;
import ru.nukkit.multipass.Multipass;

public class MultipassProvider implements PrefixSuffixProvider {

    @Override
    public String getPrefix(Player player) {
        return Multipass.getPrefix(player);
    }

    @Override
    public String getSuffix(Player player) {
        return Multipass.getSuffix(player);
    }
}