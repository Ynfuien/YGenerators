package pl.ynfuien.ygenerators;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ydevlib.config.ConfigHandler;
import pl.ynfuien.ydevlib.config.ConfigObject;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ydevlib.utils.YamlComparer;
import pl.ynfuien.ygenerators.commands.doubledrop.DoubledropCommand;
import pl.ynfuien.ygenerators.commands.main.MainCommand;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.generator.GeneratorItem;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerators;
import pl.ynfuien.ygenerators.hooks.Hooks;
import pl.ynfuien.ygenerators.listeners.*;
import pl.ynfuien.ygenerators.storage.Database;
import pl.ynfuien.ygenerators.storage.MysqlDatabase;
import pl.ynfuien.ygenerators.storage.SqliteDatabase;

import java.util.HashMap;
import java.util.List;

public final class YGenerators extends JavaPlugin {
    private static YGenerators instance;

    private final ConfigHandler configHandler = new ConfigHandler(this);
    private ConfigObject config;

    private final Generators generators = new Generators(this);
    private final Doubledrop doubledrop = new Doubledrop(this);
    private final PlacedGenerators placedGenerators = new PlacedGenerators(this);

    private Database database;

    @Override
    public void onEnable() {
        instance = this;

        // Set logger prefix
        YLogger.setup("<dark_aqua>[<aqua>Y<blue>Generators<dark_aqua>] <white>", getComponentLogger());
        YLogger.setDebugging(true);

        // Configuration
        loadConfigs();
        loadLang();
        config = configHandler.getConfigObject(ConfigName.CONFIG);

        // Database
        if (!loadDatabase()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Generators and doubledrop
        GeneratorItem.NSKey.setup(this);
        loadGenerators();
        doubledrop.load(database);

        // Placed generators (database)
        if (!placedGenerators.load(database)) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

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
        doubledrop.stopInterval();
        placedGenerators.stopUpdateInterval();
        placedGenerators.save();

        if (database != null) database.close();

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

    private Database getDatabase(ConfigurationSection config) {
        String type = config.getString("type");
        if (type.equalsIgnoreCase("sqlite")) return new SqliteDatabase(this);
        else if (type.equalsIgnoreCase("mysql")) return new MysqlDatabase(this);

        YLogger.error("Database type is incorrect! Available database types: sqlite, mysql");
        return null;
    }

    private void loadConfigs() {
        configHandler.load(ConfigName.CONFIG, true, false, List.of("vanilla-generators.blocks"));
        configHandler.load(ConfigName.LANG, true, true);
        configHandler.load(ConfigName.GENERATORS, false);
    }

    private void loadLang() {
        FileConfiguration config = configHandler.getConfig(ConfigName.LANG);
        Lang.loadLang(config);
    }

    private boolean loadDatabase() {
        ConfigurationSection dbConfig = config.getConfig().getConfigurationSection("database");
        database = getDatabase(dbConfig);

        if (database == null) return false;
        if (!database.setup(dbConfig)) return false;

        return database.createTables();
    }

    public void loadGenerators() {
        generators.removeRecipes();
        generators.load(config.getConfig(), configHandler.getConfig(ConfigName.GENERATORS));
        generators.loadRecipes();
    }

    public boolean reloadPlugin() {
        boolean fullSuccess = true;

        // Stop intervals
        doubledrop.stopInterval();
        placedGenerators.stopUpdateInterval();

        // Save data
        placedGenerators.save();

        // Reload configs
        if (!configHandler.reloadAll()) fullSuccess = false;
        loadLang();

        // Database reload if needed
        ConfigurationSection oldConfig = database.getConfig();
        ConfigurationSection newConfig = config.getConfig().getConfigurationSection("database");
        HashMap<String, Object> changes = YamlComparer.getChangedFields(oldConfig, newConfig);
        if (!changes.isEmpty()) {
            if (changes.size() == 1 && changes.containsKey("update-interval")) {
                database.setUpdateInterval(newConfig.getInt("update-interval"));
            } else {
                loadDatabase();
            }
        }

        loadGenerators();

        doubledrop.load(database);
        placedGenerators.load(database);
        return fullSuccess;
    }

    public static YGenerators getInstance() {
        return instance;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return config.getConfig();
    }

    public Generators getGenerators() {
        return generators;
    }

    public Doubledrop getDoubledrop() {
        return doubledrop;
    }

    public PlacedGenerators getPlacedGenerators() {
        return placedGenerators;
    }

    public Database getDatabase() {
        return database;
    }
}
