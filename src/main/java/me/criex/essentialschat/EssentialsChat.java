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
import lombok.Getter;
import lombok.Setter;
import me.criex.essentialschat.api.*;
import me.criex.essentialschat.managers.*;
import me.criex.essentialschat.providers.FallbackProvider;
import me.criex.essentialschat.providers.PrefixSuffixProvider;
import me.criex.essentialschat.providers.ProviderSelector;
import me.criex.essentialschat.utils.ConfigUtils;
import me.criex.essentialschat.utils.Message;

import java.util.*;

@Getter
@Setter
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
    private Message message;

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

        // Event registration
        getServer().getPluginManager().registerEvents(this, this);
        startUpdateTimer();
        if (configUtils.isDebug()) {
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
        if (configUtils.isDebug()) {
            this.getLogger().info("§b[DEBUG] Command executed: " + command.getName() + ", sender: " + sender.getName() + ", args: " + Arrays.toString(args));
        }

        if (command.getName().equalsIgnoreCase("prefix")) {
            if (!(sender instanceof Player)) {
                message.send(sender, "command-only-for-players");
                return true;
            }
            Player player = (Player) sender;
            if (!prefixProviderEnabled) {
                message.send(player, "invalid-provider", "{provider}", "None");
                return true;
            }
            if (args.length == 0) {
                message.send(player, "invalid-usage");
                return true;
            }
            String input = args[0];
            if (input.equalsIgnoreCase("off") || input.equalsIgnoreCase("clear")) {
                prefixManager.clearPlayerPrefix(player);
                message.send(player, "prefix-cleared");
                return true;
            }
            if (configUtils.getPrefixBlacklist().contains(input.toLowerCase())) {
                message.send(player, "prefix-in-blacklist");
                return true;
            }
            if (input.length() < configUtils.getPrefixMinCharacters() || input.length() > configUtils.getPrefixMaxCharacters()) {
                message.send(player, "prefix-length-error", "{min}", String.valueOf(configUtils.getPrefixMinCharacters()), "{max}", String.valueOf(configUtils.getPrefixMaxCharacters()));
                return true;
            }
            if (!input.matches("^[" + configUtils.getAllowedCharactersRegex() + "]+$")) {
                message.send(player, "prefix-invalid-characters", "{allowed}", configUtils.getAllowedCharactersRegex());
                return true;
            }
            prefixManager.setPlayerPrefix(player, input);
            message.send(player, "prefix-set", "{prefix}", input);
            return true;

        } else if (command.getName().equalsIgnoreCase("nick")) {
            if (!(sender instanceof Player)) {
                message.send(sender, "command-only-for-players");
                return true;
            }
            Player player = (Player) sender;
            if (args.length == 0) {
                message.send(player, "nick-usage");
                return true;
            }
            String input = args[0];
            if (input.equalsIgnoreCase(player.getName()) || input.equalsIgnoreCase("off") || input.equalsIgnoreCase("clear")) {
                nickManager.clearPlayerNick(player);
                displayManager.updateDisplay(player);
                message.send(player, "nick-cleared");
                return true;
            }
            if (configUtils.getNicknameBlacklist().contains(input.toLowerCase())) {
                message.send(player, "nick-in-blacklist");
                return true;
            }
            if (input.length() < configUtils.getMinNickCharacters() || input.length() > configUtils.getMaxNickCharacters()) {
                message.send(player, "nick-characters-limit", "{min}", String.valueOf(configUtils.getMinNickCharacters()), "{max}", String.valueOf(configUtils.getMaxNickCharacters()));
                return true;
            }
            if (!input.matches("^[" + configUtils.getAllowedCharactersInNickRegex() + "]+$")) {
                message.send(player, "nick-allowed-characters", "{allowed}", configUtils.getAllowedCharactersInNickRegex());
                return true;
            }
            if (!configUtils.isAllowDuplicateNicknames() && nickManager.isUsed(input)) {
                message.send(player, "nick-used");
                return true;
            }
            nickManager.setPlayerNick(player, input);
            displayManager.updateDisplay(player);
            message.send(player, "nick-success", "{nick}", input);
            return true;

        } else if (command.getName().equalsIgnoreCase("realname")) {
            if (args.length != 1) {
                message.send(sender, "realname-usage");
                return true;
            }
            String real = nickManager.getRealName(args[0]);
            if (real != null) {
                message.send(sender, "realname-output", "{player}", args[0], "{nick}", real);
            } else {
                message.send(sender, "realname-not-found");
            }
            return true;
        }
        return false;
    }

    public String formatNick(String nick) {
        if (configUtils.isAllowColoredNick()) {
            return configUtils.getFakeNicknameCharacter() + nick.replace('&', '§');
        }
        return configUtils.getFakeNicknameCharacter() + nick;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String messageText = event.getMessage();
        String name = player.getName();

        if (configUtils.isChatFilterEnabled()) {
            long now = System.currentTimeMillis();
            long lastTime = lastMessageTime.getOrDefault(name, 0L);
            long cooldownMillis = configUtils.getChatCooldown() * 1000L;
            long timeLeftMillis = cooldownMillis - (now - lastTime);
            if (timeLeftMillis > 0) {
                double timeLeftSeconds = Math.ceil(timeLeftMillis / 1000.0);
                message.send(player, "cooldown-for-messages", "{seconds}", String.valueOf(timeLeftSeconds));
                event.setCancelled(true);
                return;
            }
            if (messageText.length() > configUtils.getChatMaxChars()) {
                message.send(player, "max-message-characters", "{max}", String.valueOf(configUtils.getChatMaxChars()));
                event.setCancelled(true);
                return;
            }
            String lastMsg = lastPlayerMessage.getOrDefault(name, "");
            int repeatCount = messageText.equalsIgnoreCase(lastMsg)
                    ? messageRepetitionCount.getOrDefault(name, 1) + 1
                    : 1;
            if (repeatCount > configUtils.getChatMaxRepeat()) {
                event.setCancelled(true);
                switch (configUtils.getRepetitionPunishment().toLowerCase()) {
                    case "kick":
                        message.send(player, "max-messages-repetition");
                        player.kick("You are sending the same message too many times!", false);
                        break;
                    case "command":
                        if (!configUtils.getPunishmentCommand().isEmpty()) {
                            String command = configUtils.getPunishmentCommand().replace("{player}", player.getName());
                            getServer().dispatchCommand(getServer().getConsoleSender(), command);
                        } else {
                            message.send(player, "max-messages-repetition");
                        }
                        break;
                    case "message":
                    default:
                        message.send(player, "max-messages-repetition");
                        break;
                }
                return;
            }
            lastMessageTime.put(name, now);
            lastPlayerMessage.put(name, messageText);
            messageRepetitionCount.put(name, repeatCount);
        }

        String formatted;
        switch (configUtils.getFormatMethod()) {
            case 2: // Single format
                formatted = chatManager.formatChatMessage(player, configUtils.getSingleChatFormat(), messageText);
                if (configUtils.getSingleChatType().equalsIgnoreCase("local")) {
                    event.getRecipients().removeIf(p -> p instanceof Player &&
                            ((Player) p).distance(player) > configUtils.getLocalChatRadius());
                }
                break;
            case 3: // Default Minecraft format
                formatted = chatManager.formatChatMessage(player, configUtils.getDefaultChatFormat(), messageText);
                break;
            case 1: // L/G chat
            default:
                if (messageText.startsWith(configUtils.getGlobalChatSymbol())) {
                    formatted = chatManager.formatChatMessage(player, configUtils.getGlobalChatFormat(), messageText.substring(configUtils.getGlobalChatSymbol().length()).trim());
                } else {
                    formatted = chatManager.formatChatMessage(player, configUtils.getLocalChatFormat(), messageText);
                    event.getRecipients().removeIf(p -> p instanceof Player &&
                            ((Player) p).distance(player) > configUtils.getLocalChatRadius());
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
}