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
import lombok.Getter;
import lombok.Setter;
import me.criex.essentialschat.EssentialsChat;
import me.criex.essentialschat.utils.ConfigUtils;
import me.criex.essentialschat.utils.Data;

import java.util.Map;

@Getter
@Setter
public class NickManager {
    private final EssentialsChat plugin;
    private final ConfigUtils configUtils;
    private final Data data;

    public NickManager(EssentialsChat plugin, ConfigUtils configUtils, Data data) {
        this.plugin = plugin;
        this.configUtils = configUtils;
        this.data = data;
    }

    public void setPlayerNick(Player player, String nick) {
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Setting nick for player: " + player.getName() + ", nick: " + (nick != null ? nick : "null"));
        }
        if (nick == null) {
            data.removeNick(player.getName());
        } else {
            data.setNick(player.getName(), nick);
        }
    }

    public void clearPlayerNick(Player player) {
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Clearing nick for player: " + player.getName());
        }
        data.removeNick(player.getName());
    }

    public String getRealName(String fakeNick) {
        if (fakeNick == null) {
            if (configUtils.isDebug()) {
                plugin.getLogger().info("§b[DEBUG] Fake nick is null, returning null");
            }
            return null;
        }
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Looking up real name for nick: " + fakeNick);
        }
        String realName = data.getRealName(fakeNick);
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Found real name: " + (realName != null ? realName : "none") + " for nick: " + fakeNick);
        }
        return realName;
    }

    public String getPlayerNick(Player player) {
        // not need this debug now
        //if (plugin.isDebugEnabled()) {
        //    plugin.getLogger().info("§b[DEBUG] Retrieved nick for player: " + player.getName() + ", nick: " + (nick != null ? nick : "none"));
        //}
        String nick = data.getNick(player.getName());
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Retrieved nick for player: " + player.getName() + ", nick: " + (nick != null ? nick : "none"));
        }
        return nick;
    }

    public boolean isUsed(String nick) {
        if (nick == null) {
            if (configUtils.isDebug()) {
                plugin.getLogger().info("§b[DEBUG] Nick to check is null, returning false");
            }
            return false;
        }
        boolean used = data.isNickUsed(nick);
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Checking if nick is used: " + nick + ", result: " + used);
        }
        return used;
    }

    public Map<String, String> getAllNicks() {
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Retrieving all nicks");
        }
        return data.getAllNicks();
    }
}