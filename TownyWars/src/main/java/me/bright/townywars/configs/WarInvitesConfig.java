package me.bright.townywars.configs;

import me.bright.townywars.TownyWars;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class WarInvitesConfig extends WConfig {

    private FileConfiguration conf;
    private File file;

    public WarInvitesConfig() {
        initialize();
    }

    private void initialize() {
        file = new File(TownyWars.getPlugin().getDataFolder() + "/warinvites.yml");
        fileMaker(file);
        conf = YamlConfiguration.loadConfiguration(file);
        save();
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
