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
package me.criex.essentialschat.api;

import cn.nukkit.Player;
import me.criex.essentialschat.managers.DisplayManager;
import me.criex.essentialschat.managers.NickManager;
import me.criex.essentialschat.managers.PrefixManager;
import me.criex.essentialschat.providers.PrefixSuffixProvider;
import me.criex.essentialschat.utils.ConfigUtils;

public class EssentialsChatAPIImpl implements EssentialsChatAPI {
    private final NickManager nickManager;
    private final PrefixManager prefixManager;
    private final PrefixSuffixProvider provider;
    private final DisplayManager displayManager;

    public EssentialsChatAPIImpl(NickManager nickManager, PrefixManager prefixManager, PrefixSuffixProvider provider, DisplayManager displayManager, ConfigUtils configUtils) {
        this.nickManager = nickManager;
        this.prefixManager = prefixManager;
        this.provider = provider;
        this.displayManager = displayManager;
    }

    @Override
    public void setNickname(Player player, String nickname) {
        nickManager.setPlayerNick(player, nickname);
    }

    @Override
    public void clearNickname(Player player) {
        nickManager.clearPlayerNick(player);
    }

    @Override
    public String getRealName(String fakeNick) {
        return nickManager.getRealName(fakeNick);
    }

    @Override
    public void setPrefix(Player player, String prefix) {
        prefixManager.setPlayerPrefix(player, prefix);
    }

    @Override
    public void clearPrefix(Player player) {
        prefixManager.clearPlayerPrefix(player);
    }

    @Override
    public String getPrefix(Player player) {
        return provider.getPrefix(player);
    }

    @Override
    public String getSuffix(Player player) {
        return provider.getSuffix(player);
    }
}