package me.bright.townywars.configs;

import java.io.File;
import java.io.IOException;

public abstract class WConfig {

    protected void fileMaker(File file) {
        if(!file.exists()) {
            try {
                File parentDir = file.getParentFile();
                parentDir.mkdirs();
                file.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
