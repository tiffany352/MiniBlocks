/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package com.tiffnix.miniblocks;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MiniBlockCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        switch (args[0]) {
            case "reload":
                return onReload(sender);
            case "player":
                return onPlayer(sender, args);
            case "get":
                return onGet(sender, args);
            default:
                return false;
        }
    }

    private boolean onReload(CommandSender sender) {
        if (!sender.hasPermission("miniblocks.manage")) {
            sender.sendMessage("You don't have permission to do this.");
            return true;
        }

        MiniBlocks.INSTANCE.reloadConfig();
        MiniBlocks.INSTANCE.reload();
        final long count = MiniBlocks.TRADES.size();
        sender.sendMessage("Reloaded successfully. Loaded " + count + " trades.");

        return true;
    }

    private boolean onPlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("miniblocks.give")) {
            sender.sendMessage("You don't have permission to do this.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(
                    "Usage: /miniblocks player <username or UUID> [optional item name] [optional item lore]");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        Player senderPlayer = (Player) sender;

        OfflinePlayer player;
        try {
            UUID uuid = UUID.fromString(args[1]);
            player = Bukkit.getOfflinePlayer(uuid);
        } catch (IllegalArgumentException error) {
            // This is marked deprecated as saying you shouldn't use it for persistent
            // storage. However, this is for a slash command...
            player = Bukkit.getOfflinePlayer(args[1]);
        }

        String name = args.length >= 3 ? args[2] : null;
        if ("null".equals(name)) {
            name = null;
        }
        List<String> lore = null;
        if (args.length >= 4) {
            lore = new ArrayList<>(Arrays.asList(args).subList(3, args.length));
        }

        ItemStack item = HeadUtil.createPlayerHead(name, lore, player.getName(), player.getUniqueId());
        giveItem(senderPlayer, item);

        return true;
    }

    private boolean onGet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("miniblocks.give")) {
            sender.sendMessage("You don't have permission to do this.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /miniblocks get <search terms>");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;

        List<String> terms = new ArrayList<>(Arrays.asList(args).subList(1, args.length));

        List<TradesTable.TradeEntry> trades = MiniBlocks.TRADES.findMatches(terms);

        if (trades.isEmpty()) {
            sender.sendMessage("No results found.");
            return true;
        }

        TradesTable.TradeEntry trade = trades.get(0);
        giveItem(player, trade.sell);

        return true;
    }

    private void giveItem(Player player, ItemStack item) {
        Item entity = (Item) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.DROPPED_ITEM);
        entity.setItemStack(item.clone());
        entity.setPickupDelay(0);
        entity.setThrower(player.getUniqueId());
        entity.setVelocity(player.getEyeLocation().getDirection());
    }
}
