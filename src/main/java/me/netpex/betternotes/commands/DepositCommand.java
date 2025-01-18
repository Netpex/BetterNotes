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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DepositCommand implements CommandExecutor {

    private BetterNotes BM;
    private Database database;
    private NoteUtility noteUtility;
    private Economy economy;
    private ConfigManager configManager;

    public DepositCommand(BetterNotes plugin) {
        this.BM = plugin;
        this.database = plugin.getDatabase();
        this.noteUtility = plugin.getNoteUtil();
        this.economy = plugin.getEconomy();
        this.configManager = BM.getPluginConfig();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &cOnly players can use this command!"));
            return true;
        }

        Player player = (Player) sender;
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!NoteUtility.isBanknote(itemInHand)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &cYou are not holding a banknote!"));
            return true;
        }

        String noteUUID = NoteUtility.getBanknoteUUID(itemInHand);
        ResultSet success = database.select(
                "banknotes",
                "created_at, amount, claimed",
                "uuid = ?",
                noteUUID
        );

        try {
            if (success != null && success.next()) {

                player.getInventory().setItemInMainHand(null);
                if (success.getBoolean("claimed")) {

                    player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes\n&cDuplicated banknote detected!"));
                    return true;
                }

                boolean updated = database.update(
                        "banknotes",
                        "claimed = ?, claimed_by = ?, claimed_at = ?",
                        "uuid = ?",
                        true, player.getUniqueId(), new Timestamp(System.currentTimeMillis()), noteUUID
                );

                if (!updated) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &e&l&oCritical &edatabase error. &ePlease contact server owner."));
                    return  true;
                }

               economy.depositPlayer(player, success.getDouble("amount"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &aYou have successfully deposited &6" + configManager.p() + success.getDouble("amount")+ "!"));
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

        return  true;
    }
}
