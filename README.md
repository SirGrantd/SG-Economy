# SG Economy API

## Overview

**SG Economy API** adds a flexible, modern, and extensible economic system to Minecraft. It works as a **Mod API**, allowing players, modpack creators, developers, and server administrators to create and integrate custom economies in a simple and consistent way.

The economic system is based on a **persistent numeric balance**, directly associated with player data. This balance is **not represented by in-game items**, functioning as a “virtual wallet” whose name, meaning, and usage may vary depending on the implementation or integration with other mods.

## Key Features

### 💰 Flexible Monetary System

Allows configuration to use **integer** or **decimal** values, adapting the economy to different use cases (simple currencies, systems with cents, etc.). The configuration is done via a file and directly affects the format of the stored balance.

### 🔧 Developer API

Provides documented classes and methods for:

* Querying and modifying player balances
* Native integration with other mods
* Creating custom economic systems

The GitHub repository includes practical examples and implementation guides.

### 🛠️ Administrative Commands

Includes commands for in-game economy management, allowing:

* Balance checks
* Adding, setting, or removing values
* Transferring balance between players (optional)

These commands can be enabled or disabled according to server needs.

### 🖥️ Display Interface

Displays the player’s economic balance in the inventory interface and/or on-screen, in a visual and configurable way.
The display reflects only the **value stored in the player’s data**, with no relation to physical items.

### ⚙️ Configuration Files

Configuration files allow customization of the economic system’s behavior, including currency format, balance retention rules, available commands, and interface display options.