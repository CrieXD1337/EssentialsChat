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
package me.criex.essentialschat.providers;

import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.Config;
import me.criex.essentialschat.EssentialsChat;

import java.io.File;

public class ProviderSelector {
    public static PrefixSuffixProvider selectProvider(EssentialsChat plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        File providerFile = new File(plugin.getDataFolder(), "provider.yml");
        Config providerConfig = new Config(providerFile, Config.YAML);
        boolean hasLuckPerms = pluginManager.getPlugin("LuckPerms") != null;
        boolean hasMultipass = pluginManager.getPlugin("Multipass") != null;
        String configProvider = providerConfig.getString("provider", "luckperms").toLowerCase();

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] Selecting provider: LuckPerms=" + hasLuckPerms + ", Multipass=" + hasMultipass + ", ConfigProvider=" + configProvider);
        }

        // Only one plugin available
        if (hasLuckPerms && !hasMultipass) {
            if (!configProvider.equals("luckperms")) {
                providerConfig.set("provider", "luckperms");
                providerConfig.save();
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] Auto-set provider to LuckPerms in provider.yml");
                }
            }
            return new LProvider();
        } else if (hasMultipass && !hasLuckPerms) {
            if (!configProvider.equals("multipass")) {
                providerConfig.set("provider", "multipass");
                providerConfig.save();
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] Auto-set provider to Multipass in provider.yml");
                }
            }
            return new MultipassProvider();
        } else if (!hasLuckPerms && !hasMultipass) {
            if (!configProvider.equals("fallback")) {
                providerConfig.set("provider", "fallback");
                providerConfig.save();
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] Auto-set provider to Fallback in provider.yml");
                }
            }
            return new FallbackProvider();
        }

        // Both plugins available, use config preference
        switch (configProvider) {
            case "multipass":
                return new MultipassProvider();
            case "fallback":
                return new FallbackProvider();
            case "luckperms":
            default:
                return new LProvider();
        }
    }
}