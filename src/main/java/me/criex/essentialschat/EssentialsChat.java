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
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import me.criex.essentialschat.api.*;
import me.criex.essentialschat.managers.*;
import me.criex.essentialschat.providers.FallbackProvider;
import me.criex.essentialschat.providers.PrefixSuffixProvider;
import me.criex.essentialschat.providers.ProviderSelector;
import me.criex.essentialschat.utils.ConfigUtils;

import java.util.*;

public class EssentialsChat extends PluginBase implements Listener {

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

    // Config
    private ConfigUtils configUtils;

    // Chat filtering
    private final Map<String, Long> lastMessageTime = new HashMap<>();
    private final Map<String, String> lastPlayerMessage = new HashMap<>();
    private final Map<String, Integer> messageRepetitionCount = new HashMap<>();

    private Timer updateTimer;

    public static EssentialsChat getInstance() {
        return instance;
    }

    public static EssentialsChatAPI getAPI() {
        return api;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config
        configUtils = new ConfigUtils(this);

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
            if (configUtils.debug) {
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

        // Event registration
        getServer().getPluginManager().registerEvents(this, this);
        startUpdateTimer();
        if (configUtils.debug) {
            this.getLogger().info("§aCurrent plugin version: §2" + getDescription().getVersion());
            this.getLogger().info("§aEssentialsChat loaded with §2debug mode§a. Provider: §2" + provider.getClass().getSimpleName());
        } else {
            getLogger().info("§aEssentialsChat loaded. Provider: §2" + provider.getClass().getSimpleName());
        }
    }

    @Override
    public void onDisable() {
        if (updateTimer != null) updateTimer.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (configUtils.debug) {
            this.getLogger().info("§b[DEBUG] Command executed: " + command.getName() + ", sender: " + sender.getName() + ", args: " + Arrays.toString(args));
        }

        if (command.getName().equalsIgnoreCase("prefix")) {
            if (!(sender instanceof Player)) {
                // other debug - soon?
                //if (isDebugEnabled()) {
                //    getLogger().info("§b[DEBUG] Command /prefix failed: sender is not a player");
                //}
                sender.sendMessage(configUtils.msgCmdOnlyForPlayers);
                return true;
            }
            Player player = (Player) sender;
            if (!prefixProviderEnabled) {
                player.sendMessage("§cPrefixes disabled: No prefix provider installed.");
                return true;
            }
            if (args.length == 0) {
                player.sendMessage(configUtils.msgPrefixUsage);
                return true;
            }
            String input = args[0];
            if (input.equalsIgnoreCase("off") || input.equalsIgnoreCase("clear")) {
                prefixManager.clearPlayerPrefix(player);
                player.sendMessage(configUtils.msgPrefixCleared);
                return true;
            }
            if (configUtils.prefixBlacklist.contains(input.toLowerCase())) {
                player.sendMessage(configUtils.msgPrefixBlackList);
                return true;
            }
            if (input.length() < configUtils.prefixMinCharacters || input.length() > configUtils.prefixMaxCharacters) {
                player.sendMessage(configUtils.msgPrefixLengthError.replace("{min}", String.valueOf(configUtils.prefixMinCharacters)).replace("{max}", String.valueOf(configUtils.prefixMaxCharacters)));
                return true;
            }
            if (!input.matches("^[" + configUtils.allowedCharactersRegex + "]+$")) {
                player.sendMessage(configUtils.msgPrefixInvalidChars.replace("{allowed}", configUtils.allowedCharactersRegex));
                return true;
            }
            prefixManager.setPlayerPrefix(player, input);
            player.sendMessage(configUtils.msgPrefixSet.replace("{prefix}", input));
            return true;

        } else if (command.getName().equalsIgnoreCase("nick")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(configUtils.msgCmdOnlyForPlayers);
                return true;
            }
            Player player = (Player) sender;
            if (args.length == 0) {
                player.sendMessage(configUtils.msgNickUsage);
                return true;
            }
            String input = args[0];
            if (input.equalsIgnoreCase(player.getName()) || input.equalsIgnoreCase("off") || input.equalsIgnoreCase("clear")) {
                nickManager.clearPlayerNick(player);
                displayManager.updateDisplay(player);
                player.sendMessage(configUtils.msgNickCleared);
                return true;
            }
            if (configUtils.nicknameBlacklist.contains(input.toLowerCase())) {
                player.sendMessage(configUtils.msgNickBlackList);
                return true;
            }
            if (input.length() < configUtils.minNickCharacters || input.length() > configUtils.maxNickCharacters) {
                player.sendMessage(configUtils.msgNickMaxLimit.replace("{min}", String.valueOf(configUtils.minNickCharacters)).replace("{max}", String.valueOf(configUtils.maxNickCharacters)));
                return true;
            }
            if (!input.matches("^[" + configUtils.allowedCharactersInNickRegex + "]+$")) {
                player.sendMessage(configUtils.msgNickAllowedCharacters.replace("{allowed}", configUtils.allowedCharactersInNickRegex));
                return true;
            }
            if (!configUtils.allowDuplicateNicknames && nickManager.isUsed(input)) {
                player.sendMessage(configUtils.msgNickUsed);
                return true;
            }
            nickManager.setPlayerNick(player, input);
            displayManager.updateDisplay(player);
            player.sendMessage(configUtils.msgNickSuccess.replace("{nick}", input));
            return true;

        } else if (command.getName().equalsIgnoreCase("realname")) {
            if (args.length != 1) {
                sender.sendMessage(configUtils.msgRealNameUsage);
                return true;
            }
            String real = nickManager.getRealName(args[0]);
            if (real != null) {
                sender.sendMessage(configUtils.msgRealNameOutput.replace("{player}", args[0]).replace("{nick}", real));
            } else {
                sender.sendMessage(configUtils.msgRealNameNotFound);
            }
            return true;
        }
        return false;
    }

    public boolean isPrefixInSettingsAndHeadEnabled() {
        return configUtils.prefixInSettingsAndHeadEnabled;
    }

    public String getPrefixInSettingsAndHeadFormat() {
        return configUtils.prefixInSettingsAndHeadFormat;
    }

    public String formatNick(String nick) {
        if (configUtils.allowColoredNick) {
            return configUtils.fakeNicknameCharacter + nick.replace('&', '§');
        }
        return configUtils.fakeNicknameCharacter + nick;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String message = event.getMessage();
        String name = player.getName();

        if (configUtils.chatFilterEnabled) {
            long now = System.currentTimeMillis();
            long lastTime = lastMessageTime.getOrDefault(name, 0L);
            long cooldownMillis = configUtils.chatCooldown * 1000L;
            long timeLeftMillis = cooldownMillis - (now - lastTime);
            if (timeLeftMillis > 0) {
                double timeLeftSeconds = Math.ceil(timeLeftMillis / 1000.0);
                player.sendMessage(configUtils.msgCooldown.replace("{seconds}", String.valueOf(timeLeftSeconds)));
                event.setCancelled(true);
                return;
            }
            if (message.length() > configUtils.chatMaxChars) {
                player.sendMessage(configUtils.msgTooLong.replace("{max}", String.valueOf(configUtils.chatMaxChars)));
                event.setCancelled(true);
                return;
            }
            String lastMsg = lastPlayerMessage.getOrDefault(name, "");
            int repeatCount = message.equalsIgnoreCase(lastMsg)
                    ? messageRepetitionCount.getOrDefault(name, 1) + 1
                    : 1;
            if (repeatCount > configUtils.chatMaxRepeat) {
                event.setCancelled(true);
                switch (configUtils.repetitionPunishment.toLowerCase()) {
                    case "kick":
                        player.kick(configUtils.msgTooManyRepeat, false);
                        break;
                    case "command":
                        if (!configUtils.punishmentCommand.isEmpty()) {
                            String command = configUtils.punishmentCommand.replace("{player}", player.getName());
                            getServer().dispatchCommand(getServer().getConsoleSender(), command);
                        } else {
                            player.sendMessage(configUtils.msgTooManyRepeat);
                        }
                        break;
                    case "message":
                    default:
                        player.sendMessage(configUtils.msgTooManyRepeat);
                        break;
                }
                return;
            }
            lastMessageTime.put(name, now);
            lastPlayerMessage.put(name, message);
            messageRepetitionCount.put(name, repeatCount);
        }

        String formatted;
        switch (configUtils.formatMethod) {
            case 2: // Single format
                formatted = chatManager.formatChatMessage(player, configUtils.singleChatFormat, message);
                if (configUtils.singleChatType.equalsIgnoreCase("local")) {
                    event.getRecipients().removeIf(p -> p instanceof Player &&
                            ((Player) p).distance(player) > configUtils.localChatRadius);
                }
                break;
            case 3: // Default Minecraft format
                formatted = chatManager.formatChatMessage(player, configUtils.defaultChatFormat, message);
                break;
            case 1: // L/G chat
            default:
                if (message.startsWith(configUtils.globalChatSymbol)) {
                    formatted = chatManager.formatChatMessage(player, configUtils.globalChatFormat, message.substring(configUtils.globalChatSymbol.length()).trim());
                } else {
                    formatted = chatManager.formatChatMessage(player, configUtils.localChatFormat, message);
                    event.getRecipients().removeIf(p -> p instanceof Player &&
                            ((Player) p).distance(player) > configUtils.localChatRadius);
                }
                break;
        }
        formatted = parsePlaceholders(player, formatted);
        event.setFormat(formatted);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        displayManager.updateDisplay(event.getPlayer());
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

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public NickManager getNickManager() {
        return nickManager;
    }

    public boolean isDebugEnabled() {
        return configUtils.debug;
    }

    public ConfigUtils getConfigUtils() {
        return configUtils;
    }
}