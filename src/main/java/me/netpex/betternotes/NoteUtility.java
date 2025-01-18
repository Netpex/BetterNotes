package me.netpex.betternotes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class NoteUtility {

    private static BetterNotes BM;
    private static NamespacedKey key;
    private static ConfigManager configManager;

    public NoteUtility(BetterNotes plugin) {
        this.BM = plugin;
        this.key = new NamespacedKey(plugin, "banknote_uuid");
        this.configManager = this.BM.getPluginConfig();
    }

    public ItemStack createBanknote(double amount, String creator) {
        boolean customDenominations = configManager.getPluginConfig().getBoolean("withdraw.customDenominations");

        Material material = Material.valueOf(configManager.getPluginConfig().getString("banknote.default.material"));
        int modelData = (int) configManager.getPluginConfig().getDouble("banknote.default.modelData");

        if (customDenominations) {
            List<?> denominations = configManager.getPluginConfig().getList("withdraw.denominations");
            for (Object obj : denominations) {
                if (obj instanceof Map) {
                    Map<?, ?> denomMap = (Map<?, ?>) obj;
                    double denomAmount = Double.parseDouble(denomMap.keySet().iterator().next().toString());
                    if (denomAmount == amount) {
                        material = Material.valueOf((String) denomMap.get("material"));
                        modelData = (int) denomMap.get("modelData");
                        break;
                    }
                }
            }
        }

        ItemStack item = new ItemStack(material, 1);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String uuid = UUID.randomUUID().toString();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, uuid);
        meta.setCustomModelData(modelData);

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', configManager.getPluginConfig().getString("banknote.default.name")));

        List<String> lore = configManager.getPluginConfig().getStringList("banknote.default.lore");
        lore = lore.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("%amount%", configManager.p() + String.valueOf(amount))
                        .replace("%creator%", creator)))
                .collect(Collectors.toList());
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isBanknote(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    public static String getBanknoteUUID(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(key, PersistentDataType.STRING);
    }

}
