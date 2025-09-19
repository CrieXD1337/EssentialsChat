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
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.Setter;
import me.criex.essentialschat.EssentialsChat;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ConfigUtils {
    private final EssentialsChat main;
    private final Config config;

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
    public boolean prefixNameTagEnabled;
    public String prefixNameTagFormat;
    public long prefixNameTagUpdate;
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
    public String language;

    public ConfigUtils(EssentialsChat main) {
        this.main = main;
        this.main.saveDefaultConfig();
        this.main.saveResource("lang/eng.lang");
        this.main.saveResource("lang/zho.lang");
        this.main.saveResource("lang/rus.lang");
        this.config = main.getConfig();
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
        language = config.getString("language", "default");

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
        prefixNameTagEnabled = config.getBoolean("prefix-nametag.enabled", false);
        prefixNameTagFormat = TextFormat.colorize(config.getString("prefix-nametag.format"));
        prefixNameTagUpdate = config.getLong("prefix-nametag.update", 20L);
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
    }

    public void reloadConfig() {
        main.reloadConfig();
        loadConfig();
    }
}