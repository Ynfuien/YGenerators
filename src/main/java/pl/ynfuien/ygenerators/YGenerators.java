package pl.ynfuien.ygenerators;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import pl.ynfuien.ygenerators.data.Generators;
import pl.ynfuien.ygenerators.generators.Database;
import pl.ynfuien.ygenerators.hooks.Hooks;
import pl.ynfuien.ygenerators.managers.Lang;
import pl.ynfuien.ygenerators.managers.config.ConfigManager;
import pl.ynfuien.ygenerators.updater.Updater;
import pl.ynfuien.ygenerators.utils.Logger;
import pl.ynfuien.ygenerators.utils.Startup;
import pl.ynfuien.ygenerators.utils.Util;

public final class YGenerators extends JavaPlugin {
    private static YGenerators instance;
    private ConfigManager configManager = new ConfigManager(this);
    private Generators generators = new Generators();
    private Database database = new Database(this);

    private boolean successfulLoad = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        // Set logger prefix
        Logger.setPrefix("&3[&bY&9Generators&3] &f");

        // Register listeners
        Startup.registerListeners(this);
        // Register commands
        Startup.registerCommands();
        // Setup instances for classes
        Startup.setupInstances(this);
        // Load config files
        boolean success = Startup.loadConfigs(this);

        if (!success) return;

        // Load generators
        reloadGenerators();

        // Load lang
        reloadLang();

        if (!Util.isPapiEnabled()) {
            Logger.logWarning("There is no PlaceholderAPI on the server, placeholders won't work!");
        }

        // Load hooks
        Hooks.load(this);

        // Create scheduler to run after server finish startup
        Bukkit.getScheduler().runTask(this, () -> {
            // Load generators from file
            database.loadFromFile();
        });

        Logger.log("Plugin successfully &aenabled&f!");

        // Welcome screen
        Logger.log("&9╔═════════════════════════════════╗");
        Logger.log("");
        logCentered("&bY&9Generators &fby &7Ynfuien", 35);
        logCentered(String.format("&bVersion: &3%s", getDescription().getVersion()), 35);
        logCentered(String.format("&bServer version: &3%s", Bukkit.getMinecraftVersion()), 35);
        logCentered(String.format("&bGenerators: &3%d", generators.getAll().size()), 35);
        logCentered(String.format("&bPlaced generators: &3%d", database.getAll().size()), 35);
        logCentered("&bPAPI hook: " + (Hooks.isPapiHookEnabled() ? "&aenabled" : "&cdisabled"), 35);
        logCentered("&bSS2 hook: " + (Hooks.isSS2HookEnabled() ? "&aenabled" : "&cdisabled"), 35);
        Logger.log("");
        Logger.log("&9╚═════════════════════════════════╝");

        // If checking updates is enabled
        if (getConfig().getBoolean("check-updates")) {
            // Create updater instance
            Updater updater = new Updater(this, getDescription().getAuthors().get(0), getName());
            // Check for update
            updater.checkUpdate();
        }

        successfulLoad = true;
    }

    private void logCentered(String message, int width) {
        int messageLength = message.replaceAll("&[0-9a-fA-Fl-o-L-O]", "").length();
        int sideSpaceLength = (width - messageLength) / 2;
        String sideSpace = " ".repeat(sideSpaceLength);

        Logger.log(sideSpace + message + sideSpace);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // Save database to file
        if (successfulLoad) {
            Logger.log("Saving generators database to file...");
            database.stopUpdateInterval();
            database.saveToFile();
            Logger.log("Generators database saved!");
        }

        Logger.log("Plugin successfully &cdisabled&f!");
    }

    // Reloads generators
    public boolean reloadGenerators() {
        // Remove current generator recipes
        this.generators.removeRecipes();

        // Get generators config
        FileConfiguration generators = configManager.getConfig("generators.yml");
        // Get double drop config
        FileConfiguration doubledrop = configManager.getConfig("doubledrop.yml");

        // Load generators from config
        this.generators.loadFromConfig(generators, getConfig(), doubledrop);

        // Load generator recipes
        this.generators.loadRecipes();
        return true;
    }

    // Reloads lang
    public void reloadLang() {
        // Get lang config
        FileConfiguration config = configManager.getConfig("lang.yml");

        // Reload lang
        Lang.reloadLang(config);
    }

    // Gets plugin instance
    public static YGenerators getInstance() {
        return instance;
    }

    // Gets config manager
    public ConfigManager getConfigManager() {
        return configManager;
    }

    // Gets generators instance
    public Generators getGenerators() {
        return generators;
    }

    // Gets generators instance
    public Database getDatabase() {
        return database;
    }
}
