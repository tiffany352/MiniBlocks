package com.tiffnix.miniblocks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MiniBlockCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        if ("reload".equals(args[0])) {
            MiniBlocks.INSTANCE.reloadConfig();
            MiniBlocks.INSTANCE.reload();
            final long count = MiniBlocks.TRADES.size();
            sender.sendMessage("Reloaded successfully. Loaded " + count + " trades.");
            return true;
        }
        return false;
    }
}
