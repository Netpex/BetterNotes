package me.netpex.betternotes.listeners;

import me.netpex.betternotes.BetterNotes;
import me.netpex.betternotes.ConfigManager;
import me.netpex.betternotes.Database;
import me.netpex.betternotes.NoteUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class DepositListener  implements Listener {

    private BetterNotes BM;
    private Database database;
    private ConfigManager configManager;
    private final NoteUtility noteUtility;

    public DepositListener(BetterNotes plugin) {
        this.BM = plugin;
        this.database = this.BM.getDatabase();
        this.configManager = this.BM.getPluginConfig();
        this.noteUtility = plugin.getNoteUtil();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = player.getInventory().getItemInMainHand();

        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && NoteUtility.isBanknote(item)) {

            player.chat("/betternote deposit");
            return;
        }
    }
}
