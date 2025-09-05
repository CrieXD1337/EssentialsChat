# EssentialsChat API Documentation

This document provides a detailed overview of the **EssentialsChat API**, which allows developers to interact with the nickname and prefix functionalities of the EssentialsChat plugin for Nukkit servers. The API is designed to integrate seamlessly with the plugin's features, enabling developers to manage player nicknames, prefixes, and suffixes programmatically.

## Table of Contents

- [Overview](#overview)
- [API Interface](#api-interface)
- [API Implementation](#api-implementation)
- [Usage Examples](#usage-examples)
- [Accessing the API](#accessing-the-api)
- [Dependencies](#dependencies)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)
- [License](#license)

## Overview

The EssentialsChat API is provided through the `EssentialsChatAPI` interface and its implementation, `EssentialsChatAPIImpl`. It allows developers to:

- Set and clear player nicknames.
- Set and clear player prefixes.
- Retrieve player prefixes and suffixes.
- Retrieve the real name of a player based on their nickname.

The API integrates with permission plugins like **LuckPerms** or **Multipass** for prefix/suffix management and supports **PlaceholderAPI** for dynamic placeholder parsing. It is designed to be flexible and extensible, with a focus on maintaining compatibility with the plugin's configuration and provider systems.

## API Interface

The `EssentialsChatAPI` interface defines the core methods available for interacting with the EssentialsChat plugin. It is located in the `me.criex.essentialschat.api` package.

```java
package me.criex.essentialschat.api;

import cn.nukkit.Player;

public interface EssentialsChatAPI {
    void setNickname(Player player, String nickname);
    void clearNickname(Player player);
    String getRealName(String fakeNick);
    void setPrefix(Player player, String prefix);
    void clearPrefix(Player player);
    String getPrefix(Player player);
    String getSuffix(Player player);
}
```

### Method Descriptions

1. **`setNickname(Player player, String nickname)`**
    - **Description**: Sets a custom nickname for the specified player.
    - **Parameters**:
        - `player`: The `Player` object whose nickname is to be set.
        - `nickname`: The new nickname as a `String`.
    - **Behavior**: Updates the player's nickname in the `NickManager`. The nickname is validated based on the plugin's configuration (e.g., length, allowed characters, blacklist). The display name and nametag are updated via `DisplayManager`.

2. **`clearNickname(Player player)`**
    - **Description**: Removes the custom nickname for the specified player, reverting to their default name.
    - **Parameters**:
        - `player`: The `Player` object whose nickname is to be cleared.
    - **Behavior**: Clears the nickname from the `NickManager` and updates the player's display name and nametag.

3. **`getRealName(String fakeNick)`**
    - **Description**: Retrieves the real name of a player based on their nickname.
    - **Parameters**:
        - `fakeNick`: The nickname to look up.
    - **Returns**: The real player name as a `String`, or `null` if no player is found with the given nickname.
    - **Behavior**: Searches the `NickManager` for a matching nickname (case-insensitive).

4. **`setPrefix(Player player, String prefix)`**
    - **Description**: Sets a custom prefix for the specified player.
    - **Parameters**:
        - `player`: The `Player` object whose prefix is to be set.
        - `prefix`: The prefix as a `String`.
    - **Behavior**: Updates the player's prefix using the active `PrefixSuffixProvider` (e.g., LuckPerms or Multipass). The prefix is validated based on the plugin's configuration (e.g., length, allowed characters, blacklist).

5. **`clearPrefix(Player player)`**
    - **Description**: Removes the custom prefix for the specified player.
    - **Parameters**:
        - `player`: The `Player` object whose prefix is to be cleared.
    - **Behavior**: Clears the prefix using the active `PrefixSuffixProvider`.

6. **`getPrefix(Player player)`**
    - **Description**: Retrieves the current prefix for the specified player.
    - **Parameters**:
        - `player`: The `Player` object whose prefix is to be retrieved.
    - **Returns**: The prefix as a `String`, or an empty string if no prefix is set.
    - **Behavior**: Queries the active `PrefixSuffixProvider` for the player's prefix.

7. **`getSuffix(Player player)`**
    - **Description**: Retrieves the current suffix for the specified player.
    - **Parameters**:
        - `player`: The `Player` object whose suffix is to be retrieved.
    - **Returns**: The suffix as a `String`, or an empty string if no suffix is set.
    - **Behavior**: Queries the active `PrefixSuffixProvider` for the player's suffix.

## API Implementation

The `EssentialsChatAPIImpl` class implements the `EssentialsChatAPI` interface. It integrates with the plugin's internal managers (`NickManager`, `PrefixManager`, `DisplayManager`) and the `PrefixSuffixProvider` to perform the necessary operations.

### Key Components

- **NickManager**: Manages player nicknames, including setting, clearing, and retrieving nicknames.
- **PrefixManager**: Manages player prefixes, interfacing with the `PrefixSuffixProvider` (e.g., LuckPerms, Multipass, or Fallback).
- **DisplayManager**: Updates player display names and nametags based on prefixes, suffixes, and nicknames.
- **PrefixSuffixProvider**: An abstraction for handling prefix/suffix data, with implementations for:
    - `LProvider` (LuckPerms integration)
    - `MultipassProvider` (Multipass integration)
    - `FallbackProvider` (default, returns empty strings for prefixes/suffixes)

### Constructor

```java
public EssentialsChatAPIImpl(NickManager nickManager, PrefixManager prefixManager, 
                             PrefixSuffixProvider provider, DisplayManager displayManager, 
                             ConfigUtils configUtils)
```

- **Parameters**:
    - `nickManager`: Manages nickname-related operations.
    - `prefixManager`: Manages prefix-related operations.
    - `provider`: The active `PrefixSuffixProvider` for prefix/suffix operations.
    - `displayManager`: Updates player display names and nametags.
    - `configUtils`: Provides access to the plugin's configuration settings.

## Usage Examples

Below are examples of how to use the EssentialsChat API in a Nukkit plugin.

### Example 1: Setting a Nickname

```java
import cn.nukkit.Player;
import me.criex.essentialschat.EssentialsChat;
import me.criex.essentialschat.api.EssentialsChatAPI;

public class MyPlugin extends PluginBase {
    @Override
    public void onEnable() {
        Player player = getServer().getPlayer("ExamplePlayer");
        if (player != null) {
            EssentialsChatAPI api = EssentialsChat.getAPI();
            api.setNickname(player, "CoolNick");
        }
    }
}
```

This sets the nickname "CoolNick" for the player named "ExamplePlayer".

### Example 2: Clearing a Prefix

```java
import cn.nukkit.Player;
import me.criex.essentialschat.EssentialsChat;
import me.criex.essentialschat.api.EssentialsChatAPI;

public class MyPlugin extends PluginBase {
    @Override
    public void onEnable() {
        Player player = getServer().getPlayer("ExamplePlayer");
        if (player != null) {
            EssentialsChatAPI api = EssentialsChat.getAPI();
            api.clearPrefix(player);
        }
    }
}
```

This clears the prefix for the player named "ExamplePlayer".

### Example 3: Retrieving a Player's Real Name

```java
import me.criex.essentialschat.EssentialsChat;
import me.criex.essentialschat.api.EssentialsChatAPI;

public class MyPlugin extends PluginBase {
    @Override
    public void onEnable() {
        EssentialsChatAPI api = EssentialsChat.getAPI();
        String realName = api.getRealName("CoolNick");
        if (realName != null) {
            getLogger().info("The real name for nickname 'CoolNick' is: " + realName);
        } else {
            getLogger().info("No player found with nickname 'CoolNick'");
        }
    }
}
```

This retrieves the real name of the player using the nickname "CoolNick".

## Accessing the API

To access the API, use the static `getAPI()` method from the `EssentialsChat` class:

```java
EssentialsChatAPI api = EssentialsChat.getAPI();
```

Ensure that the EssentialsChat plugin is installed and enabled on the server. The API instance is initialized during the plugin's `onEnable` phase and is available via the `getAPI()` method.

## Dependencies

The EssentialsChat API has the following dependencies:

- **Nukkit**: The server platform for which the plugin is designed.
- **LuckPerms** (optional): For prefix/suffix management if used as the provider.
- **Multipass** (optional): Alternative provider for prefix/suffix management.
- **PlaceholderAPI** (optional): For parsing placeholders in display names and chat messages.

If neither LuckPerms nor Multipass is available, the plugin falls back to the `FallbackProvider`, which returns empty strings for prefixes and suffixes.

## Error Handling

The API is designed to handle errors gracefully:

- **Invalid Nicknames/Prefixes**: The `NickManager` and `PrefixManager` validate inputs based on configuration settings (e.g., length, allowed characters, blacklist). Invalid inputs are rejected, and appropriate messages are sent to the player (handled by the `Message` class).
- **Provider Errors**: If a `PrefixSuffixProvider` fails (e.g., LuckPerms or Multipass is unavailable), the plugin logs warnings and may fall back to the `FallbackProvider`.
- **PlaceholderAPI Errors**: If PlaceholderAPI is enabled but encounters an error, the `parsePlaceholders` method in `EssentialsChat` disables PlaceholderAPI support and logs a warning.

Developers should check for `null` returns (e.g., in `getRealName`) and handle cases where the API might not be available (e.g., if EssentialsChat is not enabled).

## Best Practices

1. **Check for API Availability**:
   Always verify that the EssentialsChat plugin is loaded before accessing the API:
   ```java
   if (getServer().getPluginManager().getPlugin("EssentialsChat") != null) {
       EssentialsChatAPI api = EssentialsChat.getAPI();
       // Use API
   } else {
       getLogger().warning("EssentialsChat plugin not found!");
   }
   ```

2. **Validate Inputs**:
   When setting nicknames or prefixes, ensure inputs comply with the plugin's configuration (e.g., length, allowed characters) to avoid validation errors.

3. **Handle Null Returns**:
   Methods like `getRealName`, `getPrefix`, and `getSuffix` may return `null` or empty strings. Always include null checks in your code.

4. **Respect Configuration**:
   The API respects the plugin's configuration (e.g., `ConfigUtils`). Ensure your usage aligns with the server's settings, such as whether colored nicknames or duplicate nicknames are allowed.

5. **Debug Mode**:
   If the plugin is in debug mode (`configUtils.isDebug()`), additional logging is performed. Be mindful of this when testing, as it may generate verbose output.

## License

The EssentialsChat API is part of the EssentialsChat plugin, licensed under the MIT License. See the plugin's source code for the full license text.