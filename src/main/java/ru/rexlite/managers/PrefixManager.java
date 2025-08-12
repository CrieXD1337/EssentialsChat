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
package ru.rexlite.managers;

import cn.nukkit.Player;
import ru.nukkit.multipass.Multipass;
import ru.rexlite.EssentialsChat;
import ru.rexlite.providers.LProvider;
import ru.rexlite.providers.MultipassProvider;
import ru.rexlite.providers.PrefixSuffixProvider;

public class PrefixManager {
    private final EssentialsChat plugin;
    private final PrefixSuffixProvider provider;

    public PrefixManager(EssentialsChat plugin, PrefixSuffixProvider provider) {
        this.plugin = plugin;
        this.provider = provider;
    }

    public void setPlayerPrefix(Player player, String prefix) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] Attempting to set prefix for player: " + player.getName() + ", prefix: " + prefix);
        }

        String formattedPrefix = prefix.replace("&", "§");
        if (provider instanceof LProvider) {
            try {
                Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
                Object api = luckPermsClass.getMethod("getProvider").invoke(null);
                Object userManager = luckPermsClass.getMethod("getUserManager").invoke(api);
                Object userFuture = userManager.getClass().getMethod("loadUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());
                ((java.util.concurrent.CompletableFuture<?>) userFuture).thenAccept(user -> {
                    if (user != null) {
                        try {
                            Class<?> prefixNodeClass = Class.forName("net.luckperms.api.node.types.PrefixNode");
                            Object prefixNode = prefixNodeClass.getMethod("builder", String.class, int.class)
                                    .invoke(null, formattedPrefix, 126483);
                            user.getClass().getMethod("data").invoke(user).getClass().getMethod("add", Class.forName("net.luckperms.api.node.Node")).invoke(user.getClass().getMethod("data").invoke(user), prefixNode);
                            userManager.getClass().getMethod("saveUser", user.getClass()).invoke(userManager, user);
                            if (plugin.isDebugEnabled()) {
                                plugin.getLogger().info("§b[DEBUG] Successfully set LuckPerms prefix for player: " + player.getName() + ", prefix: " + formattedPrefix);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to set LuckPerms prefix: " + e.getMessage());
                            if (plugin.isDebugEnabled()) {
                                plugin.getLogger().info("§b[DEBUG] Error setting LuckPerms prefix for player: " + player.getName() + ", error: " + e.getMessage());
                            }
                        }
                    }
                });
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                plugin.getLogger().warning("LuckPerms not found when trying to set prefix!");
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] LuckPerms not found for player: " + player.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error setting LuckPerms prefix: " + e.getMessage());
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] General error setting LuckPerms prefix for player: " + player.getName() + ", error: " + e.getMessage());
                }
            }
        } else if (provider instanceof MultipassProvider) {
            try {
                Multipass.setPlayerPrefix(player, formattedPrefix, null, 100);
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] Successfully set Multipass prefix for player: " + player.getName() + ", prefix: " + formattedPrefix);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to set prefix with Multipass: " + e.getMessage());
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] Error setting Multipass prefix for player: " + player.getName() + ", error: " + e.getMessage());
                }
            }
        }
    }

    public void clearPlayerPrefix(Player player) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] Attempting to clear prefix for player: " + player.getName());
        }

        if (provider instanceof LProvider) {
            try {
                Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
                Object api = luckPermsClass.getMethod("getProvider").invoke(null);
                Object userManager = luckPermsClass.getMethod("getUserManager").invoke(api);
                Object userFuture = userManager.getClass().getMethod("loadUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());
                ((java.util.concurrent.CompletableFuture<?>) userFuture).thenAccept(user -> {
                    if (user != null) {
                        try {
                            Object data = user.getClass().getMethod("data").invoke(user);
                            data.getClass().getMethod("clear", java.util.function.Predicate.class).invoke(data, (java.util.function.Predicate<?>) node -> {
                                try {
                                    return node.getClass().getMethod("getType").invoke(node).toString().equals("PREFIX");
                                } catch (Exception e) {
                                    return false;
                                }
                            });
                            userManager.getClass().getMethod("saveUser", user.getClass()).invoke(userManager, user);
                            if (plugin.isDebugEnabled()) {
                                plugin.getLogger().info("§b[DEBUG] Successfully cleared LuckPerms prefix for player: " + player.getName());
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to clear LuckPerms prefix: " + e.getMessage());
                            if (plugin.isDebugEnabled()) {
                                plugin.getLogger().info("§b[DEBUG] Error clearing LuckPerms prefix for player: " + player.getName() + ", error: " + e.getMessage());
                            }
                        }
                    }
                });
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                plugin.getLogger().warning("LuckPerms not found when trying to clear prefix!");
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] LuckPerms not found when clearing prefix for player: " + player.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error clearing LuckPerms prefix: " + e.getMessage());
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] General error clearing LuckPerms prefix for player: " + player.getName() + ", error: " + e.getMessage());
                }
            }
        } else if (provider instanceof MultipassProvider) {
            try {
                Multipass.setPlayerPrefix(player, "", null, 100);
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] Successfully cleared Multipass prefix for player: " + player.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to clear prefix with Multipass: " + e.getMessage());
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] Error clearing Multipass prefix for player: " + player.getName() + ", error: " + e.getMessage());
                }
            }
        }
    }
}