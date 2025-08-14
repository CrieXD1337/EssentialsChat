# EssentialsChat 🌟

**EssentialsChat** is a powerful and flexible chat management plugin for Nukkit servers! 🚀 It supports global and local chat, customizable prefixes and nicknames, integration with LuckPerms and Multipass, and advanced chat filtering. Perfect for servers that need enhanced chat control and customization. 🎉

---

## ✨ Features

- **Global and Local Chat**:
  - Local chat with configurable radius 📍
  - Global chat triggered by a symbol (default: `!`) 🌍
  - Support for default Minecraft chat format 🗨️
- **Prefix and Suffix Customization**:
  - Seamless integration with **LuckPerms** and **Multipass** for prefix/suffix management 🔖
  - `/prefix` command to set custom prefixes 🎨
- **Nickname Management**:
  - `/nick` command to set custom nicknames 🖌
  - Optional colored nicknames and duplicate nickname restrictions 🚫
  - `/realname` command to find a player's real name 🕵️‍♂️
- **Chat Filtering**:
  - Message cooldown to prevent spam ⏰
  - Maximum message length restriction 📏
  - Anti-spam protection for repeated messages 🚨
  - Configurable punishments: kick, message, or custom command ⚖️
- **PlaceholderAPI Integration**:
  - Use placeholders to customize chat formats 📝
- **Highly Configurable**:
  - Blacklists for nicknames and prefixes 🚫
  - Customizable message formats and character limits 🔧
- **Debug Mode**:
  - Detailed logging for developers 🛠️

---

## 📥 Installation

1. **Download the Plugin**:
  - Grab the [latest](https://github.com/CrieXD1337/EssentialsChat/releases/latest) `.jar` file from [Releases](https://github.com/CrieXD1337/EssentialsChat/releases/latest) 📦
2. **Install Dependencies**:
  - **Required**: [PlaceholderAPI](https://cloudburstmc.org/resources/placeholderapi.104/) for placeholder support.
  - **Optional**: [LuckPerms](https://luckperms.net/download) or [Multipass](https://cloudburstmc.org/resources/multipass.29/) for prefix/suffix support.
3. **Place the Jar**:
  - Drop `EssentialsChat.jar` into your `plugins` folder.
4. **Restart the Server**:
  - Start or restart your Nukkit server to load the plugin. The default configuration files (`config.yml`, `messages.yml`, `provider.yml`) will be generated automatically. 🔄
5. **Configure**:
  - Edit `config.yml` and `messages.yml` in the `plugins/EssentialsChat` folder to customize settings and messages. 🛠️
  - Set the provider in `provider.yml` (`luckperms`, `multipass`, or `fallback`).

---

## ⚙️ Configuration

The plugin is highly customizable through its configuration files. Below are the key files and their purposes:

- **config.yml**: Main configuration for chat formats, nickname/prefix rules, and chat filtering settings.
- **messages.yml**: Customizable messages for commands and chat filtering.
- **provider.yml**: Select the prefix/suffix provider (`luckperms`, `multipass`, or `fallback`).

### Example Config Snippets

**config.yml**:
```yaml
chat-formatting:
  format-method: 1 # 1: Local/Global, 2: Single, 3: Default Minecraft
  local-chat-radius: 100
  global-chat-symbol: "!"
  local-chat-format: "§7[§aL§7] §r{prefix}§r {player}{suffix}§r §a» §8{msg}"
  global-chat-format: "§7[§4G§7] §r{prefix}§r {player}{suffix}§r §a» §f{msg}"

nick:
  max-characters-length: 15
  min-characters-length: 3
  allow-colored-nick: false
  allow-duplicate-nicknames: false
```

**messages.yml**:
```yaml
prefix-set: "§7> §fYour prefix successfully moved to: §b{prefix}"
nick-success: "§7> §fYour nickname changed to §b{nick}"
cooldown-for-messages: "§7> §cWait §b{seconds} §cseconds"
```

Check the full configuration files in the [repository](https://github.com/CriexD1337/EssentialsChat/tree/main/main/resources) for all available options.

---

## 📜 Commands

| Command | Description | Permission | Usage                           |
|---------|-------------|------------|---------------------------------|
| `/prefix <prefix\|off>` | Set or clear your prefix | `essentialschat.prefix.setprefix` | `/prefix VIP` or `/prefix off`  |
| `/nick <nick\|off>` | Set or clear your nickname | `essentialschat.nick.setnick` | `/nick Nick` or `/nick off` |
| `/realname <player>` | Find a player's real name | `essentialschat.nick.realname` | `/realname Nick`                |

**Note**: By default, these permissions are granted to operators (`op`). Configure permissions using LuckPerms or Multipass for other players.

---

## 🔌 API

EssentialsChat provides a simple API for developers to integrate with the plugin. Use the `EssentialsChatAPI` interface to manage nicknames and prefixes programmatically.

### Example Usage

```java
import cn.nukkit.Player;
import me.criex.essentialschat.api.EssentialsChatAPI;
import me.criex.essentialschat.EssentialsChat;

// Get the API instance
EssentialsChatAPI api = EssentialsChat.getAPI();

// Set a player's nickname
api.setNickname(player, "ExampleNick");

// Clear a player's nickname
api.clearNickname(player);

// Get a player's real name from their nickname
String realName = api.getRealName("ExampleNick");

// Set a player's prefix
api.setPrefix(player, "&b[Magic]");

// Clear a player's prefix
api.clearPrefix(player);

// Get a player's prefix or suffix
String prefix = api.getPrefix(player);
String suffix = api.getSuffix(player);
```

For more details, check the [API documentation](https://github.com/CriexD1337/EssentialsChat/tree/master/src/main/java/me/criex/essentialschat/api).

---

## 📝 License

This project is licensed under MIT license. Please see the [LICENSE](./LICENSE) file for details.

🌟 **Make your server’s chat awesome with EssentialsChat!** 🌟