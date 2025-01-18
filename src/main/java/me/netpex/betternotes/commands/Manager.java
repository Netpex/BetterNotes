package me.netpex.betternotes.commands;

import me.netpex.betternotes.BetterNotes;;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Manager implements CommandExecutor, TabCompleter {
    private static BetterNotes BM;

    public Manager(BetterNotes plugin) {
        this.BM = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &cInvalid subcommand!"));
            return false;
        }

        if (!sender.hasPermission("betternote."+args[0].toLowerCase())) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &cYou do not have permission to do that!!"));
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "withdraw":
                new WithdrawCommand(BM).onCommand(sender, command, label, args);
                break;
            case "deposit":
                new DepositCommand(BM).onCommand(sender, command, label, args);
                break;
            case "info":
                new InfoCommand(BM).onCommand(sender, command, label, args);
                break;
            case "reload":
                new ReloadCommand(BM).onCommand(sender, command, label, args);
                break;
            default:
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &7Invalid subcommand!"));
                break;
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("withdraw", "deposit", "info", "reload"), suggestions);
        }

        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("withdraw")) {
                suggestions.add("<amount>");
            }
        }
        return suggestions;
    }

}
