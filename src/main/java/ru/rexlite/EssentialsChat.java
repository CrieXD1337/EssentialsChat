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

import java.util.Timer;
import java.util.TimerTask;

public class EssentialsChat extends PluginBase implements Listener {

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
    private String messagePermissionDenied;
    private String messageInvalidProvider;
    private String messagePrefixLengthError;
    private String messagePrefixInvalidCharacters;
    private String messageCommandOnlyForPlayers;

    @Override
    public void onEnable() {

        this.getServer().getPluginManager().registerEvents(this, this);

        saveResource("config.yml", true);

        // Загрузка конфига
        reloadConfig();
        Config config = getConfig();

        getLogger().info("Config loaded: " + config.getAll());
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
            opNicknameColor = "4"; // Default value
        }
        prefixMaxCharacters = config.getInt("prefix-max-characters", 15);
        prefixMinCharacters = config.getInt("prefix-min-characters", 3);
        allowedCharactersRegex = config.getString("allowed-characters", "A-Za-z0-9_-");
        messagePrefixCleared = config.getString("messages.prefix-cleared", "§7> §fYour prefix was §ccleared");
        messagePrefixSet = config.getString("messages.prefix-set", "§7> §fYour prefix successfully moved to: §b{prefix}");
        messageInvalidUsage = config.getString("messages.invalid-usage", "§7> §cUsage: /prefix <prefix|off>");
        messagePermissionDenied = config.getString("messages.permission-denied", "§c%commands.generic.permission");
        messageInvalidProvider = config.getString("messages.invalid-provider", "§7> §cProvider §4{provider} §cis currently not available for prefixes. Use §4LuckPerms.");
        messagePrefixLengthError = config.getString("messages.prefix-length-error", "§7> §cThe prefix must be between §4{min}§c and §4{max}§c characters.");
        messagePrefixInvalidCharacters = config.getString("messages.prefix-invalid-characters", "§cPrefix contains invalid characters! Only allowed: §4{allowed}");
        messageCommandOnlyForPlayers = config.getString("messages.command-only-for-players", "§cAllowed only for players!");
        startUpdateTimer();

        getLogger().info("§2EssentialsChat enabled! Provider: " + provider.getClass().getSimpleName());
        getLogger().info("§f");
        getLogger().info("§2Plugin from: https://cloudburstmc.org/resources/essentialschat.1062/");
    }

    @Override
    public void onDisable() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();
        String prefix = provider.getPrefix(player);
        String suffix = provider.getSuffix(player);
        String formattedMessage;
        if (message.startsWith(globalChatSymbol)) {
            formattedMessage = formatMessage(globalChatFormat, player, message.substring(globalChatSymbol.length()).trim());
        } else {
            formattedMessage = formatMessage(localChatFormat, player, message);
        }

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
            player.sendMessage(messagePermissionDenied);
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

    private String replaceAmpersandWithSectionSign(String input) {
        return input.replace('&', '§');
    }
}
