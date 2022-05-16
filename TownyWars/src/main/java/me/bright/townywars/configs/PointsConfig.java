package me.bright.townywars.configs;

import me.bright.townywars.TownyWars;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class PointsConfig extends WConfig {

    private FileConfiguration conf;
    private File file;

    public PointsConfig() {
        initialize();
    }

    private void initialize() {
        file = new File(TownyWars.getPlugin().getDataFolder() + "/points.yml");
        fileMaker(file);
        conf = YamlConfiguration.loadConfiguration(file);
        values();
        save();
    }

    private void dataConfig(String path, Object value) {
        if(conf.get(path) == null) {
            conf.set(path, value);
        }
    }

    private void values() {
        dataConfig("points.player_kill",5);
        dataConfig("points.get_chunk",15);
        dataConfig("points.lost_chunk",15);
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
