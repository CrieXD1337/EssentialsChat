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

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import me.criex.essentialschat.EssentialsChat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Message {
    private final EssentialsChat plugin;
    private final Properties messages;
    private final String language;

    public Message(EssentialsChat plugin) {
        this.plugin = plugin;
        this.language = plugin.getConfig().getString("language", "default");
        this.messages = new Properties();
        loadMessages();
    }

    private void loadMessages() {
        String langFile;
        if (language.equals("default")) {
            String serverLang = plugin.getServer().getLanguage().getLang();
            switch (serverLang.toLowerCase()) {
                case "rus":
                    langFile = "rus.lang";
                    break;
                case "zho":
                    langFile = "zho.lang";
                    break;
                default:
                    langFile = "eng.lang";
                    break;
            }
        } else {
            switch (language.toLowerCase()) {
                case "rus":
                    langFile = "rus.lang";
                    break;
                case "zho":
                    langFile = "zho.lang";
                    break;
                default:
                    langFile = "eng.lang";
                    break;
            }
        }
        File file = new File(plugin.getDataFolder() + "/lang/" + langFile);
        if (!file.exists()) {
            plugin.saveResource("lang/" + langFile);
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            messages.load(new java.io.InputStreamReader(fis, StandardCharsets.UTF_8));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load language file " + langFile + ": " + e.getMessage());
        }
    }

    public void send(Player player, String key, String... replacements) {
        String message = messages.getProperty(key, "Message not found: " + key);
        message = TextFormat.colorize(message);
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        player.sendMessage(message);
    }

    public void send(CommandSender sender, String key, String... replacements) {
        String message = messages.getProperty(key, "Message not found: " + key);
        message = TextFormat.colorize(message);
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        sender.sendMessage(message);
    }
}