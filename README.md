# YGenerators
Plugin for custom generators. Made and tested on version **1.17.1** but should work on 1.15 and higher.
Plugin requires at least [Paper](https://github.com/PaperMC/Paper), won't work on Spigot or Bukkit.
If you are still using Spigot then come back from stone age and use [Paper](https://github.com/PaperMC/Paper), [Pufferfish](https://github.com/pufferfish-gg/Pufferfish) or [Purpur](https://github.com/PurpurMC/Purpur)

# Features
### General:
- Support for [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)
- Placeholders to use in any another plugin [(Click)](https://github.com/Ynfuien/YGenerators/wiki/Placeholders)
- Custom generators
- Global double drop
- Custom blocks generated in vanilla generators
- 1.16 ores drop instead of 1.17 raw ores drop
- fully customizable translations ([English](https://github.com/Ynfuien/YGenerators/blob/main/src/main/resources/langs/en-lang.yml) is by default but [Polish](https://github.com/Ynfuien/YGenerators/blob/main/src/main/resources/langs/pl-lang.yml) translation is also prepared, just copy it to your `lang.yml`)
- simple commands with tab completions

### Generators:
- Custom durability (can be infinite)
- Custom generator items with any lore and displayname
- Custom chances for blocks to generate
- Custom cooldown of block generating (too low cooldown and fast digging may occur ghost blocks)
- Custom recipe of generator
- Picking up placed generator in configured by you way
- Checking placed generator durability left

### Double drop:
- Custom multiplayer
- Can be disabled for any generator
- Set for any time by command


# Media - Example generators
#### Items
![Cobblestone generator item](https://i.imgur.com/7XCybJy.png)
![Stone generator item](https://i.imgur.com/eyJVDJL.png)
#### Craftings
![Cobblestone generator item crafting](https://i.imgur.com/RZ61mTI.png)
![Stone generator item crafting](https://i.imgur.com/Q6ya7cH.png)
#### Checking durability
![Check durability interaction](https://i.imgur.com/u4olMmm.gif)
#### Picking up generator
![Check durability interaction](https://i.imgur.com/wistrWC.gif)
#### Example infinite generator with 0 tick cooldown
![Mining infinite generator](https://i.imgur.com/YtbBKPI.gif)


# Configuration files
- [`config.yml`](https://github.com/Ynfuien/YGenerators/blob/main/src/main/resources/config.yml) - It has basic settings about generators and configuration of vanilla generators.
- [`generators.yml`](https://github.com/Ynfuien/YGenerators/blob/main/src/main/resources/generators.yml) - There are all settings about each generator.
- [`doubledrop.yml`](https://github.com/Ynfuien/YGenerators/blob/main/src/main/resources/doubledrop.yml) - In this file is saved time left of double drop and its multiplayer.
- [`database.yml`](https://github.com/Ynfuien/YGenerators/blob/main/src/main/resources/database.yml) - This file contains all palced generators on the server.

Most about configuring you will learn in configs themselves. Apparently "comment" should be my second name.


# License
This project use [GNU GPLv3](https://github.com/Ynfuien/YGenerators/blob/main/LICENSE) license.