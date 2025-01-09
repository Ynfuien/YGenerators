package pl.ynfuien.ygenerators.utils;

import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.managers.config.ConfigManager;

import java.util.List;

public class Startup {
    // Load all plugin configs
    public static boolean loadConfigs(YGenerators instance) {
        ConfigManager cm = instance.getConfigManager();

        cm.get("config.yml", List.of("vanilla-generators.blocks"));
        cm.get("lang.yml", true, true);
        ConfigManager.Config config = cm.get("generators.yml", false);
        if (config == null) return false;
        cm.get("doubledrop.yml", true, true);
        cm.get("database.yml");

        return true;
    }
}
