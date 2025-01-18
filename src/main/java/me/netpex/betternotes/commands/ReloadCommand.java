package me.netpex.betternotes.commands;

import me.netpex.betternotes.BetterNotes;
import me.netpex.betternotes.ConfigManager;
import me.netpex.betternotes.Database;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final BetterNotes BM;
    private Database database;
    private final ConfigManager configManager;

    public ReloadCommand(BetterNotes plugin) {
        this.BM = plugin;
        this.database = this.BM.getDatabase();
        this.configManager = this.BM.getPluginConfig();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        configManager.reloadConfig();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &aConfig reloaded!"));

        return true;
    }

}