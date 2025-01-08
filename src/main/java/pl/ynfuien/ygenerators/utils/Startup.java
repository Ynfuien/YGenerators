package pl.ynfuien.ygenerators.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.YGeneratorsAPI;
import pl.ynfuien.ygenerators.commands.doubledrop.DoubledropCommand;
import pl.ynfuien.ygenerators.commands.main.MainCommand;
import pl.ynfuien.ygenerators.listeners.*;
import pl.ynfuien.ygenerators.managers.NBTTags;
import pl.ynfuien.ygenerators.managers.config.ConfigManager;

import java.util.List;

public class Startup {
    // Register plugin listeners
    public static void registerListeners(YGenerators instance) {
        YLogger.info("Registering listeners...");

        Listener[] listeners = new Listener[] {
                new BlockBreakListener(instance),
                new BlockFormListener(instance),
                new BlockPistonExtendListener(instance),
                new BlockPistonRetractListener(instance),
                new BlockPlaceListener(instance),
                new EntityExplodeListener(instance),
                new PlayerInteractListener(instance),
                new PrepareItemCraftListener(instance)
        };

        PluginManager pm = Bukkit.getPluginManager();
        for (Listener listener : listeners) {
            pm.registerEvents(listener, instance);
        }

        YLogger.info("Successfully registered all listeners!");
    }

    // Register plugin commands
    public static void registerCommands() {
        Bukkit.getPluginCommand("ygenerators").setExecutor(new MainCommand());
        Bukkit.getPluginCommand("ygenerators").setTabCompleter(new MainCommand());
        Bukkit.getPluginCommand("doubledrop").setExecutor(new DoubledropCommand());
        Bukkit.getPluginCommand("doubledrop").setTabCompleter(new DoubledropCommand());
    }

    public static void setupInstances(YGenerators instance) {
        NBTTags.setInstance(instance);
        YGeneratorsAPI.setInstance(instance);
    }

    // Load all plugin configs
    public static boolean loadConfigs(YGenerators instance) {
        ConfigManager cm = instance.getConfigManager();

//        cm.get(new ConfigManager.Config("config.yml", Arrays.asList("vanilla-generators.blocks")));
//        cm.get(new ConfigManager.Config("lang.yml", true, true));
//        ConfigManager.Config config = cm.get(new ConfigManager.Config("generators.yml", false, false));
//        if (config == null) return false;
//        cm.get("doubledrop.yml");
//        cm.get("database.yml");

        cm.get("config.yml", List.of("vanilla-generators.blocks"));
        cm.get("lang.yml", true, true);
        ConfigManager.Config config = cm.get("generators.yml", false);
        if (config == null) return false;
        cm.get("doubledrop.yml", true, true);
        cm.get("database.yml");

        return true;
//        cm.getConfig("config.yml", true, false, Arrays.asList("vanilla-generators.blocks"));
//        cm.getConfig("lang.yml", true, true);
//        cm.getConfig("generators.yml");
//        cm.getConfig("doubledrop.yml", true);
//        cm.getConfig("database.yml", true, true);
    }
}
