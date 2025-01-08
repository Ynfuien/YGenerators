package pl.ynfuien.ygenerators.managers.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import pl.ynfuien.ygenerators.managers.config.configupdater.ConfigUpdater;
import pl.ynfuien.ydevlib.messages.YLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class ConfigManager {
    private Plugin plugin;
    private HashMap<String, Config> configs = new HashMap<>();

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
    }

    // Gets file configuration by name
    public FileConfiguration getConfig(String name) {
        if (configs.containsKey(name)) return configs.get(name).getConfig();

        return get(name).getConfig();
    }

//    // Gets file configuration by name in config object
//    public FileConfiguration getConfig(Config config) {
//        return get(config).getConfig();
//    }

    // Gets config object by name
    public Config get(String name) {
        return get(name, true);
    }

    // Gets config object by name
    public Config get(String name, boolean updating) {
        return get(name, updating, false);
    }

    // Gets config object by name
    public Config get(String name, boolean updating, boolean canUseDefault) {
        return get(new Config(name, updating, canUseDefault, new ArrayList<>()));
    }

    // Gets config object by name
    public Config get(String name, List<String> donUpdateKeys) {
        return get(name, false, donUpdateKeys);
    }

    // Gets config object by name
    public Config get(String name, boolean canUseDefault, List<String> donUpdateKeys) {
        return get(new Config(name, true, canUseDefault, donUpdateKeys));
    }

    // Gets config object by name in provided config object
    public Config get(Config config) {
        String name = config.getName();
        if (configs.containsKey(name)) return configs.get(name);

        logInfo("Loading...", name);
        FileConfiguration fileConfig = createConfig(config);

        if (fileConfig == null) {
            logError("Fix the error and then restart server for plugin to work!", name);
            // Disable plugin
            plugin.getPluginLoader().disablePlugin(plugin);

            return null;
        }

        config.setConfig(fileConfig);

        configs.put(name, config);
        logInfo("Successfully loaded!", name);
        return config;
    }

    // Creates config
    private FileConfiguration createConfig(Config options) {
        // Get name
        String name = options.getName();

        // Create file object of config
        File configFile = new File(plugin.getDataFolder(), name);

        // If file doesn't exist create it
        boolean factoryNew = false;
        if (!configFile.exists()) {
            logInfo("Config doesn't exist, creating new...", name);
            configFile.getParentFile().mkdirs();
            plugin.saveResource(name, false);
            factoryNew = true;
        }

        // Try loading config
        FileConfiguration config = new YamlConfiguration();
        try {
            // Load config
            config.load(configFile);
            // Return if config was just created
            if (factoryNew) return config;
            // Return if config shouldn't be updated
            if (!options.getUpdating()) return config;

            // Get default config
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(name), StandardCharsets.UTF_8));

            // Get whether config is missing some keys
            boolean isMissingKeys = ((Supplier<Boolean>) () -> {
                for (String key : defaultConfig.getKeys(true)) {
                    if (config.contains(key)) continue;
                    if (options.getDontUpdateKeys().contains(key)) continue;

                    boolean missing = true;
                    for (String dontUpdateKey : options.getDontUpdateKeys()) {
                        if (key.startsWith(dontUpdateKey+".")) {
                            missing = false;
                            break;
                        }
                    }

                    if (missing) return true;
                }
                return false;
            }).get();

            // Return if loaded config isn't missing any key
            if (!isMissingKeys) return config;

            logError("Config is missing some keys, updating..", name);

            // Get date
            Date date = new Date();
            // Create date formatter
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");

            // Split name at dot
            String[] split = name.split("\\.");
            // Create name for old config in format: <name>-old_<date>.<extension>
            String oldConfigName = String.format("%s-old_%s.%s", split[0], formatter.format(date), split[1]);

            logError(String.format("Old file will be saved as %s", oldConfigName), name);

            // Create file object for old config
            File oldConfig = new File(plugin.getDataFolder(), oldConfigName);

            // Copy existing config to old config path
            Files.copy(configFile.toPath(), oldConfig.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Update existing config
            ConfigUpdater.update(plugin, name, configFile);

            // Load updated config
            config.load(configFile);
            // And return it
            return config;
        } catch (IOException | InvalidConfigurationException e) {
            // Print stack trace
            e.printStackTrace();
            logError("An error occurred while loading config from file!", name);

            // If can't be used default config
            if (!options.getCanUseDefault()) return null;

            logError("Will be used default one...", name);
            // Get default config and return it
            return YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(name), StandardCharsets.UTF_8));
        }
    }

    // Reloads configs
    public void reloadConfigs() {
        // Loop through configs
        for (Config config : configs.values()) {
            // Get file configuration
            FileConfiguration fileConfig = createConfig(config);

            // If config couldn't be loaded
            if (fileConfig == null) {
                String name = config.getName();
                logError("Config couldn't be reloaded!", name);
                continue;
            }

            // Set new config
            config.setConfig(fileConfig);
        }
    }

    // Logs info message
    private void logInfo(String message, String name) {
        YLogger.info(String.format("[Configs-%s] %s", name, message));
    }

    // Logs error message
    private void logError(String message, String name) {
        YLogger.warn(String.format("[Configs-%s] %s", name, message));
    }

    // Config class
    public static class Config {
        private final String name;
        private FileConfiguration config;
        private final boolean updating;
        private final boolean canUseDefault;
        private final List<String> dontUpdateKeys;

        public Config(String name, boolean updating, boolean canUseDefault, List<String> dontUpdateKeys) {
            this.name = name;
            this.updating = updating;
            this.canUseDefault = canUseDefault;
            this.dontUpdateKeys = dontUpdateKeys;
        }

        // Gets name
        public String getName() {
            return name;
        }

        // Gets file configuration
        public FileConfiguration getConfig() {
            return config;
        }

        // Sets file configuration
        public void setConfig(FileConfiguration config) {
            this.config = config;
        }

        // Gets whether config should be updated
        public boolean getUpdating() {
            return updating;
        }

        // Gets whether config can use default
        public boolean getCanUseDefault() {
            return canUseDefault;
        }

        // Gets dont update keys
        public List<String> getDontUpdateKeys() {
            return dontUpdateKeys;
        }
    }
}
