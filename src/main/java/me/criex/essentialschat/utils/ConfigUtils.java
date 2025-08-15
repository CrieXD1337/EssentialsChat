package me.criex.essentialschat.utils;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import me.criex.essentialschat.EssentialsChat;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigUtils {
    private final EssentialsChat main;
    private final Config config;
    private final Config messagesConfig;

    // Settings
    public List<String> nicknameBlacklist;
    public List<String> prefixBlacklist;
    public boolean debug;
    public int localChatRadius;
    public String globalChatSymbol;
    public String localChatFormat;
    public String globalChatFormat;
    public String defaultChatFormat;
    public String singleChatFormat;
    public String singleChatType;
    public int formatMethod;
    public String opNicknameColor;
    public boolean allowColoredNick;
    public String fakeNicknameCharacter;
    public boolean allowDuplicateNicknames;
    public boolean prefixInSettingsAndHeadEnabled;
    public String prefixInSettingsAndHeadFormat;
    public int prefixMaxCharacters;
    public int prefixMinCharacters;
    public int maxNickCharacters;
    public int minNickCharacters;
    public String allowedCharactersRegex;
    public String allowedCharactersInNickRegex;
    public String repetitionPunishment;
    public String punishmentCommand;
    public boolean chatFilterEnabled;
    public int chatCooldown;
    public int chatMaxChars;
    public int chatMaxRepeat;

    // Messages
    public String msgPrefixSet;
    public String msgPrefixCleared;
    public String msgInvalidProvider;
    public String msgPrefixBlackList;
    public String msgNickSuccess;
    public String msgNickCleared;
    public String msgNickBlackList;
    public String msgNickUsed;
    public String msgNickUsage;
    public String msgRealNameUsage;
    public String msgRealNameOutput;
    public String msgRealNameNotFound;
    public String msgPrefixLengthError;
    public String msgPrefixInvalidChars;
    public String msgCmdOnlyForPlayers;
    public String msgPrefixUsage;
    public String msgCooldown;
    public String msgTooLong;
    public String msgTooManyRepeat;
    public String msgNickMaxLimit;
    public String msgNickAllowedCharacters;

    public ConfigUtils(EssentialsChat main) {
        this.main = main;
        this.main.saveDefaultConfig();
        this.main.saveResource("messages.yml");
        this.config = main.getConfig();
        this.messagesConfig = new Config(new File(main.getDataFolder(), "messages.yml"), Config.YAML);
        loadConfig();
    }

    private void loadConfig() {
        // Blacklist reading
        nicknameBlacklist = config.getStringList("nick.nicknames-blacklist").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        prefixBlacklist = config.getStringList("prefix.prefixes-blacklist").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        // General settings
        debug = config.getBoolean("debug", false);

        // Chat format settings
        formatMethod = config.getInt("chat-formatting.format-method", 1);
        localChatRadius = config.getInt("chat-formatting.local-chat-radius", 100);
        globalChatSymbol = TextFormat.colorize(config.getString("chat-formatting.global-chat-symbol", "!"));
        localChatFormat = TextFormat.colorize(config.getString("chat-formatting.local-chat-format", "§7[§aL§7] §r{prefix}§r {player}{suffix}§r §a» §8{msg}"));
        globalChatFormat = TextFormat.colorize(config.getString("chat-formatting.global-chat-format", "§7[§4G§7] §r{prefix}§r {player}{suffix}§r §a» §f{msg}"));
        singleChatType = config.getString("chat-formatting.single-chat-type", "global");
        singleChatFormat = singleChatType.equalsIgnoreCase("local") ? localChatFormat : globalChatFormat;
        defaultChatFormat = TextFormat.colorize(config.getString("chat-formatting.default-chat-format", "§r<{prefix}§r{player}{suffix}§r> §f{msg}"));

        // Prefix settings
        prefixInSettingsAndHeadEnabled = config.getBoolean("prefix.prefix-in-settings-and-head.enabled", false);
        prefixInSettingsAndHeadFormat = TextFormat.colorize(config.getString("prefix.prefix-in-settings-and-head.format"));
        prefixMaxCharacters = config.getInt("prefix.max-characters-in-prefix", 15);
        prefixMinCharacters = config.getInt("prefix.min-characters-in-prefix", 3);
        allowedCharactersRegex = config.getString("prefix.allowed-characters", "A-Za-z0-9_-");

        // Nick settings
        maxNickCharacters = config.getInt("nick.max-characters-length", 15);
        minNickCharacters = config.getInt("nick.min-characters-length", 3);
        allowedCharactersInNickRegex = config.getString("nick.allowed-characters-in-nick", "A-Za-z0-9_-");
        allowColoredNick = config.getBoolean("nick.allow-colored-nick", false);
        allowDuplicateNicknames = config.getBoolean("nick.allow-duplicate-nicknames", false);
        fakeNicknameCharacter = config.getString("nick.fake-nickname-character", "italic");
        if ("italic".equalsIgnoreCase(fakeNicknameCharacter)) {
            fakeNicknameCharacter = "§o";
        } else if ("bold".equalsIgnoreCase(fakeNicknameCharacter)) {
            fakeNicknameCharacter = "§l";
        } else {
            fakeNicknameCharacter = TextFormat.colorize(fakeNicknameCharacter.replace('&', '§'));
        }
        opNicknameColor = config.getString("nick.op-nickname-color", "c");
        if (!opNicknameColor.matches("[0-9a-f]")) {
            main.getLogger().warning("Invalid OP color. Default set: 4");
            opNicknameColor = "4";
        }

        // Chat filtering settings
        chatFilterEnabled = config.getBoolean("chat-filtering.enabled", true);
        chatCooldown = config.getInt("chat-filtering.cooldown-for-messages", 3);
        chatMaxChars = config.getInt("chat-filtering.max-message-characters", 200);
        chatMaxRepeat = config.getInt("chat-filtering.max-messages-repetition", 5);
        repetitionPunishment = config.getString("chat-filtering.repetition-punishment", "Message");
        punishmentCommand = TextFormat.colorize(config.getString("chat-filtering.punishment-command", ""));

        // Validate repetition-punishment
        if (!List.of("kick", "message", "command").contains(repetitionPunishment.toLowerCase())) {
            main.getLogger().warning("§eInvalid repetition-punishment value: " + repetitionPunishment + ". Defaulting to 'Message'.");
            repetitionPunishment = "Message";
        }
        if (repetitionPunishment.equalsIgnoreCase("command") && punishmentCommand.isEmpty()) {
            main.getLogger().warning("§ePunishment command is empty for 'Command' repetition-punishment. Defaulting to 'Message'.");
            repetitionPunishment = "Message";
        }

        // Messages
        msgPrefixSet = TextFormat.colorize(messagesConfig.getString("prefix-set", "§7> §fYour prefix successfully moved to: §b{prefix}"));
        msgPrefixCleared = TextFormat.colorize(messagesConfig.getString("prefix-cleared", "§7> §fYour prefix was §ccleared"));
        msgInvalidProvider = TextFormat.colorize(messagesConfig.getString("invalid-provider", "§7> §cProvider §4{provider} §cis currently not available for prefixes. Use §4LuckPerms."));
        msgPrefixBlackList = TextFormat.colorize(messagesConfig.getString("prefix-in-blacklist", "§7> §cThis prefix is banned!"));
        msgNickSuccess = TextFormat.colorize(messagesConfig.getString("nick-success", "§7> §fYour nickname changed to §b{nick}"));
        msgNickCleared = TextFormat.colorize(messagesConfig.getString("nick-cleared", "§7> §fYour nickname §ccleared"));
        msgNickBlackList = TextFormat.colorize(messagesConfig.getString("nick-in-blacklist", "§7> §cThis nickname is banned!"));
        msgNickUsed = TextFormat.colorize(messagesConfig.getString("nick-used", "§7> §cThis nickname is already in use!"));
        msgNickUsage = TextFormat.colorize(messagesConfig.getString("nick-usage", "§7> §cUsage: §e/nick <nick>"));
        msgRealNameUsage = TextFormat.colorize(messagesConfig.getString("realname-usage", "§7> §cUsage: §e/realname <player>"));
        msgRealNameOutput = TextFormat.colorize(messagesConfig.getString("realname-output", "§7> §fReal name of player §b{player}: §3{nick}"));
        msgRealNameNotFound = TextFormat.colorize(messagesConfig.getString("realname-not-found", "§7> §cPlayer not found!"));
        msgPrefixLengthError = TextFormat.colorize(messagesConfig.getString("prefix-length-error", "§7> §cThe prefix must be between §4{min}§c and §4{max}§c characters."));
        msgPrefixInvalidChars = TextFormat.colorize(messagesConfig.getString("prefix-invalid-characters", "§cPrefix contains invalid characters! Only allowed: §4{allowed}"));
        msgCmdOnlyForPlayers = TextFormat.colorize(messagesConfig.getString("command-only-for-players", "§cAllowed only for players!"));
        msgPrefixUsage = TextFormat.colorize(messagesConfig.getString("invalid-usage", "§7> §cUsage: §e/prefix <prefix|off>"));
        msgCooldown = TextFormat.colorize(messagesConfig.getString("cooldown-for-messages", "§7> §cWait §b{seconds} §cseconds"));
        msgTooLong = TextFormat.colorize(messagesConfig.getString("max-message-characters", "§7> §cMaximum characters in message - §b{max}§c!"));
        msgTooManyRepeat = TextFormat.colorize(messagesConfig.getString("max-messages-repetition", "§7> §cYou are sending the same message too many times!"));
        msgNickMaxLimit = TextFormat.colorize(messagesConfig.getString("nick-characters-limit", "§7> §cNickname must be from §b{min}§c to §b{max} §ccharacters!"));
        msgNickAllowedCharacters = TextFormat.colorize(messagesConfig.getString("nick-allowed-characters", "§cInvalid characters! Allowed: §4{allowed}"));
    }

    public void reloadConfig() {
        main.reloadConfig();
        messagesConfig.reload();
        loadConfig();
    }
}