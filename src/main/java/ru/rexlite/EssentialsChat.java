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
package ru.rexlite;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import ru.rexlite.api.EssentialsChatAPI;
import ru.rexlite.api.EssentialsChatAPIImpl;
import ru.rexlite.managers.ChatManager;
import ru.rexlite.managers.DisplayManager;
import ru.rexlite.managers.NickManager;
import ru.rexlite.managers.PrefixManager;
import ru.rexlite.providers.FallbackProvider;
import ru.rexlite.providers.LProvider;
import ru.rexlite.providers.MultipassProvider;
import ru.rexlite.providers.PrefixSuffixProvider;

import java.util.*;
import java.util.stream.Collectors;

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

    // Settings
    private List<String> nicknameBlacklist;
    private List<String> prefixBlacklist;

    private int localChatRadius;
    private String globalChatSymbol;
    private String localChatFormat;
    private String globalChatFormat;
    private String opNicknameColor;
    private boolean allowColoredNick;
    private String fakeNicknameCharacter;
    private boolean allowDuplicateNicknames;
    private boolean prefixInSettingsAndHeadEnabled;
    private String prefixInSettingsAndHeadFormat;
    private int prefixMaxCharacters, prefixMinCharacters;
    private int maxNickCharacters, minNickCharacters;
    private String allowedCharactersRegex, allowedCharactersInNickRegex;

    // Chat filtering
    private boolean chatFilterEnabled;
    private int chatCooldown;
    private int chatMaxChars;
    private int chatMaxRepeat;
    private final Map<String, Long> lastMessageTime = new HashMap<>();
    private final Map<String, String> lastPlayerMessage = new HashMap<>();
    private final Map<String, Integer> messageRepetitionCount = new HashMap<>();

    // Messages
    private String msgPrefixSet, msgPrefixCleared, msgInvalidProvider, msgPrefixBlackList;
    private String msgNickSuccess, msgNickCleared, msgNickBlackList, msgNickUsed;
    private String msgNickUsage, msgRealNameUsage, msgRealNameOutput, msgRealNameNotFound;
    private String msgPrefixLengthError, msgPrefixInvalidChars, msgCmdOnlyForPlayers, msgPrefixUsage;
    private String msgCooldown;
    private String msgTooLong;
    private String msgTooManyRepeat;
    private String msgNickMaxLimit;
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

        saveDefaultConfig();
        saveResource("messages.yml");
        Config messagesConfig = new Config(getDataFolder() + "/messages.yml", Config.YAML);
        reloadConfig();
        Config config = getConfig();

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

        // Blacklist reading
        nicknameBlacklist = config.getStringList("nicknames-blacklist").stream().map(String::toLowerCase).collect(Collectors.toList());
        prefixBlacklist = config.getStringList("prefixes-blacklist").stream().map(String::toLowerCase).collect(Collectors.toList());

        // Settings
        localChatRadius = config.getInt("local-chat-radius", 100);
        globalChatSymbol = config.getString("global-chat-symbol", "!");
        localChatFormat = config.getString("local-chat-format");
        globalChatFormat = config.getString("global-chat-format");
        prefixInSettingsAndHeadEnabled = config.getBoolean("prefix-in-settings-and-head.enabled", false);
        prefixInSettingsAndHeadFormat = config.getString("prefix-in-settings-and-head.format");

        prefixMaxCharacters = config.getInt("prefix-max-characters", 15);
        prefixMinCharacters = config.getInt("prefix-min-characters", 3);
        allowedCharactersRegex = config.getString("allowed-characters", "A-Za-z0-9_-");
        maxNickCharacters = config.getInt("max-nick-characters-length", 15);
        minNickCharacters = config.getInt("min-nick-characters-length", 3);
        allowedCharactersInNickRegex = config.getString("allowed-characters-in-nick", "A-Za-z0-9_-");
        allowColoredNick = config.getBoolean("allow-colored-nick", false);
        allowDuplicateNicknames = config.getBoolean("allow-duplicate-nicknames", false);
        fakeNicknameCharacter = config.getString("fake-nickname-character", "~");
        if ("BOLD".equalsIgnoreCase(fakeNicknameCharacter)) fakeNicknameCharacter = "§o";
        else fakeNicknameCharacter = fakeNicknameCharacter.replace('&', '§');
        opNicknameColor = config.getString("op-nickname-color", "c");
        if (!opNicknameColor.matches("[0-9a-f]")) {
            getLogger().warning("Invalid OP color. Default set: 4");
            opNicknameColor = "4";
        }

        // Chat filtering
        chatFilterEnabled = config.getBoolean("chat-filtering.enabled", true);
        chatCooldown = config.getInt("chat-filtering.cooldown-for-messages", 3);
        chatMaxChars = config.getInt("chat-filtering.max-message-characters", 200);
        chatMaxRepeat = config.getInt("chat-filtering.max-messages-repetition", 5);

        // Messages
        msgPrefixSet = messagesConfig.getString("prefix-set", "§7> §fYour prefix successfully moved to: §b{prefix}");
        msgPrefixCleared = messagesConfig.getString("prefix-cleared", "§7> §fYour prefix was §ccleared");
        msgInvalidProvider = messagesConfig.getString("invalid-provider", "§7> §cProvider §4{provider} §cis currently not available for prefixes. Use §4LuckPerms.");
        msgPrefixBlackList = messagesConfig.getString("prefix-in-blacklist", "§7> §cThis prefix is banned!");
        msgNickSuccess = messagesConfig.getString("nick-success", "§7> §fYour nickname changed to §b{nick}");
        msgNickCleared = messagesConfig.getString("nick-cleared", "§7> §fYour nickname §ccleared");
        msgNickBlackList = messagesConfig.getString("nick-in-blacklist", "§7> §cThis nickname is banned!");
        msgNickUsed = messagesConfig.getString("nick-used", "§7> §cThis nickname is already in use!");
        msgNickUsage = messagesConfig.getString("nick-usage", "§7> §cUsage: §e/nick <nick>");
        msgNickMaxLimit = messagesConfig.getString("nick-usage", "§7> §cUsage: §e/nick <nick>");
        msgRealNameUsage = messagesConfig.getString("realname-usage", "§7> §cUsage: §e/realname <player>");
        msgRealNameOutput = messagesConfig.getString("realname-output", "§7> §fReal name of player §b{player}: §3{nick}");
        msgRealNameNotFound = messagesConfig.getString("realname-not-found", "§7> §cPlayer not found!");
        msgPrefixLengthError = messagesConfig.getString("prefix-length-error", "§7> §cThe prefix must be between §4{min}§c and §4{max}§c characters.");
        msgPrefixInvalidChars = messagesConfig.getString("prefix-invalid-characters", "§cPrefix contains invalid characters! Only allowed: §4{allowed}");
        msgCmdOnlyForPlayers = messagesConfig.getString("command-only-for-players", "§cAllowed only for players!");
        msgPrefixUsage = messagesConfig.getString("invalid-usage", "§7> §cUsage: §e/prefix <prefix|off>");
        msgCooldown = messagesConfig.getString("cooldown-for-messages", "§7> §cWait §b{seconds} §cseconds");
        msgTooLong = messagesConfig.getString("max-message-characters", "§7> §cMaximum characters in message - §b{max}§c!");
        msgTooManyRepeat = messagesConfig.getString("max-messages-repetition", "§7> §cYou are sending the same message too many times!");

        // Provider select
        String providerName = config.getString("provider", "Multipass");
        if (prefixProviderEnabled) {
            if (providerName.equalsIgnoreCase("Multipass") && getServer().getPluginManager().getPlugin("Multipass") != null) {
                provider = new MultipassProvider();
            } else if (providerName.equalsIgnoreCase("LuckPerms") && getServer().getPluginManager().getPlugin("LuckPerms") != null) {
                try {
                    provider = new LProvider();
                } catch (NoClassDefFoundError e) {
                    getLogger().warning("§eLuckPerms classes not found! Falling back to Multipass or Fallback provider.");
                    provider = getServer().getPluginManager().getPlugin("Multipass") != null ? new MultipassProvider() : new FallbackProvider();
                }
            } else {
                provider = new FallbackProvider();
                getLogger().warning("§eSpecified provider not found. Using fallback provider.");
            }
        } else {
            provider = new FallbackProvider();
        }

        nickManager = new NickManager(this);
        prefixManager = new PrefixManager(this, provider);
        displayManager = new DisplayManager(this, provider);
        chatManager = new ChatManager(provider);
        api = new EssentialsChatAPIImpl(nickManager, prefixManager, provider, displayManager);

        // Event registration
        getServer().getPluginManager().registerEvents(this, this);
        startUpdateTimer();
        getLogger().info("§aEssentialsChat loaded. Provider: §e" + provider.getClass().getSimpleName());
    }

    @Override
    public void onDisable() {
        if (updateTimer != null) updateTimer.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("prefix")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(msgCmdOnlyForPlayers);
                return true;
            }
            Player player = (Player) sender;
            if (!prefixProviderEnabled) {
                player.sendMessage("§cPrefixes disabled: No prefix provider installed.");
                return true;
            }
            if (args.length == 0) {
                player.sendMessage(msgPrefixUsage);
                return true;
            }
            String input = args[0];
            if (input.equalsIgnoreCase("off") || input.equalsIgnoreCase("clear")) {
                prefixManager.clearPlayerPrefix(player);
                player.sendMessage(msgPrefixCleared);
                return true;
            }
            if (prefixBlacklist.contains(input.toLowerCase())) {
                player.sendMessage(msgPrefixBlackList);
                return true;
            }
            if (input.length() < prefixMinCharacters || input.length() > prefixMaxCharacters) {
                player.sendMessage(msgPrefixLengthError.replace("{min}", String.valueOf(prefixMinCharacters)).replace("{max}", String.valueOf(prefixMaxCharacters)));
                return true;
            }
            if (!input.matches("^[" + allowedCharactersRegex + "]+$")) {
                player.sendMessage(msgPrefixInvalidChars.replace("{allowed}", allowedCharactersRegex));
                return true;
            }
            prefixManager.setPlayerPrefix(player, input);
            player.sendMessage(msgPrefixSet.replace("{prefix}", input));
            return true;

        } else if (command.getName().equalsIgnoreCase("nick")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(msgCmdOnlyForPlayers);
                return true;
            }
            Player player = (Player) sender;
            if (args.length == 0) {
                player.sendMessage(msgNickUsage);
                return true;
            }
            String input = args[0];
            if (input.equalsIgnoreCase(player.getName()) || input.equalsIgnoreCase("off") || input.equalsIgnoreCase("clear")) {
                nickManager.clearPlayerNick(player);
                displayManager.updateDisplay(player);
                player.sendMessage(msgNickCleared);
                return true;
            }
            if (nicknameBlacklist.contains(input.toLowerCase())) {
                player.sendMessage(msgNickBlackList);
                return true;
            }
            if (input.length() < minNickCharacters || input.length() > maxNickCharacters) {
                player.sendMessage(msgNickMaxLimit);
                player.sendMessage("§7> §cNickname must be from §b" + minNickCharacters + "§c to §b" + maxNickCharacters + " §ccharacters!");
                return true;
            }
            if (!input.matches("^[" + allowedCharactersInNickRegex + "]+$")) {
                player.sendMessage("§cInvalid characters! Allowed: §4" + allowedCharactersInNickRegex);
                return true;
            }
            if (!allowDuplicateNicknames && nickManager.isUsed(input)) {
                player.sendMessage(msgNickUsed);
                return true;
            }
            nickManager.setPlayerNick(player, input);
            displayManager.updateDisplay(player);
            player.sendMessage(msgNickSuccess.replace("{nick}", input));
            return true;

        } else if (command.getName().equalsIgnoreCase("realname")) {
            if (args.length != 1) {
                sender.sendMessage(msgRealNameUsage);
                return true;
            }
            String real = nickManager.getRealName(args[0]);
            if (real != null) {
                sender.sendMessage(msgRealNameOutput.replace("{player}", args[0]).replace("{nick}", real));
            } else {
                sender.sendMessage(msgRealNameNotFound);
            }
            return true;
        }
        return false;
    }

    public boolean isPrefixInSettingsAndHeadEnabled() {
        return this.prefixInSettingsAndHeadEnabled;
    }

    public String getPrefixInSettingsAndHeadFormat() {
        return this.prefixInSettingsAndHeadFormat;
    }

    public String formatNick(String nick) {
        if (allowColoredNick) {
            return fakeNicknameCharacter + nick.replace('&', '§');
        }
        return fakeNicknameCharacter + nick;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String message = event.getMessage();
        String name = player.getName();

        if (chatFilterEnabled) {
            long now = System.currentTimeMillis();
            long lastTime = lastMessageTime.getOrDefault(name, 0L);
            long cooldownMillis = chatCooldown * 1000L;
            long timeLeftMillis = cooldownMillis - (now - lastTime);
            if (timeLeftMillis > 0) {
                double timeLeftSeconds = Math.ceil(timeLeftMillis / 1000.0);
                player.sendMessage(msgCooldown.replace("{seconds}", String.valueOf(timeLeftSeconds)));
                event.setCancelled(true);
                return;
            }
            if (message.length() > chatMaxChars) {
                player.sendMessage(msgTooLong.replace("{max}", String.valueOf(chatMaxChars)));
                event.setCancelled(true);
                return;
            }
            String lastMsg = lastPlayerMessage.getOrDefault(name, "");
            int repeatCount = message.equalsIgnoreCase(lastMsg)
                    ? messageRepetitionCount.getOrDefault(name, 1) + 1
                    : 1;
            if (repeatCount > chatMaxRepeat) {
                player.sendMessage(msgTooManyRepeat);
                event.setCancelled(true);
                return;
            }
            lastMessageTime.put(name, now);
            lastPlayerMessage.put(name, message);
            messageRepetitionCount.put(name, repeatCount);
        }
        String formatted;

        if (message.startsWith(globalChatSymbol)) {
            formatted = chatManager.formatChatMessage(player,
                    globalChatFormat,
                    message.substring(globalChatSymbol.length()).trim());
        } else {
            formatted = chatManager.formatChatMessage(player,
                    localChatFormat,
                    message);
            event.getRecipients().removeIf(p -> p instanceof Player &&
                    ((Player) p).distance(player) > localChatRadius);
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
}