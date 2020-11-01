package com.tiffnix.miniblocks;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TradesTable {
    public static class TradeEntry {
        public final Material buy1;
        public final Material buy2;
        public final ItemStack sell;

        TradeEntry(Material buy1, Material buy2, ItemStack sell) {
            this.buy1 = buy1;
            this.buy2 = buy2;
            this.sell = sell;
        }

        @Override
        public String toString() {
            String buy1Str = buy1 != null ? buy1.getKey().toString() : "null";
            String buy2Str = buy2 != null ? buy2.getKey().toString() : "null";
            return "TradeEntry(" + buy1Str + " + " +
                    buy2Str + " -> " + sell.toString() + ")";
        }
    }

    private final ArrayList<TradeEntry> entries = new ArrayList<>();

    public void addFromTsv(String input) {
        boolean first = true;
        for (String line : input.split("\n")) {
            // Skip the first line, which is a heading.
            if (first) {
                first = false;
                continue;
            }

            String[] columns = line.split("\t");
            Material buy1 = Material.matchMaterial(columns[0]);
            Material buy2 = Material.matchMaterial(columns[1]);
            // Third column (name) left unused.
            String nbtText = columns[3];

            NBTContainer compound = new NBTContainer(nbtText);
            ItemStack sell = NBTItem.convertNBTtoItem(compound);

            entries.add(new TradeEntry(buy1, buy2, sell));
        }
    }

    public void addDefaults() {
        final Logger logger = MiniBlocks.INSTANCE.getLogger();
        InputStream stream = MiniBlocks.INSTANCE.getResource("trades.tsv");
        if (stream == null) {
            logger.log(Level.SEVERE, "Could not find builtin trade list");
            return;
        }
        String string = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        addFromTsv(string);
    }

    public TradeEntry[] getRandomTrades(int count, Random random) {
        TradeEntry[] list = new TradeEntry[count];

        int i = 0;
        while (i < count) {
            int index = random.nextInt(entries.size());
            TradeEntry entry = entries.get(index);
            if (Arrays.stream(list).noneMatch((value) -> value == entry)) {
                list[i] = entry;
                i += 1;
            }
        }

        return list;
    }

    public long size() {
        return entries.size();
    }
}
