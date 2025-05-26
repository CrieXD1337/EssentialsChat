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
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import ru.rexlite.EssentialsChat;
import ru.rexlite.providers.LProvider;
import ru.rexlite.providers.PrefixSuffixProvider;

import java.util.concurrent.CompletableFuture;

public class PrefixManager {
    private final EssentialsChat plugin;
    private final PrefixSuffixProvider provider;

    public PrefixManager(EssentialsChat plugin, PrefixSuffixProvider provider) {
        this.plugin = plugin;
        this.provider = provider;
    }

    public void setPlayerPrefix(Player player, String prefix) {
        if (provider instanceof LProvider) {
            LuckPerms api = LuckPermsProvider.get();
            String formattedPrefix = prefix.replace("&", "§");
            CompletableFuture<User> userFuture = api.getUserManager().loadUser(player.getUniqueId());
            userFuture.thenAccept(user -> {
                if (user != null) {
                    Node prefixNode = PrefixNode.builder(formattedPrefix, 126483).build();
                    user.data().add(prefixNode);
                    api.getUserManager().saveUser(user);
                }
            });
        }
    }

    public void clearPlayerPrefix(Player player) {
        if (provider instanceof LProvider) {
            LuckPerms api = LuckPermsProvider.get();
            CompletableFuture<User> userFuture = api.getUserManager().loadUser(player.getUniqueId());
            userFuture.thenAccept(user -> {
                if (user != null) {
                    user.data().clear(NodeType.PREFIX::matches);
                    api.getUserManager().saveUser(user);
                }
            });
        }
    }
}
