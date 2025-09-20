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

package me.criex.essentialschat.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import me.criex.essentialschat.EssentialsChat;
import me.criex.essentialschat.managers.DisplayManager;
import me.criex.essentialschat.utils.ConfigUtils;
import me.criex.essentialschat.utils.Message;

public class PlayerListener implements EventListener, Listener {
    private final EssentialsChat plugin;
    private final Message message;
    private final ConfigUtils configUtils;
    private final DisplayManager displayManager;

    public PlayerListener(EssentialsChat plugin) {
        this.plugin = plugin;
        this.message = plugin.getMessage();
        this.configUtils = plugin.getConfigUtils();
        this.displayManager = plugin.getDisplayManager();
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String messageText = event.getMessage();
        String name = player.getName();

        if (plugin.getConfigUtils().isChatFilterEnabled()) {
            long now = System.currentTimeMillis();
            long lastTime = plugin.getLastMessageTime().getOrDefault(name, 0L);
            long cooldownMillis = plugin.getConfigUtils().getChatCooldown() * 1000L;
            long timeLeftMillis = cooldownMillis - (now - lastTime);
            if (timeLeftMillis > 0) {
                double timeLeftSeconds = Math.ceil(timeLeftMillis / 1000.0);
                message.send(player, "cooldown-for-messages", "{seconds}", String.valueOf(timeLeftSeconds));
                event.setCancelled(true);
                return;
            }
            if (messageText.length() > plugin.getConfigUtils().getChatMaxChars()) {
                message.send(player, "max-message-characters", "{max}", String.valueOf(plugin.getConfigUtils().getChatMaxChars()));
                event.setCancelled(true);
                return;
            }
            String lastMsg = plugin.getLastPlayerMessage().getOrDefault(name, "");
            int repeatCount = messageText.equalsIgnoreCase(lastMsg)
                    ? plugin.getMessageRepetitionCount().getOrDefault(name, 1) + 1
                    : 1;
            if (repeatCount > plugin.getConfigUtils().getChatMaxRepeat()) {
                event.setCancelled(true);
                switch (plugin.getConfigUtils().getRepetitionPunishment().toLowerCase()) {
                    case "kick":
                        message.send(player, "max-messages-repetition");
                        player.kick("You are sending the same message too many times!", false);
                        break;
                    case "command":
                        if (!plugin.getConfigUtils().getPunishmentCommand().isEmpty()) {
                            String command = plugin.getConfigUtils().getPunishmentCommand().replace("{player}", player.getName());
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
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
            plugin.getLastMessageTime().put(name, now);
            plugin.getLastPlayerMessage().put(name, messageText);
            plugin.getMessageRepetitionCount().put(name, repeatCount);
        }

        String formatted;
        switch (plugin.getConfigUtils().getFormatMethod()) {
            case 2: // Single format
                formatted = plugin.getChatManager().formatChatMessage(player, plugin.getConfigUtils().getSingleChatFormat(), messageText);
                if (plugin.getConfigUtils().getSingleChatType().equalsIgnoreCase("local")) {
                    event.getRecipients().removeIf(p -> p instanceof Player &&
                            ((Player) p).distance(player) > plugin.getConfigUtils().getLocalChatRadius());
                }
                break;
            case 3: // Default Minecraft format
                formatted = plugin.getChatManager().formatChatMessage(player, plugin.getConfigUtils().getDefaultChatFormat(), messageText);
                break;
            case 1: // L/G chat
            default:
                if (messageText.startsWith(plugin.getConfigUtils().getGlobalChatSymbol())) {
                    formatted = plugin.getChatManager().formatChatMessage(player, plugin.getConfigUtils().getGlobalChatFormat(), messageText.substring(plugin.getConfigUtils().getGlobalChatSymbol().length()).trim());
                } else {
                    formatted = plugin.getChatManager().formatChatMessage(player, plugin.getConfigUtils().getLocalChatFormat(), messageText);
                    event.getRecipients().removeIf(p -> p instanceof Player &&
                            ((Player) p).distance(player) > plugin.getConfigUtils().getLocalChatRadius());
                }
                break;
        }
        formatted = plugin.parsePlaceholders(player, formatted);
        event.setFormat(formatted);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getDisplayManager().updateDisplay(event.getPlayer());
        Player player = event.getPlayer();
        displayManager.updateDisplay(player);

        if (configUtils.isWelcomeMessageEnabled()) {
            String message = configUtils.getWelcomeMessageText().replace("{player}", player.getName());
            String type = configUtils.getWelcomeMessageType();

            switch (type) {
                case "message":
                    player.sendMessage(message);
                    break;
                case "title":
                    player.sendTitle(message, "", 20, 40, 20);
                    break;
                case "actionbar":
                    player.sendActionBar(message);
                    break;
            }
            if (configUtils.isDebug()) {
                plugin.getLogger().info("§b[DEBUG] Sent welcome message to " + player.getName() + ": " + message + " (type: " + type + ")");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String message;
        String type;
        boolean enabled;

        if (player.getKiller() instanceof Player && configUtils.isDeathMessageEnabled()) {
            Player killer = (Player) player.getKiller();
            message = configUtils.getDeathMessageText()
                    .replace("{player}", player.getName())
                    .replace("{killer}", killer.getName());
            type = configUtils.getDeathMessageType();
            enabled = true;
        } else if (configUtils.isDefaultDeathMessageEnabled()) {
            message = configUtils.getDefaultDeathMessageText().replace("{player}", player.getName());
            type = configUtils.getDefaultDeathMessageType();
            enabled = true;
        } else {
            return;
        }

        if (enabled) {
            switch (type) {
                case "message":
                    plugin.getServer().getOnlinePlayers().values().forEach(p -> p.sendMessage(message));
                    break;
                case "title":
                    plugin.getServer().getOnlinePlayers().values().forEach(p -> p.sendTitle(message, "", 20, 40, 20));
                    break;
                case "actionbar":
                    plugin.getServer().getOnlinePlayers().values().forEach(p -> p.sendActionBar(message));
                    break;
            }
            if (configUtils.isDebug()) {
                plugin.getLogger().info("§b[DEBUG] Sent death message for " + player.getName() + ": " + message + " (type: " + type + ")");
            }
        }
    }
}