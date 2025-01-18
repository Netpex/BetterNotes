package me.netpex.betternotes.commands;

import me.netpex.betternotes.BetterNotes;
import me.netpex.betternotes.ConfigManager;
import me.netpex.betternotes.Database;
import me.netpex.betternotes.NoteUtility;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WithdrawCommand implements CommandExecutor  {

    private BetterNotes BM;
    private Database database;
    private NoteUtility noteUtility;
    private Economy economy;
    private ConfigManager configManager;

    public WithdrawCommand(BetterNotes plugin) {
        this.BM = plugin;
        this.database = plugin.getDatabase();
        this.noteUtility = plugin.getNoteUtil();
        this.economy = plugin.getEconomy();
        this.configManager = BM.getPluginConfig();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;
        Boolean others = false;
        if (!(sender instanceof Player) || (sender.hasPermission("betternote.withdraw.others") && args.length == 3)) {
            player = Bukkit.getPlayer(args[2]);
            others = true;
        } else {
            player = (Player) sender;
        }

        if (!sender.hasPermission("betternote.override.maxunclaimednotes") && database.getPlayerCreatedBanknotesUnclaimed(player.getUniqueId()) > configManager.getPluginConfig().getInt("withdraw.maxUnClaimedNotes")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &cYou have reached the maximum unclaimed notes!"));
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &cYou must specify an amount!"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &cInvalid amount specified!"));
            return true;
        }

        boolean customDenominations = configManager.getPluginConfig().getBoolean("withdraw.customDenominations");
        if (customDenominations && !sender.hasPermission("betternote.override.customdenominations")) {
            List<?> denominations = configManager.getPluginConfig().getList("withdraw.denominations");
            boolean isValidAmount = denominations.stream().anyMatch(denomination -> {
                if (denomination instanceof Map) {
                    Map<?, ?> denomMap = (Map<?, ?>) denomination;
                    for (Object key : denomMap.keySet()) {
                        try {
                            if (Double.parseDouble(key.toString()) == amount) {
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            // Log or handle invalid format gracefully
                        }
                    }
                }
                return false;
            });

            if (!isValidAmount) {
                String allowedDenominations = denominations.stream()
                        .map(demon -> ((Map<?, ?>) demon).keySet().iterator().next().toString()) // Cast to Map and get the key
                        .collect(Collectors.joining(", "));

                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&6&lBetterNotes &8| &cInvalid denomination. &6Allowed denominations are:&7 " + allowedDenominations + "&c!"));
                return true;
            }
        }

        if (amount <= 0 ||(amount < configManager.getPluginConfig().getInt("withdraw.minAmount"))) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &cThe amount must be greater than &6" + configManager.getPluginConfig().getInt("withdraw.minAmount") +"&c!"));
            return true;
        }

        if (amount > configManager.getPluginConfig().getInt("withdraw.maxAmount") && !sender.hasPermission("betternote.override.maxamount")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &cThe amount must be less than &6" + configManager.getPluginConfig().getInt("withdraw.maxAmount") +"&c!"));
            return true;
        }

        if (!economy.has(player, amount) && !sender.hasPermission("betternote.override.fundsrequired")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &cYou don't have enough money!"));
            return true;
        }

        economy.withdrawPlayer(player, amount);

        ItemStack banknote = noteUtility.createBanknote(amount, player.getName());

        String uuid = noteUtility.getBanknoteUUID(banknote);
        boolean success = database.insert(
                "banknotes",
                "uuid, amount, creator, created_at",
                uuid, amount, player.getUniqueId(), new Timestamp(System.currentTimeMillis())
        );

        if (!success) {
            economy.depositPlayer(player, amount);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &e&l&oCritical &edatabase error. &ePlease contact server owner."));
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), banknote);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &eYour inventory is full. The banknote has been dropped at your location!"));
        } else {
            if (sender instanceof Player) {((Player) sender).getPlayer().getInventory().addItem(banknote);} else {player.getInventory().addItem(banknote);}
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &aYou have withdrawn a banknote for &6" + configManager.p() + amount + "&a!"));
        }

        if (others) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lBetterNotes &8| &dYou have withdrawn a banknote for &6" + configManager.p() + amount + "&a on behalf of &6" + args[2] + "&a!"));
        }

        return true;
    }

}
