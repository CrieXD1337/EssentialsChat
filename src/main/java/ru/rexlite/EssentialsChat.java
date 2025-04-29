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
import ru.rexlite.providers.PrefixSuffixProvider;
import ru.rexlite.providers.LProvider;
import ru.rexlite.providers.MultipassProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class EssentialsChat extends PluginBase implements Listener {
    private boolean placeholderAPIEnabled = false;
    private static EssentialsChat instance;
    private List<String> nicknameBlacklist;
    private List<String> prefixBlacklist;

    public static EssentialsChat getInstance() {
        return instance;
    }

    private int localChatRadius;
    private String globalChatSymbol;
    private String localChatFormat;
    private String globalChatFormat;
    private PrefixSuffixProvider provider;
    private boolean prefixInSettingsAndHeadEnabled;
    private String prefixInSettingsAndHeadFormat;
    private String opNicknameColor;
    private int prefixMaxCharacters;
    private int prefixMinCharacters;
    private String allowedCharactersRegex;
    private Timer updateTimer;
    private String messagePrefixCleared;
    private String messagePrefixSet;
    private String messageInvalidUsage;
    private String messageInvalidProvider;
    private String messagePrefixLengthError;
    private String messagePrefixInvalidCharacters;
    private String messageCommandOnlyForPlayers;
    private int maxNickCharacters;
    private int minNickCharacters;
    private String allowedCharactersInNickRegex;
    private boolean allowColoredNick;
    private String messageNickSuccess;
    private String messageNickCleared;
    private String messageNickUsage;
    private String messageNickBlackList;
    private String messagePrefixBlackList;
    private String messageRealNameUsage;
    private String messageRealNameOutput;
    private String messageRealNameNotFound;
    private String fakeNicknameCharacter;
    private String messageNicknameAlreadyUsed;
    private boolean allowDuplicateNicknames;
    private final Map<String, String> playerNicks = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        this.getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        reloadConfig();
        Config config = getConfig();

        // Blacklist load
        nicknameBlacklist = config.getStringList("nicknames-blacklist").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        prefixBlacklist = config.getStringList("prefixes-blacklist").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            getLogger().info("§aPlaceholderAPI was found. Placeholders support enabled.");
        } else {
            getLogger().error("§ePlaceholderAPI not found. Placeholders support disabled.");
        }
        localChatRadius = config.getInt("local-chat-radius", 100);
        globalChatSymbol = config.getString("global-chat-symbol", "!");
        localChatFormat = config.getString("local-chat-format", "§7[§aL§7] §7[{prefix}§r§7] §f{player}{suffix} §a» §8{msg}");
        globalChatFormat = config.getString("global-chat-format", "§7[§4G§7] §7[{prefix}§r§7] §f{player}{suffix} §a» §f{msg}");
        prefixInSettingsAndHeadEnabled = config.getBoolean("prefix-in-settings-and-head.enabled", false);
        prefixInSettingsAndHeadFormat = config.getString("prefix-in-settings-and-head.format", "[{prefix}] {player}{suffix}");
        String providerName = config.getString("provider", "LuckPerms");
        switch (providerName.toLowerCase()) {
            case "luckperms":
                provider = new LProvider();
                break;
            case "multipass":
                provider = new MultipassProvider();
                break;
            default:
                getLogger().error("§c[Essentials§eChat] §cUnknown provider:§e " + providerName + ". §cThe provider has been reset to §eLuckPerms.");
                provider = new LProvider();
                break;
        }
        opNicknameColor = config.getString("op-nickname-color", "c");
        if (!isValidMinecraftColor(opNicknameColor)) {
            getLogger().warning("§cUnsupported color format in op-nickname-color: §4" + opNicknameColor + "§c. Moved to default value: §44");
            opNicknameColor = "4";
        }
        prefixMaxCharacters = config.getInt("prefix-max-characters", 15);
        prefixMinCharacters = config.getInt("prefix-min-characters", 3);
        allowedCharactersRegex = config.getString("allowed-characters", "A-Za-z0-9_-");
        maxNickCharacters = config.getInt("max-nick-characters-length", 15);
        minNickCharacters = config.getInt("min-nick-characters-length", 3);
        allowedCharactersInNickRegex = config.getString("allowed-characters-in-nick", "A-Za-z0-9_-");
        allowColoredNick = config.getBoolean("allow-colored-nick", false);
        allowDuplicateNicknames = config.getBoolean("allow-duplicate-nicknames", false);
        fakeNicknameCharacter = config.getString("fake-nickname-character", "~");
        if ("BOLD".equalsIgnoreCase(fakeNicknameCharacter)) {
            fakeNicknameCharacter = "§o";
        } else {
            fakeNicknameCharacter = replaceAmpersandWithSectionSign(fakeNicknameCharacter);
        }
        messagePrefixCleared = config.getString("messages.prefix-cleared", "§7> §fYour prefix was §ccleared");
        messagePrefixSet = config.getString("messages.prefix-set", "§7> §fYour prefix successfully moved to: §b{prefix}");
        messageInvalidUsage = config.getString("messages.invalid-usage", "§7> §cUsage: /prefix <prefix|off>");
        messageInvalidProvider = config.getString("messages.invalid-provider", "§7> §cProvider §4{provider} §cis currently not available for prefixes. Use §4LuckPerms.");
        messagePrefixLengthError = config.getString("messages.prefix-length-error", "§7> §cThe prefix must be between §4{min}§c and §4{max}§c characters.");
        messagePrefixInvalidCharacters = config.getString("messages.prefix-invalid-characters", "§cPrefix contains invalid characters! Only allowed: §4{allowed}");
        messagePrefixBlackList = config.getString("messages.prefix-in-blacklist");
        messageCommandOnlyForPlayers = config.getString("messages.command-only-for-players", "§cAllowed only for players!");
        messageNickSuccess = config.getString("messages.nick-success", "§7> §fYour nickname changed to §b{nick}");
        messageNickCleared = config.getString("messages.nick-cleared", "§7> §fYour nickname §ccleared");
        messageNickUsage = config.getString("messages.nick-usage", "§7> §cUsage: §e/nick <nick>");
        messageNickBlackList = config.getString("messages.nick-in-blacklist");
        messageRealNameUsage = config.getString("messages.realname-usage", "§7> §cUsage: §e/realname <player>");
        messageRealNameOutput = config.getString("messages.realname-output", "§7> Real name of player §b{player}: §3{nick}");
        messageRealNameNotFound = config.getString("messages.realname-not-found", "§7> §cPlayer not found");
        messageNicknameAlreadyUsed = config.getString("messages.nickname-already-used", "§7> §cThis nickname is already used by another player!");
        startUpdateTimer();
        getLogger().info("§bEssentialsChat enabled! Provider: §3" + provider.getClass().getSimpleName());
        getLogger().info("§bPlugin from:§3 https://cloudburstmc.org/resources/essentialschat.1062/");
    }

    @Override
    public void onDisable() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }

    public static String parsePlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) return text;
        EssentialsChat plugin = getInstance();
        if (!plugin.placeholderAPIEnabled) {
            return text;
        }
        try {
            Class<?> placeholderAPIClass = Class.forName("com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI");
            Object apiInstance = placeholderAPIClass.getMethod("getInstance").invoke(null);
            java.lang.reflect.Method method = placeholderAPIClass.getMethod("translateString", String.class, Player.class);
            return (String) method.invoke(apiInstance, text, player);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("§ePlaceholderAPI not found. Plugin works without placeholders!");
            plugin.placeholderAPIEnabled = false;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().error("Error #3 by PlaceholderAPI", e);
        }
        return text;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        String message = event.getMessage();
        String formattedMessage;
        if (message.startsWith(globalChatSymbol)) {
            formattedMessage = formatMessage(globalChatFormat, player, message.substring(globalChatSymbol.length()).trim());
        } else {
            formattedMessage = formatMessage(localChatFormat, player, message);
        }
        formattedMessage = parsePlaceholders(player, formattedMessage);
        event.setFormat(formattedMessage);
        if (!message.startsWith(globalChatSymbol)) {
            event.getRecipients().removeIf(onlinePlayer -> {
                if (!(onlinePlayer instanceof Player)) {
                    return true;
                }
                Player playerInList = (Player) onlinePlayer;
                return playerInList.distance(player) > localChatRadius;
            });
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updatePlayerDisplayName(player);
    }

    private String formatMessage(String format, Player player, String message) {
        return format.replace("{prefix}", provider.getPrefix(player))
                .replace("{player}", getPlayerDisplayName(player))
                .replace("{suffix}", provider.getSuffix(player))
                .replace("{msg}", message);
    }

    private String getPlayerDisplayName(Player player) {
        String nick = playerNicks.getOrDefault(player.getName(), null);
        if (nick != null) {
            String formattedNick = allowColoredNick ? replaceAmpersandWithSectionSign(nick) : nick;
            if ("§o".equals(fakeNicknameCharacter)) {
                return "§o" + formattedNick + "§r";
            }
            return fakeNicknameCharacter + formattedNick;
        }
        if (player.isOp()) {
            return "§" + opNicknameColor + player.getName() + "§r§f";
        }
        return player.getName();
    }

    private void updatePlayerDisplayName(Player player) {
        if (!prefixInSettingsAndHeadEnabled) {
            return;
        }
        String prefix = provider.getPrefix(player);
        String suffix = provider.getSuffix(player);
        String displayName = prefixInSettingsAndHeadFormat
                .replace("{prefix}", prefix)
                .replace("{player}", getPlayerDisplayName(player))
                .replace("{suffix}", suffix);
        displayName = parsePlaceholders(player, displayName);
        player.setDisplayName(displayName);
        player.setNameTag(displayName);
    }

    private void startUpdateTimer() {
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers().values()) {
                    updatePlayerDisplayName(player);
                }
            }
        }, 0, 5 * 1000);
    }

    private boolean isValidMinecraftColor(String colorCode) {
        return colorCode.matches("[0-9a-f]");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("prefix")) {
            return handlePrefixCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("nick")) {
            return handleNickCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("realname")) {
            return handleRealNameCommand(sender, args);
        }
        return false;
    }

    private boolean handlePrefixCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageCommandOnlyForPlayers);
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("essentialschat.prefix.setprefix")) {
            player.sendMessage("§c%commands.generic.permission");
            return true;
        }
        if (!(provider instanceof LProvider)) {
            player.sendMessage(messageInvalidProvider.replace("{provider}", provider.getClass().getSimpleName()));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(messageInvalidUsage);
            return true;
        }
        String input = args[0];
        if (input.equalsIgnoreCase("off") || input.equalsIgnoreCase("clear")) {
            clearPlayerPrefix(player);
            return true;
        }
        if (prefixBlacklist.contains(input.toLowerCase())) {
            player.sendMessage(messagePrefixBlackList);
            return true;
        }
        if (input.length() < prefixMinCharacters || input.length() > prefixMaxCharacters) {
            player.sendMessage(messagePrefixLengthError
                    .replace("{min}", String.valueOf(prefixMinCharacters))
                    .replace("{max}", String.valueOf(prefixMaxCharacters)));
            return true;
        }
        if (!input.matches("^[" + allowedCharactersRegex + "]+$")) {
            player.sendMessage(messagePrefixInvalidCharacters.replace("{allowed}", allowedCharactersRegex));
            return true;
        }
        setPlayerPrefix(player, input);
        return true;
    }

    private void setPlayerPrefix(Player player, String prefix) {
        String formattedPrefix = replaceAmpersandWithSectionSign(prefix);
        String command = "lp user " + player.getName() + " meta addprefix 10483 \"" + formattedPrefix + "\"";
        getServer().dispatchCommand(getServer().getConsoleSender(), command);
        player.sendMessage(messagePrefixSet.replace("{prefix}", formattedPrefix));
    }

    private void clearPlayerPrefix(Player player) {
        String command = "lp user " + player.getName() + " meta removeprefix 10483";
        getServer().dispatchCommand(getServer().getConsoleSender(), command);
        player.sendMessage(messagePrefixCleared);
    }

    private boolean handleNickCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageCommandOnlyForPlayers);
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("essentialschat.nick.setnick")) {
            player.sendMessage("§c%commands.generic.permission");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(messageNickUsage);
            return true;
        }
        String input = args[0];
        if (input.equalsIgnoreCase(player.getName())) {
            clearPlayerNick(player);
            return true;
        }
        if (input.equalsIgnoreCase("off") || input.equalsIgnoreCase("clear")) {
            clearPlayerNick(player);
            return true;
        }
        if (nicknameBlacklist.contains(input.toLowerCase())) {
            player.sendMessage(messageNickBlackList);
            return true;
        }
        if (input.length() < minNickCharacters || input.length() > maxNickCharacters) {
            player.sendMessage("§7> §cThe nickname must be between §4" + minNickCharacters + "§c and §4" + maxNickCharacters + "§c characters.");
            return true;
        }
        if (!input.matches("^[" + allowedCharactersInNickRegex + "]+$")) {
            player.sendMessage("§cNickname contains invalid characters! Only allowed: §4" + allowedCharactersInNickRegex);
            return true;
        }
        if (!allowDuplicateNicknames && isNicknameAlreadyUsed(input)) {
            player.sendMessage(messageNicknameAlreadyUsed);
            return true;
        }
        setPlayerNick(player, input);
        return true;
    }

    private boolean isNicknameAlreadyUsed(String nick) {
        for (String existingNick : playerNicks.values()) {
            if (existingNick.equalsIgnoreCase(nick)) {
                return true;
            }
        }
        return false;
    }

    private void setPlayerNick(Player player, String nick) {
        String formattedNick = allowColoredNick ? replaceAmpersandWithSectionSign(nick) : nick;
        playerNicks.put(player.getName(), formattedNick);
        updatePlayerDisplayName(player);
        String displayNick = formattedNick;
        if ("§o".equals(fakeNicknameCharacter)) {
            displayNick = "§o" + formattedNick + "§r";
        } else {
            displayNick = fakeNicknameCharacter + formattedNick;
        }
        player.sendMessage(messageNickSuccess.replace("{nick}", displayNick));
    }

    private void clearPlayerNick(Player player) {
        playerNicks.remove(player.getName());
        updatePlayerDisplayName(player);
        player.sendMessage(messageNickCleared);
    }

    private boolean handleRealNameCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essentialschat.nick.realname")) {
            sender.sendMessage("§c%commands.generic.permission");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(messageRealNameUsage);
            return true;
        }
        String input = args[0];
        Player target = getServer().getPlayer(input);
        if (target == null) {
            for (Map.Entry<String, String> entry : playerNicks.entrySet()) {
                String realName = entry.getKey();
                String fakeNick = entry.getValue();
                if (fakeNick.equalsIgnoreCase(input)) {
                    target = getServer().getPlayer(realName);
                    break;
                }
            }
        }
        if (target == null) {
            sender.sendMessage(messageRealNameNotFound);
            return true;
        }
        String realName = target.getName();
        String displayName = getPlayerDisplayName(target);
        sender.sendMessage(messageRealNameOutput
                .replace("{player}", displayName)
                .replace("{nick}", realName));
        return true;
    }

    private String replaceAmpersandWithSectionSign(String input) {
        return input.replace('&', '§');
    }

    public List<String> getNicknameBlacklist() {
        return nicknameBlacklist;
    }

    public List<String> getPrefixBlacklist() {
        return prefixBlacklist;
    }
}
