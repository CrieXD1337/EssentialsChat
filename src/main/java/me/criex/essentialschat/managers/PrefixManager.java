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
package me.criex.essentialschat.managers;

import cn.nukkit.Player;
import ru.nukkit.multipass.Multipass;
import me.criex.essentialschat.EssentialsChat;
import me.criex.essentialschat.providers.LProvider;
import me.criex.essentialschat.providers.MultipassProvider;
import me.criex.essentialschat.providers.PrefixSuffixProvider;
import me.criex.essentialschat.utils.ConfigUtils;

public class PrefixManager {
    private final EssentialsChat plugin;
    private final PrefixSuffixProvider provider;
    private final ConfigUtils configUtils;

    public PrefixManager(EssentialsChat plugin, PrefixSuffixProvider provider, ConfigUtils configUtils) {
        this.plugin = plugin;
        this.provider = provider;
        this.configUtils = configUtils;
    }

    public void setPlayerPrefix(Player player, String prefix) {
        if (configUtils.debug) {
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
                            if (configUtils.debug) {
                                plugin.getLogger().info("§b[DEBUG] Successfully set LuckPerms prefix for player: " + player.getName() + ", prefix: " + formattedPrefix);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to set LuckPerms prefix: " + e.getMessage());
                            if (configUtils.debug) {
                                plugin.getLogger().info("§b[DEBUG] Error setting LuckPerms prefix for player: " + player.getName() + ", error: " + e.getMessage());
                            }
                        }
                    }
                });
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                plugin.getLogger().warning("LuckPerms not found when trying to set prefix!");
                if (configUtils.debug) {
                    plugin.getLogger().info("§b[DEBUG] LuckPerms not found for player: " + player.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error setting LuckPerms prefix: " + e.getMessage());
                if (configUtils.debug) {
                    plugin.getLogger().info("§b[DEBUG] General error setting LuckPerms prefix for player: " + player.getName() + ", error: " + e.getMessage());
                }
            }
        } else if (provider instanceof MultipassProvider) {
            try {
                Multipass.setPlayerPrefix(player, formattedPrefix, null, 100);
                if (configUtils.debug) {
                    plugin.getLogger().info("§b[DEBUG] Successfully set Multipass prefix for player: " + player.getName() + ", prefix: " + formattedPrefix);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to set prefix with Multipass: " + e.getMessage());
                if (configUtils.debug) {
                    plugin.getLogger().info("§b[DEBUG] Error setting Multipass prefix for player: " + player.getName() + ", error: " + e.getMessage());
                }
            }
        }
    }

    public void clearPlayerPrefix(Player player) {
        if (configUtils.debug) {
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
                            if (configUtils.debug) {
                                plugin.getLogger().info("§b[DEBUG] Successfully cleared LuckPerms prefix for player: " + player.getName());
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to clear LuckPerms prefix: " + e.getMessage());
                            if (configUtils.debug) {
                                plugin.getLogger().info("§b[DEBUG] Error clearing LuckPerms prefix for player: " + player.getName() + ", error: " + e.getMessage());
                            }
                        }
                    }
                });
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                plugin.getLogger().warning("LuckPerms not found when trying to clear prefix!");
                if (configUtils.debug) {
                    plugin.getLogger().info("§b[DEBUG] LuckPerms not found when clearing prefix for player: " + player.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error clearing LuckPerms prefix: " + e.getMessage());
                if (configUtils.debug) {
                    plugin.getLogger().info("§b[DEBUG] General error clearing LuckPerms prefix for player: " + player.getName() + ", error: " + e.getMessage());
                }
            }
        } else if (provider instanceof MultipassProvider) {
            try {
                Multipass.setPlayerPrefix(player, "", null, 100);
                if (configUtils.debug) {
                    plugin.getLogger().info("§b[DEBUG] Successfully cleared Multipass prefix for player: " + player.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to clear prefix with Multipass: " + e.getMessage());
                if (configUtils.debug) {
                    plugin.getLogger().info("§b[DEBUG] Error clearing Multipass prefix for player: " + player.getName() + ", error: " + e.getMessage());
                }
            }
        }
    }
}