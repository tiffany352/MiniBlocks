# MiniBlocks

MiniBlocks is a Spigot plugin which ports a couple of head-related
[VanillaTweaks data packs](https://vanillatweaks.net/picker/datapacks/)
to a Spigot plugin. Currently, it has these:

- [Wandering trader block trades](https://vanillatweaks.net/share#CanB6U):
  The data pack has to run code every tick, has no config options.
- [Player heads drop on death](https://vanillatweaks.net/share#flGepK):
  Incompatible with grave plugins like Gravy, has no config options.

Some data packs work fine on Spigot and don't need porting. These
include:

- [More Mob Heads](https://vanillatweaks.net/share#XxZ9nY): The data
  pack contains only loot tables and has no known incompatibilities.

Additional features unrelated to Vanilla Tweaks:

- Player head name fix: Fixes a bug in Vanilla where player heads lose
  their name and lore text when placed and picked back up.
- Mob head name fix: The same, but for other head types (creeper, wither
  skeleton, dragon, etc.).

## Setup

**NBTApi is required for the plugin to work.**

<https://www.spigotmc.org/resources/nbt-api.7939/>

After first startup, the config file will be created and has comments
explaining what each option does.

`/miniblocks reload` can be used to safely reload the config while the
server is running. By default requires operator permissions, this can be
changed using the `miniblocks.manage` permission.

Other Vanilla Tweaks data packs may be added alongside this plugin. To
install them on Spigot/Paper, put them in `world/datapacks`. This folder
is global for all worlds/dimensions on your server.

## Credits

The idea for this plugin was directly inspired by Vanilla Tweaks.

Additionally, the Wandering Trader's trades list is taken directly from
Vanilla Tweaks.

<https://vanillatweaks.net/>

This plugin is not affiliated with or endorsed by Vanilla Tweaks.

## Contributing

To get started, import the Maven project (pom.xml) into your favorite
IDE. Make sure you don't commit any IDE project/cache files into Git,
put them into .gitignore if necessary.

Your contributions must be available under the terms of the [MPLv2 license](./LICENSE-MPL).

Make sure new files added have the license boilerplate.

```java
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
```

### Updating the trades list

`src/main/resources/trades.tsv` is created by opening [this google docs
spreadsheet][1] and exporting it to TSV. It should be occasionally
updated to stay in sync with Vanilla Tweaks.

[1]: https://docs.google.com/spreadsheets/d/1udjXJUFiTCUUtfa7srQa4Ip8O3oCFYsq3AsD4Ax_7Dw/edit#gid=406953118
