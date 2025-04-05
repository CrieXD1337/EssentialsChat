package ru.rexlite.providers;

import cn.nukkit.Player;

public interface PrefixSuffixProvider {
    String getPrefix(Player player);
    String getSuffix(Player player);
}