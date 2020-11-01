package com.tiffnix.miniblocks;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MiniBlocks extends JavaPlugin {
    public static MiniBlocks INSTANCE;
    public static TradesTable TRADES;
    public static NamespacedKey TRADES_PRESENT;
    public static NamespacedKey ORIGINAL_NAME;

    /**
     * Returns true if the entity is a wandering trader which hasn't yet been populated with mini blocks trades.
     *
     * @param entity Any entity
     */
    public static boolean isFreshTrader(Entity entity) {
        return entity != null && entity.getType() == EntityType.WANDERING_TRADER && !entity.getPersistentDataContainer().has(TRADES_PRESENT, PersistentDataType.BYTE);
    }

    public static void populateTrades(Merchant merchant) {
        HumanEntity trader = merchant.getTrader();
        if (trader != null && trader.getPersistentDataContainer().has(TRADES_PRESENT, PersistentDataType.BYTE)) {
            return;
        }

        List<MerchantRecipe> originalRecipes = merchant.getRecipes();
        ArrayList<MerchantRecipe> recipes = new ArrayList<>();

        Random random = new Random();
        // Choose a count between 3 and 5 trades to show.
        // TODO: Make configurable.
        int recipeCount = random.nextInt(2) + 3;
        TradesTable.TradeEntry[] trades = TRADES.getRandomTrades(recipeCount, random);

        recipes.ensureCapacity(originalRecipes.size() + recipeCount);

        for (TradesTable.TradeEntry entry : trades) {
            MerchantRecipe recipe = new MerchantRecipe(entry.sell, 1);
            if (entry.buy1 != null && !entry.buy1.isAir()) {
                recipe.addIngredient(new ItemStack(entry.buy1, 1));
            }
            if (entry.buy2 != null && !entry.buy2.isAir()) {
                recipe.addIngredient(new ItemStack(entry.buy2, 1));
            }
            recipes.add(recipe);
        }

        recipes.addAll(originalRecipes);
        merchant.setRecipes(recipes);
        if (trader != null) {
            trader.getPersistentDataContainer().set(TRADES_PRESENT, PersistentDataType.BYTE, (byte) 1);
        }
    }

    public static boolean isPlayerHead(BlockState state) {
        return state.getType() == Material.PLAYER_HEAD || state.getType() == Material.PLAYER_WALL_HEAD;
    }

    public static void retrieveOriginalName(BlockState state, ItemStack dest) {
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            String original = tileState.getPersistentDataContainer().getOrDefault(ORIGINAL_NAME, PersistentDataType.STRING, "");
            NBTContainer displayTag = new NBTContainer(original);

            NBTItem nbtItem = new NBTItem(dest);
            NBTCompound display = nbtItem.addCompound("display");
            display.mergeCompound(displayTag);
            nbtItem.applyNBT(dest);
        }
    }

    public static void storeOriginalName(BlockState state, ItemStack source) {
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            NBTItem nbtItem = new NBTItem(source);
            NBTCompound itemDisplay = nbtItem.getCompound("display");
            String name = itemDisplay != null ? itemDisplay.toString() : null;
            INSTANCE.getLogger().info("storing tag: " + name);
            if (name != null) {
                tileState.getPersistentDataContainer().set(ORIGINAL_NAME, PersistentDataType.STRING, name);
            }
        } else {
            INSTANCE.getLogger().warning(state.toString() + " is missing a corresponding tile entity");
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        INSTANCE = this;
        TRADES_PRESENT = new NamespacedKey(this, "trades_present");
        ORIGINAL_NAME = new NamespacedKey(this, "original_name");

        reload();
        getLogger().info("Registered " + TRADES.size() + " trades for the Wandering Trader.");

        getServer().getPluginManager().registerEvents(new PluginListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reload() {
        TRADES = new TradesTable();
        TRADES.addDefaults();
    }
}
