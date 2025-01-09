package pl.ynfuien.ygenerators;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.ynfuien.ydevlib.config.ConfigHandler;
import pl.ynfuien.ydevlib.config.ConfigObject;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ygenerators.commands.doubledrop.DoubledropCommand;
import pl.ynfuien.ygenerators.commands.main.MainCommand;
import pl.ynfuien.ygenerators.data.Doubledrop;
import pl.ynfuien.ygenerators.data.Generators;
import pl.ynfuien.ygenerators.generators.Database;
import pl.ynfuien.ygenerators.hooks.Hooks;
import pl.ynfuien.ygenerators.listeners.*;
import pl.ynfuien.ygenerators.managers.config.ConfigManager;

import java.util.HashMap;

public final class YGenerators extends JavaPlugin {
    private static YGenerators instance;

    private final ConfigHandler configHandler = new ConfigHandler(this);
    private ConfigObject config;

    private final Generators generators = new Generators(this);
    private final Doubledrop doubledrop = new Doubledrop(this);

    private final Database database = new Database(this);

    @Override
    public void onEnable() {
        instance = this;

        // Set logger prefix
        YLogger.setup("<dark_aqua>[<aqua>Y<blue>Generators<dark_aqua>] <white>", getComponentLogger());

        // TO DO
        // Register commands, listeners
        // Startup logic, configs, database

        loadConfigs();
        loadLang();
//        config = configHandler.getConfigObject(ConfigName.CONFIG);


//        ConfigurationSection dbConfig = config.getConfig().getConfigurationSection("database");
//        database = getDatabase(dbConfig);
//        if (database != null && database.setup(dbConfig)) database.createNicknamesTable();
//        Storage.setup(this);

        // Load hooks
        Hooks.load(this);

        setupCommands();
        registerListeners();

        // BStats
        new Metrics(this, 24412);

        YLogger.info("Plugin successfully <green>enabled<white>!");
    }

    @Override
    public void onDisable() {
//        if (database != null) database.close();

        YLogger.info("Plugin successfully <red>disabled<white>!");
    }

    private void setupCommands() {
        HashMap<String, CommandExecutor> commands = new HashMap<>();
        commands.put("ygenerators", new MainCommand(this));
        commands.put("doubledrop", new DoubledropCommand(this));

        for (String name : commands.keySet()) {
            CommandExecutor cmd = commands.get(name);

            getCommand(name).setExecutor(cmd);
            getCommand(name).setTabCompleter((TabCompleter) cmd);
        }
    }

    private void registerListeners() {
        Listener[] listeners = new Listener[] {
                new BlockBreakListener(this),
                new BlockFormListener(this),
                new BlockPistonExtendListener(this),
                new BlockPistonRetractListener(this),
                new BlockPlaceListener(this),
                new EntityExplodeListener(this),
                new PlayerInteractListener(this),
                new PrepareItemCraftListener(this)
        };

        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    private void loadLang() {
        // Get lang config
//        FileConfiguration config = configHandler.getConfig(ConfigName.LANG);

        // Reload lang
        Lang.loadLang(config);
    }

    private void loadConfigs() {
//        configHandler.load(ConfigName.CONFIG);
//        configHandler.load(ConfigName.LANG, true, true);
    }

    // Reloads generators
    public boolean reloadGenerators() {
        // Remove current generator recipes
        this.generators.removeRecipes();

//        FileConfiguration generators = configManager.getConfig("generators.yml");

        // Load generators from config
//        this.generators.load(generators, getConfig());

        // Load generator recipes
        this.generators.loadRecipes();
        return true;
    }

    public boolean reloadPlugin() {
        doubledrop.cancelInterval();

        database.stopUpdateInterval();
        database.saveToFile();

//        // Reload all configs
//        configManager.reloadConfigs();
//
//        reloadGenerators();
//        reloadLang();

        database.loadFromFile();
        database.startUpdateInterval(instance.getConfig().getInt("database-update-interval"));

        return true;
    }

    public static YGenerators getInstance() {
        return instance;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public Generators getGenerators() {
        return generators;
    }

    public Doubledrop getDoubledrop() {
        return doubledrop;
    }

    public Database getDatabase() {
        return database;
    }
}
