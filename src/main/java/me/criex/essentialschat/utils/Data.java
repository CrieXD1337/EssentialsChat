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

package me.criex.essentialschat.utils;

import cn.nukkit.utils.Config;
import me.criex.essentialschat.EssentialsChat;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Data {
    private final EssentialsChat plugin;
    private final ConfigUtils configUtils;
    private final String dataProvider;
    private final File nicksFolder;

    public Data(EssentialsChat plugin, ConfigUtils configUtils) {
        this.plugin = plugin;
        this.configUtils = configUtils;
        this.dataProvider = configUtils.getDataProvider().toLowerCase();
        this.nicksFolder = new File(plugin.getDataFolder(), "data/nicks");
        initializeDataFiles();
    }

    private void initializeDataFiles() {
        if (!nicksFolder.exists()) {
            nicksFolder.mkdirs();
        }
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Initialized nicks directory: " + nicksFolder.getAbsolutePath());
        }
    }

    private Config getPlayerNickConfig(String playerName) {
        File nickFile = new File(nicksFolder, playerName + "." + dataProvider);
        int configType = dataProvider.equals("yaml") ? Config.YAML : Config.JSON;
        return new Config(nickFile, configType);
    }

    public void setNick(String playerName, String nick) {
        Config config = getPlayerNickConfig(playerName);
        config.set("nickname", nick);
        config.set("hasFakeNick", true);
        config.save();
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Saved nick for player: " + playerName + ", nick: " + nick);
        }
    }

    public void removeNick(String playerName) {
        Config config = getPlayerNickConfig(playerName);
        config.set("nickname", null);
        config.set("hasFakeNick", false);
        config.save();
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Removed nick for player: " + playerName);
        }
    }

    public String getNick(String playerName) {
        Config config = getPlayerNickConfig(playerName);
        if (!config.exists("hasFakeNick") || !config.getBoolean("hasFakeNick")) {
            if (configUtils.isDebug()) {
                plugin.getLogger().info("§b[DEBUG] No fake nick for player: " + playerName);
            }
            return null;
        }
        String nick = config.getString("nickname", null);
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Retrieved nick for player: " + playerName + ", nick: " + (nick != null ? nick : "none"));
        }
        return nick;
    }

    public boolean hasFakeNick(String playerName) {
        Config config = getPlayerNickConfig(playerName);
        boolean hasFakeNick = config.exists("hasFakeNick") && config.getBoolean("hasFakeNick");
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Checking if player has fake nick: " + playerName + ", result: " + hasFakeNick);
        }
        return hasFakeNick;
    }

    public Map<String, String> getAllNicks() {
        Map<String, String> nicks = new HashMap<>();
        File[] files = nicksFolder.listFiles((dir, name) -> name.endsWith("." + dataProvider));
        if (files != null) {
            for (File file : files) {
                Config config = new Config(file, dataProvider.equals("yaml") ? Config.YAML : Config.JSON);
                if (config.exists("hasFakeNick") && config.getBoolean("hasFakeNick")) {
                    String playerName = file.getName().substring(0, file.getName().length() - (dataProvider.length() + 1));
                    String nick = config.getString("nickname", null);
                    if (nick != null) {
                        nicks.put(playerName, nick);
                    }
                }
            }
        }
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Retrieved all nicks: " + nicks);
        }
        return nicks;
    }

    public boolean isNickUsed(String nick) {
        File[] files = nicksFolder.listFiles((dir, name) -> name.endsWith("." + dataProvider));
        if (files != null) {
            for (File file : files) {
                Config config = new Config(file, dataProvider.equals("yaml") ? Config.YAML : Config.JSON);
                if (config.exists("hasFakeNick") && config.getBoolean("hasFakeNick")) {
                    String storedNick = config.getString("nickname", null);
                    if (nick.equalsIgnoreCase(storedNick)) {
                        if (configUtils.isDebug()) {
                            plugin.getLogger().info("§b[DEBUG] Nick is used: " + nick);
                        }
                        return true;
                    }
                }
            }
        }
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Nick is not used: " + nick);
        }
        return false;
    }

    public String getRealName(String fakeNick) {
        File[] files = nicksFolder.listFiles((dir, name) -> name.endsWith("." + dataProvider));
        if (files != null) {
            for (File file : files) {
                Config config = new Config(file, dataProvider.equals("yaml") ? Config.YAML : Config.JSON);
                if (config.exists("hasFakeNick") && config.getBoolean("hasFakeNick")) {
                    String storedNick = config.getString("nickname", null);
                    if (fakeNick.equalsIgnoreCase(storedNick)) {
                        String playerName = file.getName().substring(0, file.getName().length() - (dataProvider.length() + 1));
                        if (configUtils.isDebug()) {
                            plugin.getLogger().info("§b[DEBUG] Found real name: " + playerName + " for nick: " + fakeNick);
                        }
                        return playerName;
                    }
                }
            }
        }
        if (configUtils.isDebug()) {
            plugin.getLogger().info("§b[DEBUG] No real name found for nick: " + fakeNick);
        }
        return null;
    }
}