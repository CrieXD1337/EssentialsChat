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
package me.criex.essentialschat.commands.impl;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.ConfigSection;
import me.criex.essentialschat.EssentialsChat;
import me.criex.essentialschat.commands.BaseCommand;
import me.criex.essentialschat.utils.Message;

public class RealNameCommand extends BaseCommand {
    private final EssentialsChat plugin;
    private final Message message;

    public RealNameCommand(EssentialsChat plugin, ConfigSection data) {
        super("realname", data);
        this.plugin = plugin;
        this.message = plugin.getMessage();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (plugin.getConfigUtils().isDebug()) {
            plugin.getLogger().info("§b[DEBUG] Command executed: " + getName() + ", sender: " + sender.getName() + ", args: " + java.util.Arrays.toString(args));
        }

        if (args.length != 1) {
            message.send(sender, "realname-usage");
            return true;
        }

        String real = plugin.getNickManager().getRealName(args[0]);
        if (real != null) {
            message.send(sender, "realname-output", "{player}", args[0], "{nick}", real);
        } else {
            message.send(sender, "realname-not-found");
        }
        return true;
    }
}