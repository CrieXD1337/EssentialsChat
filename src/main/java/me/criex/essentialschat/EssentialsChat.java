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
package me.criex.essentialschat;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import lombok.Getter;
import lombok.Setter;
import me.criex.essentialschat.api.*;
import me.criex.essentialschat.commands.CommandRegistry;
import me.criex.essentialschat.listeners.PlayerListener;
import me.criex.essentialschat.managers.*;
import me.criex.essentialschat.providers.FallbackProvider;
import me.criex.essentialschat.providers.PrefixSuffixProvider;
import me.criex.essentialschat.providers.ProviderSelector;
import me.criex.essentialschat.utils.ConfigUtils;
import me.criex.essentialschat.utils.Message;

import java.util.*;

@Getter
@Setter
public class EssentialsChat extends PluginBase {

    private static EssentialsChat instance;
    private static EssentialsChatAPI api;

    private PrefixSuffixProvider provider;
    private boolean placeholderAPIEnabled = false;
    private boolean prefixProviderEnabled = false;

    // Managers
    private NickManager nickManager;
    private PrefixManager prefixManager;
    private DisplayManager displayManager;
    private ChatManager chatManager;
    private Message message;

    // Config
    private ConfigUtils configUtils;

    // Chat filtering
    private final Map<String, Long> lastMessageTime = new HashMap<>();
    private final Map<String, String> lastPlayerMessage = new HashMap<>();
    private final Map<String, Integer> messageRepetitionCount = new HashMap<>();

    private Timer updateTimer;
    private CommandRegistry commandRegistry;

    public static EssentialsChat getInstance() {
        return instance;
    }

    public static EssentialsChatAPI getAPI() {
        return api;
    }

    @Override
    public void onLoad() {
        saveResource("commands.yml");
    }

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config
        configUtils = new ConfigUtils(this);
        message = new Message(this);

        // Check for prefix providers
        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            prefixProviderEnabled = true;
            getLogger().info("§aLuckPerms connected.");
        } else if (getServer().getPluginManager().getPlugin("Multipass") != null) {
            prefixProviderEnabled = true;
            getLogger().info("§aMultipass connected.");
        } else {
            getLogger().warning("§eNo prefix provider found! Prefix support disabled.");
        }

        // PAPI Checker
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            getLogger().info("§aPlaceholderAPI connected.");
        } else {
            getLogger().warning("§ePlaceholderAPI not found. Support disabled.");
        }

        // Provider select
        provider = ProviderSelector.selectProvider(this);
        prefixProviderEnabled = !(provider instanceof FallbackProvider);
        if (prefixProviderEnabled) {
            if (configUtils.isDebug()) {
                getLogger().info("§aPrefix provider auto-selected: §2" + provider.getClass().getSimpleName());
            }
        } else {
            getLogger().warning("§eNo prefix provider found! Prefix support disabled.");
        }

        nickManager = new NickManager(this, configUtils);
        prefixManager = new PrefixManager(this, provider, configUtils);
        displayManager = new DisplayManager(this, provider, configUtils);
        chatManager = new ChatManager(provider, configUtils);
        api = new EssentialsChatAPIImpl(nickManager, prefixManager, provider, displayManager, configUtils);

        // Initialize command registry
        commandRegistry = new CommandRegistry(this);

        // Event registration
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        startUpdateTimer();
        if (configUtils.isDebug()) {
            this.getLogger().info("§aCurrent plugin version: §2" + getDescription().getVersion());
            this.getLogger().info("§aEssentialsChat loaded with §2debug mode§a. Provider: §2" + provider.getClass().getSimpleName());
        } else {
            this.getLogger().info("§aEssentialsChat loaded. Provider: §2" + provider.getClass().getSimpleName());
        }
    }

    @Override
    public void onDisable() {
        if (updateTimer != null) updateTimer.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    public String formatNick(String nick) {
        if (configUtils.isAllowColoredNick()) {
            return configUtils.getFakeNicknameCharacter() + nick.replace('&', '§');
        }
        return configUtils.getFakeNicknameCharacter() + nick;
    }

    private void startUpdateTimer() {
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers().values()) {
                    displayManager.updateDisplay(player);
                }
            }
        }, 0, 5000);
    }

    public String parsePlaceholders(Player player, String text) {
        if (!placeholderAPIEnabled || text == null || text.isEmpty()) return text;
        try {
            Class<?> placeholderAPIClass = Class.forName("com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI");
            Object apiInstance = placeholderAPIClass.getMethod("getInstance").invoke(null);
            return (String) placeholderAPIClass.getMethod("translateString", String.class, Player.class).invoke(apiInstance, text, player);
        } catch (Exception e) {
            getLogger().warning("§ePlaceholderAPI error: " + e.getMessage());
            placeholderAPIEnabled = false;
        }
        return text;
    }
}