package com.tiffnix.miniblocks;

import org.bukkit.entity.Entity;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class PluginListener implements Listener {
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (MiniBlocks.isFreshTrader(entity)) {
            WanderingTrader trader = (WanderingTrader) entity;

            MiniBlocks.populateTrades(trader);
        }
    }
}
