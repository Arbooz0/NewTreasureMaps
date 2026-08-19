package kawun.new_treasure_maps.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kawun.new_treasure_maps.Constants;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config", Constants.MOD_ID + "_config.json");

    public static ConfigData config = new ConfigData();


    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                config = GSON.fromJson(reader, ConfigData.class);
            } catch (Exception e) {
                Constants.LOG.error("Error load config: " + e.getMessage());
                save();
            }
        } else {
            save();
        }
    }


    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
                Constants.LOG.info("Config saved");
            }
        } catch (Exception e) {
            Constants.LOG.error("Error save config: " + e.getMessage());
        }
    }

}
