package com.tiffnix.miniblocks;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.configuration.file.FileConfiguration;
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
import java.util.stream.Collectors;

public final class MiniBlocks extends JavaPlugin {
    public static MiniBlocks INSTANCE;
    public static TradesTable TRADES;
    public static NamespacedKey TRADES_PRESENT;
    public static NamespacedKey ORIGINAL_NAME;

    public static class CustomPlayer {
        final String name;
        final String uuid;

        public CustomPlayer(String name, String uuid) {
            this.name = name;
            this.uuid = uuid;
        }
    }

    // Head name fix
    boolean fixPlayerHeadNames = false;
    boolean fixMobHeadNames = false;

    // Wandering trader
    int traderMinOffers = 3;
    int traderMaxOffers = 5;
    int traderPlayerMaxTrades = 3;
    boolean traderHermitCraft = false;
    ArrayList<CustomPlayer> traderCustomPlayers = new ArrayList<>();
    boolean traderMiniBlocks = true;
    int traderMiniBlocksMaxTrades = 1;

    /**
     * Returns true if the entity is a wandering trader which hasn't yet been
     * populated with mini blocks trades.
     *
     * @param entity Any entity
     */
    public static boolean isFreshTrader(Entity entity) {
        return entity != null && entity.getType() == EntityType.WANDERING_TRADER
                && !entity.getPersistentDataContainer().has(TRADES_PRESENT, PersistentDataType.BYTE);
    }

    public void populateTrades(Merchant merchant) {
        HumanEntity trader = merchant.getTrader();
        if (trader != null && trader.getPersistentDataContainer().has(TRADES_PRESENT, PersistentDataType.BYTE)) {
            return;
        }

        List<MerchantRecipe> originalRecipes = merchant.getRecipes();
        ArrayList<MerchantRecipe> recipes = new ArrayList<>();

        Random random = new Random();
        int recipeCount = random.nextInt(traderMaxOffers - traderMinOffers) + traderMinOffers;
        TradesTable.TradeEntry[] trades = TRADES.getRandomTrades(recipeCount, random);

        recipes.ensureCapacity(originalRecipes.size() + recipeCount);

        for (TradesTable.TradeEntry entry : trades) {
            MerchantRecipe recipe = new MerchantRecipe(entry.sell, entry.uses);
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

    public static boolean isPlayerHead(Material mat) {
        return mat == Material.PLAYER_HEAD || mat == Material.PLAYER_WALL_HEAD;
    }

    public static boolean isMobHead(Material mat) {
        return mat == Material.SKELETON_SKULL || mat == Material.WITHER_SKELETON_SKULL || mat == Material.PLAYER_HEAD
                || mat == Material.ZOMBIE_HEAD || mat == Material.CREEPER_HEAD || mat == Material.DRAGON_HEAD
                || mat == Material.SKELETON_WALL_SKULL || mat == Material.WITHER_SKELETON_WALL_SKULL
                || mat == Material.PLAYER_WALL_HEAD || mat == Material.ZOMBIE_WALL_HEAD
                || mat == Material.CREEPER_WALL_HEAD || mat == Material.DRAGON_WALL_HEAD;
    }

    public boolean isHeadToApplyFixTo(Material mat) {
        return fixPlayerHeadNames && isPlayerHead(mat) || fixMobHeadNames && isMobHead(mat);
    }

    public static void retrieveOriginalName(BlockState state, ItemStack dest) {
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            String original = tileState.getPersistentDataContainer().getOrDefault(ORIGINAL_NAME,
                    PersistentDataType.STRING, "");
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

        this.saveDefaultConfig();

        reload();
        getLogger().info("Registered " + TRADES.size() + " trades for the Wandering Trader.");

        getServer().getPluginManager().registerEvents(new PluginListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reload() {
        FileConfiguration config = getConfig();

        fixPlayerHeadNames = config.getBoolean("head-name-fix.player-heads", true);
        fixMobHeadNames = config.getBoolean("head-name-fix.mob-heads", true);

        traderMinOffers = config.getInt("wandering-trader.min-offers", 3);
        traderMaxOffers = config.getInt("wandering-trader.max-offers", 5);
        traderPlayerMaxTrades = config.getInt("wandering-trader.player-heads.max-trades", 3);
        traderHermitCraft = config.getBoolean("wandering-trader.player-heads.hermit-craft-members", false);
        traderCustomPlayers = config.getMapList("wandering-trader.player-heads.custom-members").stream()
                .map(map -> new CustomPlayer((String) map.get("name"), (String) map.get("uuid")))
                .collect(Collectors.toCollection(ArrayList::new));
        traderMiniBlocks = config.getBoolean("wandering-trader.mini-blocks.enabled", true);
        traderMiniBlocksMaxTrades = config.getInt("wandering-trader.mini-blocks.max-trades", 1);

        TRADES = new TradesTable();
        TRADES.addDefaults(traderHermitCraft, traderMiniBlocks);
        TRADES.addPlayerHeads(traderCustomPlayers);
    }
}
