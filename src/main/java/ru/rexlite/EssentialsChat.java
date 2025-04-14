package ru.rexlite;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
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
    private boolean prefixInSettingsAndHeadEnabled;
    private String prefixInSettingsAndHeadFormat;

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
        prefixInSettingsAndHeadEnabled = config.getBoolean("prefix-in-settings-and-head.enabled", false);
        prefixInSettingsAndHeadFormat = config.getString("prefix-in-settings-and-head.format", "[{prefix}] {player}{suffix}");

        String providerName = config.getString("provider", "LuckPerms");
        switch (providerName.toLowerCase()) {
            case "luckperms":
                provider = new LProvider();
                break;
            case "multipass":
                provider = new MultipassProvider();
                break;
            default:
                getLogger().error("§c[Essentials§4Chat] §cUnknown provider:§4 " + providerName + ". §cThe provider has been reset to §4LuckPerms.");
                provider = new LProvider();
                break;
        }

        getLogger().info("§2EssentialsChat enabled! Provider: " + providerName);
        getLogger().info("§f");
        getLogger().info("§2Plugin from: https://cloudburstmc.org/resources/essentialschat.1062/");
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();

        String prefix = provider.getPrefix(player);
        String suffix = provider.getSuffix(player);

        String formattedMessage;
        if (message.startsWith(globalChatSymbol)) {
            formattedMessage = formatMessage(globalChatFormat, player, message.substring(globalChatSymbol.length()).trim());
        } else {
            formattedMessage = formatMessage(localChatFormat, player, message);
        }

        event.setFormat(formattedMessage);

        if (!message.startsWith(globalChatSymbol)) {
            event.getRecipients().removeIf(onlinePlayer -> {
                if (!(onlinePlayer instanceof Player)) {
                    return true;
                }
                Player playerInList = (Player) onlinePlayer;
                return playerInList.distance(player) > localChatRadius;
            });
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updatePlayerDisplayName(player);
    }

    private String formatMessage(String format, Player player, String message) {
        return format.replace("{prefix}", provider.getPrefix(player))
                .replace("{player}", player.getName())
                .replace("{suffix}", provider.getSuffix(player))
                .replace("{msg}", message);
    }

    private void updatePlayerDisplayName(Player player) {
        if (!prefixInSettingsAndHeadEnabled) {
            return;
        }

        String prefix = provider.getPrefix(player);
        String suffix = provider.getSuffix(player);

        String displayName = prefixInSettingsAndHeadFormat
                .replace("{prefix}", prefix)
                .replace("{player}", player.getName())
                .replace("{suffix}", suffix);

        player.setDisplayName(displayName);
        player.setNameTag(displayName);
    }
}
