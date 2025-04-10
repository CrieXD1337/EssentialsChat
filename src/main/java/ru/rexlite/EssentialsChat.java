package ru.rexlite;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import ru.rexlite.providers.PrefixSuffixProvider;
import ru.rexlite.providers.LProvider;
import ru.rexlite.providers.MultipassProvider;

public class EssentialsChat extends PluginBase implements Listener {

    private int localChatRadius;
    private String globalChatSymbol;
    private String localChatFormat;
    private String globalChatFormat;
    private PrefixSuffixProvider provider;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);

        saveDefaultConfig();
        reloadConfig();

        Config config = getConfig();
        localChatRadius = config.getInt("local-chat-radius", 100);
        globalChatSymbol = config.getString("global-chat-symbol", "!");
        localChatFormat = config.getString("local-chat-format", "§7[§aL§7] §7[{prefix}§r§7] §f{player}{suffix} §a» §8{msg}");
        globalChatFormat = config.getString("global-chat-format", "§7[§4G§7] §7[{prefix}§r§7] §f{player}{suffix} §a» §f{msg}");

        String providerName = config.getString("provider", "LuckPerms");
        switch (providerName.toLowerCase()) {
            case "luckperms":
                provider = new LProvider();
                break;
            case "multipass":
                provider = new MultipassProvider();
                break;
            default:
                getLogger().error("§c[LP§4Chat] §cUknown provider:§4 " + providerName + ". §cThe provider has been reset to §4LuckPerms.");
                provider = new LProvider();
                break;
        }

        getLogger().info("§2EssentialsChat enabled! Provider: §a" + providerName);
        getLogger().info("§f");
        getLogger().info("§2Plugin from: §ahttps://cloudburstmc.org/resources/essentialschat.1062/");
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        String prefix = provider.getPrefix(player);
        String suffix = provider.getSuffix(player);

        // Определение типа чата
        if (message.startsWith(globalChatSymbol)) {
            // Глобальный чат
            String formattedMessage = globalChatFormat
                    .replace("{prefix}", prefix)
                    .replace("{player}", player.getName())
                    .replace("{suffix}", suffix)
                    .replace("{msg}", message.substring(globalChatSymbol.length()).trim());

            event.setCancelled(true); // Отменяем стандартное событие чата
            for (Player onlinePlayer : getServer().getOnlinePlayers().values()) {
                onlinePlayer.sendMessage(formattedMessage);
            }
        } else {
            // Локальный чат
            String formattedMessage = localChatFormat
                    .replace("{prefix}", prefix)
                    .replace("{player}", player.getName())
                    .replace("{suffix}", suffix)
                    .replace("{msg}", message);

            event.setCancelled(true); // Отменяем стандартное событие чата
            for (Player onlinePlayer : getServer().getOnlinePlayers().values()) {
                if (onlinePlayer.distance(player) <= localChatRadius) {
                    onlinePlayer.sendMessage(formattedMessage);
                }
            }
        }
    }
}
