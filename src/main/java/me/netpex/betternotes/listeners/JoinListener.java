package me.netpex.betternotes.listeners;

import me.netpex.betternotes.BetterNotes;
import me.netpex.betternotes.ConfigManager;
import me.netpex.betternotes.Database;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final BetterNotes BM;
    private Database database;
    private final ConfigManager configManager;

    public JoinListener(BetterNotes plugin) {
        this.BM = plugin;
        this.database = this.BM.getDatabase();
        this.configManager = this.BM.getPluginConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isOp()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6&lBetterNotes &8| &fThere is currently &6" + configManager.p() + database.getTotalUnclaimedAmount()+ "&f in unclaimed banknotes!"));
        }
    }

}
