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

import java.util.HashMap;
import java.util.Map;

public class NickManager {
    private final EssentialsChat plugin;
    private final Map<String, String> playerNicks = new HashMap<>();

    public NickManager(EssentialsChat plugin) {
        this.plugin = plugin;
    }

    public void setPlayerNick(Player player, String nick) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] Setting nick for player: " + player.getName() + ", nick: " + nick);
        }
        playerNicks.put(player.getName(), nick);
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] Successfully set nick for player: " + player.getName() + ", nick: " + nick);
        }
    }

    public void clearPlayerNick(Player player) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] Clearing nick for player: " + player.getName());
        }
        playerNicks.remove(player.getName());
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] Successfully cleared nick for player: " + player.getName());
        }
    }

    public String getRealName(String fakeNick) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] Looking up real name for nick: " + fakeNick);
        }
        for (Map.Entry<String, String> entry : playerNicks.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(fakeNick)) {
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] Found real name: " + entry.getKey() + " for nick: " + fakeNick);
                }
                return entry.getKey();
            }
        }
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] No real name found for nick: " + fakeNick);
        }
        return null;
    }

    public String getPlayerNick(Player player) {
        String nick = playerNicks.getOrDefault(player.getName(), null);
        // not need this debug now
        //if (plugin.isDebugEnabled()) {
        //    plugin.getLogger().info("§b[DEBUG] Retrieved nick for player: " + player.getName() + ", nick: " + (nick != null ? nick : "none"));
        //}
        return nick;
    }

    public boolean isUsed(String nick) {
        boolean used = playerNicks.containsValue(nick);
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] Checking if nick is used: " + nick + ", result: " + used);
        }
        return used;
    }

    public Map<String, String> getAllNicks() {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] Retrieving all nicks: " + playerNicks);
        }
        return playerNicks;
    }
}