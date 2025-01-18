package me.netpex.betternotes.commands;

import me.netpex.betternotes.BetterNotes;
import me.netpex.betternotes.ConfigManager;
import me.netpex.betternotes.Database;
import me.netpex.betternotes.NoteUtility;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class InfoCommand implements CommandExecutor {

    private BetterNotes BM;
    private Database database;
    private NoteUtility noteUtility;
    private Economy economy;
    private ConfigManager configManager;

    public InfoCommand(BetterNotes plugin) {
        this.BM = plugin;
        this.database = plugin.getDatabase();
        this.noteUtility = plugin.getNoteUtil();
        this.economy = plugin.getEconomy();
        this.configManager = BM.getPluginConfig();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Boolean console = false;
        if (!(sender instanceof Player)) {
            console = true;
        }

        if (!console) {
            Player player = (Player) sender;
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (NoteUtility.isBanknote(itemInHand)) {
                String noteUUID = NoteUtility.getBanknoteUUID(itemInHand);
                ResultSet success = database.select(
                        "banknotes",
                        "created_at",
                        "uuid = ?",
                        noteUUID
                );

                try {
                    if (success != null && success.next()) {
                        Timestamp createdAt = success.getTimestamp("created_at");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMM d, yyyy 'at' hh:mmaa");
                        String formattedDate = dateFormat.format(new Date(createdAt.getTime()));

                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &fThis banknote was created at: " + formattedDate));
                    } else {
                        System.out.println("No records found or query failed.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (success != null) {
                            success.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }
                int playerBanknotesCreated = database.getPlayerCreatedBanknotes(player.getUniqueId());
                int playerBanknotesClaimed = database.getPlayerClaimedBanknotes(player.getUniqueId());

                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6&lBetterNotes &8| &7Info:"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8| &fYou have created &6" + playerBanknotesCreated + " banknotes!"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8| &fYou have claimed &6" + playerBanknotesClaimed + " banknotes!"));
        }

        if (console) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6&lBetterNotes &8| &7Info:"));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&8| &fThere is currently &6" + configManager.p() + database.getTotalUnclaimedAmount()+ "&r in &cunclaimed &6banknotes!"));
        return true;
    }
}
