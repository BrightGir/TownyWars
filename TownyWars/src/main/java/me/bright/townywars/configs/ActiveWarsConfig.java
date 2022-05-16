package me.bright.townywars.configs;

import me.bright.townywars.TownyWars;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ActiveWarsConfig extends WConfig {

    private FileConfiguration conf;
    private File file;

    public ActiveWarsConfig() {
        initialize();
    }

    private void initialize() {
        file = new File(TownyWars.getPlugin().getDataFolder() + "/activewars.yml");
        fileMaker(file);
        conf = YamlConfiguration.loadConfiguration(file);
        values();
        save();
    }

    private void dataConfig(String path, Object value) {
        conf.set(path, value);
    }

    private void values() {

    }

    public FileConfiguration getConfig() {
        return conf;
    }

    public void save() {
        try {
            conf.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
