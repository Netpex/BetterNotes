package me.netpex.betternotes;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final File configFile;
    private FileConfiguration config;

    public ConfigManager(File dataFolder) {
        this.configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            try {
                dataFolder.mkdirs();
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveDefaultConfig() {
        try {
            if (!configFile.exists()) {
                config.save(configFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String p() {

        return config.getString("banknote.currencyPrefix");
    }
}
