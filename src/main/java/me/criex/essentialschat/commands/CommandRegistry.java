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
package me.criex.essentialschat.commands;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.utils.Config;
import me.criex.essentialschat.EssentialsChat;
import me.criex.essentialschat.commands.impl.NickCommand;
import me.criex.essentialschat.commands.impl.PrefixCommand;
import me.criex.essentialschat.commands.impl.RealNameCommand;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private final EssentialsChat plugin;
    private final Map<String, BaseCommand> commands;

    public CommandRegistry(EssentialsChat plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        loadCommands();
    }

    private void loadCommands() {
        File commandsFile = new File(plugin.getDataFolder(), "commands.yml");
        if (!commandsFile.exists()) {
            plugin.saveResource("commands.yml");
        }
        Config config = new Config(commandsFile, Config.YAML);

        // Register commands
        if (config.exists("prefix")) {
            commands.put("prefix", new PrefixCommand(plugin, config.getSection("prefix")));
        }
        if (config.exists("nick")) {
            commands.put("nick", new NickCommand(plugin, config.getSection("nick")));
        }
        if (config.exists("realname")) {
            commands.put("realname", new RealNameCommand(plugin, config.getSection("realname")));
        }

        // Register commands with the server
        for (BaseCommand command : commands.values()) {
            Server.getInstance().getCommandMap().register("EssentialsChat", command);
        }

        if (plugin.getConfigUtils().isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Registered " + commands.size() + " commands.");
        }
    }

    public BaseCommand getCommand(String name) {
        return commands.get(name);
    }
}