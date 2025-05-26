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
package ru.rexlite.managers;

import cn.nukkit.Player;
import ru.rexlite.EssentialsChat;

import java.util.HashMap;
import java.util.Map;

public class NickManager {
    private final EssentialsChat plugin;
    private final Map<String, String> playerNicks = new HashMap<>();

    public NickManager(EssentialsChat plugin) {
        this.plugin = plugin;
    }

    public void setPlayerNick(Player player, String nick) {
        playerNicks.put(player.getName(), nick);
    }

    public void clearPlayerNick(Player player) {
        playerNicks.remove(player.getName());
    }

    public String getRealName(String fakeNick) {
        for (Map.Entry<String, String> entry : playerNicks.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(fakeNick)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getPlayerNick(Player player) {
        return playerNicks.getOrDefault(player.getName(), null);
    }

    public boolean isUsed(String nick) {
        return playerNicks.containsValue(nick);
    }

    public Map<String, String> getAllNicks() {
        return playerNicks;
    }
}
