/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package com.tiffnix.miniblocks;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TradesTable {
    public static class TradeEntry {
        public final Material buy1;
        public final Material buy2;
        public final ItemStack sell;
        public final int uses;

        TradeEntry(Material buy1, Material buy2, ItemStack sell, int uses) {
            this.buy1 = buy1;
            this.buy2 = buy2;
            this.sell = sell;
            this.uses = uses;
        }

        @Override
        public String toString() {
            String buy1Str = buy1 != null ? buy1.getKey().toString() : "null";
            String buy2Str = buy2 != null ? buy2.getKey().toString() : "null";
            return "TradeEntry(" + buy1Str + " + " + buy2Str + " -> " + sell.toString() + ")";
        }

        public String getColorName() {
            ItemMeta meta = sell.getItemMeta();
            assert meta != null;
            return meta.getDisplayName();
        }

        public String getPlainName() {
            return ChatColor.stripColor(getColorName());
        }
    }

    private final ArrayList<TradeEntry> entries = new ArrayList<>();

    public void addFromTsv(String input, boolean includeHermits, boolean includeBlocks) {
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

            // The data doesn't distinguish whether an entry is a Hermit head or a mini
            // block, so use this heuristic.
            boolean isHermitHead = buy1 == Material.EMERALD && buy2 == Material.AIR;

            if (isHermitHead && !includeHermits) {
                continue;
            } else if (!isHermitHead && !includeBlocks) {
                continue;
            }

            NBTContainer compound = new NBTContainer(nbtText);
            ItemStack sell = NBTItem.convertNBTtoItem(compound);

            int uses;
            if (isHermitHead) {
                uses = MiniBlocks.INSTANCE.traderPlayerMaxTrades;
            } else {
                uses = MiniBlocks.INSTANCE.traderMiniBlocksMaxTrades;
            }

            entries.add(new TradeEntry(buy1, buy2, sell, uses));
        }
    }

    public void addDefaults(boolean includeHermits, boolean includeBlocks) {
        if (!includeHermits && !includeBlocks) {
            return;
        }

        final Logger logger = MiniBlocks.INSTANCE.getLogger();
        InputStream stream = MiniBlocks.INSTANCE.getResource("trades.tsv");
        if (stream == null) {
            logger.log(Level.SEVERE, "Could not find builtin trade list");
            return;
        }
        String string = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));
        addFromTsv(string, includeHermits, includeBlocks);
    }

    public void addPlayerHeads(List<MiniBlocks.CustomPlayer> players) {
        for (MiniBlocks.CustomPlayer player : players) {
            final Material buy1 = Material.EMERALD;
            final Material buy2 = Material.AIR;

            final ItemStack sell = HeadUtil.createPlayerHead("§r§e" + player.name, null, player.name,
                    UUID.fromString(player.uuid));

            entries.add(new TradeEntry(buy1, buy2, sell, MiniBlocks.INSTANCE.traderPlayerMaxTrades));
        }
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

    public List<TradeEntry> findMatches(List<String> terms) {
        class ScoredMatch {
            final TradeEntry entry;
            final int score;
            final String name;

            ScoredMatch(TradeEntry entry, int score, String name) {
                this.entry = entry;
                this.score = score;
                this.name = name;
            }
        }

        ArrayList<ScoredMatch> matches = new ArrayList<>();
        terms = terms.stream().map(String::toLowerCase).collect(Collectors.toList());
        String termsStr = String.join(" ", terms);

        for (TradeEntry entry : entries) {
            String name = entry.getPlainName().toLowerCase();
            int score = 0;
            if (name.equals(termsStr)) {
                score = Integer.MIN_VALUE;
            } else {
                for (String term : terms) {
                    if (name.contains(term)) {
                        score -= 1;
                        break;
                    }
                }
            }
            if (score < 0) {
                matches.add(new ScoredMatch(entry, score, name));
            }
        }

        return matches.stream().sorted((left, right) -> {
            int score1 = Integer.compare(left.score, right.score);
            if (score1 != 0) {
                return score1;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(left.name, right.name);
        }).limit(25).map(match -> match.entry).collect(Collectors.toList());
    }

    public long size() {
        return entries.size();
    }
}
