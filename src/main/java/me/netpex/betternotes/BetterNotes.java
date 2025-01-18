package me.netpex.betternotes;

import me.netpex.betternotes.commands.Manager;
import me.netpex.betternotes.listeners.DepositListener;
import me.netpex.betternotes.listeners.JoinListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class BetterNotes extends JavaPlugin {

    private static BetterNotes BM;
    private static Database database;
    private static ConfigManager configManager;
    private static NoteUtility noteUtility;

    private static Economy economy = null;

    @Override
    public void onEnable() {
        BM = this;

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',"&6&lBetterNotes &8| &c&lVault not found, disabling."));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        configManager = new ConfigManager(getDataFolder());
        configManager.saveDefaultConfig();
        File databaseFile = new File(getDataFolder(), "banknotes.db");

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        database = new Database(databaseFile.getAbsolutePath());
        database.initialize();

        noteUtility = new NoteUtility(this);
        this.getCommand("betternote").setExecutor(new Manager(this));
        this.getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        this.getServer().getPluginManager().registerEvents(new DepositListener(this), this);

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',"&6&lBetterNotes &8| &c&lCould not get economy, disabling."));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        economy = rsp.getProvider();

        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',"&6&lBetterNotes &8| &7Plugin &a&lenabled!"));
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',"&6&lBetterNotes &8| &7There is currently " + configManager.p() + database.getTotalUnclaimedAmount()+ " in unclaimed banknotes!"));
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }
    }

    public static BetterNotes get() {
        return BM;
    }
    public static Database getDatabase() {
        return database;
    }
    public static ConfigManager getPluginConfig() {
        return configManager;
    }
    public static NoteUtility getNoteUtil() {
        return noteUtility;
    }
    public Economy getEconomy() {
        return economy;
    }
}
