package me.criex.essentialschat.providers;

import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.Config;
import me.criex.essentialschat.EssentialsChat;

public class ProviderSelector {
    public static PrefixSuffixProvider selectProvider(EssentialsChat plugin, Config config) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        boolean hasLuckPerms = pluginManager.getPlugin("LuckPerms") != null;
        boolean hasMultipass = pluginManager.getPlugin("Multipass") != null;
        String configProvider = config.getString("provider", "luckperms").toLowerCase();

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("§b[DEBUG] Selecting provider: LuckPerms=" + hasLuckPerms + ", Multipass=" + hasMultipass + ", ConfigProvider=" + configProvider);
        }

        // Only one plugin available
        if (hasLuckPerms && !hasMultipass) {
            if (!configProvider.equals("luckperms")) {
                config.set("provider", "luckperms");
                config.save();
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] Auto-set provider to LuckPerms in config");
                }
            }
            return new LProvider();
        } else if (hasMultipass && !hasLuckPerms) {
            if (!configProvider.equals("multipass")) {
                config.set("provider", "multipass");
                config.save();
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] Auto-set provider to Multipass in config");
                }
            }
            return new MultipassProvider();
        } else if (!hasLuckPerms && !hasMultipass) {
            if (!configProvider.equals("fallback")) {
                config.set("provider", "fallback");
                config.save();
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("§b[DEBUG] Auto-set provider to Fallback in config");
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