/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package com.tiffnix.miniblocks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PluginListener implements Listener {
    private static class SpawnItemJob extends BukkitRunnable {
        final ItemStack itemStack;
        final Location spawnLocation;

        SpawnItemJob(ItemStack itemStack, Location spawnLocation) {
            this.itemStack = itemStack;
            this.spawnLocation = spawnLocation;
        }

        @Override
        public void run() {
            final World world = spawnLocation.getWorld();
            assert world != null;
            Item item = (Item) world.spawnEntity(spawnLocation, EntityType.DROPPED_ITEM);
            item.setItemStack(itemStack);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        final Entity entity = event.getEntity();
        if (MiniBlocks.isFreshTrader(entity)) {
            WanderingTrader trader = (WanderingTrader) entity;

            MiniBlocks.INSTANCE.populateTrades(trader);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!MiniBlocks.INSTANCE.fixPlayerHeadNames && !MiniBlocks.INSTANCE.fixMobHeadNames) {
            return;
        }

        final Block block = event.getBlockPlaced();
        final BlockState state = block.getState();
        if (MiniBlocks.INSTANCE.isHeadToApplyFixTo(state.getType())) {
            MiniBlocks.storeOriginalName(state, event.getItemInHand());
            state.update();
        }
    }

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (!MiniBlocks.INSTANCE.fixPlayerHeadNames && !MiniBlocks.INSTANCE.fixMobHeadNames) {
            return;
        }

        final BlockState beforeBreak = event.getBlockState();
        if (MiniBlocks.INSTANCE.isHeadToApplyFixTo(beforeBreak.getType())) {
            for (Item item : event.getItems()) {
                final ItemStack stack = item.getItemStack();
                if (MiniBlocks.INSTANCE.isHeadToApplyFixTo(stack.getType())) {
                    MiniBlocks.retrieveOriginalName(beforeBreak, stack);
                    item.setItemStack(stack);
                    break;
                }
            }
        }
    }

    private static String applyFormatting(String format, Player player, Player killer) {
        format = format.replace("%player_name%", player.getName());
        if (killer != null) {
            format = format.replace("%killer_name%", killer.getName());
        } else {
            if (format.contains("%killer_name%")) {
                return null;
            }
        }
        return format;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!MiniBlocks.INSTANCE.playerHeadsEnabled) {
            return;
        }

        final Player player = event.getEntity();
        final Player killer = player.getKiller();

        if (killer == null && MiniBlocks.INSTANCE.playerHeadsRequirePlayerKill) {
            return;
        }

        final String itemName = applyFormatting(MiniBlocks.INSTANCE.playerHeadsNameFormat, player, killer);
        final List<String> itemLore = MiniBlocks.INSTANCE.playerHeadsLoreFormat.stream()
                .map(format -> applyFormatting(format, player, killer)).filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        final ItemStack item = HeadUtil.createPlayerHead(itemName, itemLore, player);

        // Items are spawned as a runnable to make sure that other plugins can't
        // interfere with the item being dropped on the ground. This is mostly to avoid
        // any possible issues with grave plugins.
        new SpawnItemJob(item, player.getLocation()).runTask(MiniBlocks.INSTANCE);
    }
}
