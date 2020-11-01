package com.tiffnix.miniblocks;

import org.bukkit.Material;
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

            MiniBlocks.populateTrades(trader);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Block block = event.getBlockPlaced();
        final BlockState state = block.getState();
        if (MiniBlocks.isPlayerHead(state)) {
            MiniBlocks.storeOriginalName(state, event.getItemInHand());
            state.update();
        }
    }

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        final BlockState beforeBreak = event.getBlockState();
        if (MiniBlocks.isPlayerHead(beforeBreak)) {
            for (Item item : event.getItems()) {
                final ItemStack stack = item.getItemStack();
                if (stack.getType() == Material.PLAYER_HEAD) {
                    MiniBlocks.retrieveOriginalName(beforeBreak, stack);
                    item.setItemStack(stack);
                    break;
                }
            }
        }
    }
}
