/*
 * This file is part of EssentialsChat, licensed under the MIT License.
 *
 *  Copyright (c) Ivan [CrieXD1337] <criex1337@gmail.com>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package me.criex.essentialschat.managers;

import cn.nukkit.Player;
import me.criex.essentialschat.EssentialsChat;
import me.criex.essentialschat.providers.PrefixSuffixProvider;
import me.criex.essentialschat.utils.ConfigUtils;

public class DisplayManager {
    private final EssentialsChat plugin;
    private final PrefixSuffixProvider provider;
    private final ConfigUtils configUtils;

    public DisplayManager(EssentialsChat plugin, PrefixSuffixProvider provider, ConfigUtils configUtils) {
        this.plugin = plugin;
        this.provider = provider;
        this.configUtils = configUtils;
    }

    public void updateDisplay(Player player) {
        if (!configUtils.prefixInSettingsAndHeadEnabled) {
            if (configUtils.debug) {
                plugin.getLogger().info("§b[DEBUG] Prefix in settings and head is disabled, skipping display update for player: " + player.getName());
            }
            return;
        }

        //if (plugin.isDebugEnabled()) {
        //    plugin.getLogger().info("§b[DEBUG] Updating display for player: " + player.getName());
        //}

        String prefix = provider.getPrefix(player);
        String suffix = provider.getSuffix(player);
        String nick = plugin.getNickManager().getPlayerNick(player);
        String name = nick != null ? plugin.formatNick(nick) : player.getName();

        if (configUtils.debug) {
            plugin.getLogger().info("§b[DEBUG] Player: " + player.getName() + ", prefix: " + (prefix != null ? prefix : "none") + ", suffix: " + (suffix != null ? suffix : "none") + ", nick: " + (nick != null ? nick : "none") + ", formatted name: " + name);
        }

        String display = configUtils.prefixInSettingsAndHeadFormat
                .replace("{prefix}", prefix != null ? prefix : "")
                .replace("{player}", name)
                .replace("{suffix}", suffix != null ? suffix : "");
        display = plugin.parsePlaceholders(player, display);

        if (configUtils.debug) {
            plugin.getLogger().info("§b[DEBUG] Set display name and nametag for player: " + player.getName() + ", result: " + display);
        }

        player.setDisplayName(display);
        player.setNameTag(display);
    }
}