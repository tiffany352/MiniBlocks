package com.tiffnix.miniblocks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MiniBlocksCompleter implements TabCompleter {
    private static final String[] COMMANDS = new String[] { "reload", "player", "get" };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return Arrays.asList(COMMANDS);
        }

        switch (args[0]) {
            case "reload":
                return new ArrayList<>();
            case "player":
                switch (args.length) {
                    case 1:
                    case 2:
                        return null;
                    case 3:
                        return Collections.singletonList("null");
                    default:
                        return new ArrayList<>();
                }
            case "get":
                List<String> terms = new ArrayList<>();
                if (args.length >= 2) {
                    terms = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
                }
                List<TradesTable.TradeEntry> trades = MiniBlocks.TRADES.findMatches(terms);
                return trades.stream().map(TradesTable.TradeEntry::getPlainName).collect(Collectors.toList());
            default:
                if (args.length > 1) {
                    return null;
                } else {
                    return StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), new ArrayList<>());
                }
        }
    }
}
