package com.tiffnix.miniblocks;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;

public class PluginListener implements Listener {

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
}
