package pl.ynfuien.ygenerators.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.core.generator.GeneratorRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Generators {
    private final YGenerators instance;
    private final HashMap<String, Generator> generators = new HashMap<>();

    private int maxInChunk = -1;
    private List<String> disabledWorlds = new ArrayList<>();
    private List<Double> alertDurability = new ArrayList<>();
    private InteractionOptions pickUp = null;
    private InteractionOptions checkStatus = null;
    private VanillaGenerators vanillaGenerators = null;

    public Generators(YGenerators instance) {
        this.instance = instance;
    }

    public boolean load(FileConfiguration config, FileConfiguration generatorsConfig) {
        if (config == null) return false;
        if (generatorsConfig == null) return false;

        ConfigurationSection settings = config.getConfigurationSection("generators");

        maxInChunk = settings.getInt("max-in-chunk");
        disabledWorlds = settings.getStringList("disabled-worlds");
        alertDurability = settings.getDoubleList("alert-durability");

        // Pick up
        ConfigurationSection pickUpSection = settings.getConfigurationSection("pick-up");
        if (pickUpSection.getBoolean("enabled")) {
            pickUp = new InteractionOptions(pickUpSection);
        }

        // Check status
        ConfigurationSection checkStatusSection = settings.getConfigurationSection("check-status");
        if (checkStatusSection.getBoolean("enabled")) {
            checkStatus = new InteractionOptions(checkStatusSection);
        }

        // Get vanilla generators settings
        ConfigurationSection vanillaGeneSettings = config.getConfigurationSection("vanilla-generators");
        vanillaGenerators = new VanillaGenerators(vanillaGeneSettings);

        logInfo("Started loading generators...");

        Set<String> geneNames = generatorsConfig.getKeys(false);

        this.generators.clear();

        // Loop all generators
        for (String path : geneNames) {
            // Set name in lower case
            String name = path.toLowerCase();

            // Check if generator has correct name
            if (!name.matches("[a-z0-9\\-_]+")) {
                logError(String.format("Name '%s' is incorrect! Generator won't be loaded", name));
                continue;
            }

            ConfigurationSection geneConfig = generatorsConfig.getConfigurationSection(path);
            if (geneConfig == null) {
                logError(String.format("Generator '%s' couldn't be loaded because it doesn't have configuration section!", name));
                continue;
            }

            Generator gene = new Generator(this, name);
            boolean success = gene.loadFromConfigSection(geneConfig);

            if (!success) {
                logError(String.format("Generator '%s' couldn't be loaded!", name));
                continue;
            }

            this.generators.put(name, gene);
            logInfo(String.format("Generator '%s' successfully loaded!", name));
        }

        logInfo("Successfully loaded generators!");
        return true;
    }

    private void logError(String message) {
        YLogger.warn("[Generators] " + message);
    }
    private void logInfo(String message) {
        YLogger.info("[Generators] " + message);
    }

    @Nullable
    public Generator get(String name) {
        return generators.get(name);
    }

    public boolean has(String name) {
        return generators.containsKey(name);
    }

    @NotNull
    public HashMap<String, Generator> getAll() {
        return generators;
    }

    public int getMaxInChunk() {
        return maxInChunk;
    }

    @NotNull
    public List<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    @NotNull
    public List<Double> getAlertDurability() {
        return alertDurability;
    }

    @Nullable
    public InteractionOptions getPickUp() {
        return pickUp;
    }

    @Nullable
    public InteractionOptions getCheckStatus() {
        return checkStatus;
    }

    @NotNull
    public VanillaGenerators getVanillaGenerators() {
        return vanillaGenerators;
    }


    // Loads generator recipes
    public void loadRecipes() {
        YLogger.info("[Generator-Recipes] Loading generator recipes...");

        // Loop through all generators
        for (Generator gene : generators.values()) {
            // Get generator recipe
            GeneratorRecipe recipe = gene.getRecipe();
            // Skip if generator doesn't have recipe
            if (recipe == null) continue;

            boolean success = recipe.registerRecipe(this);
            if (!success) {
                YLogger.warn(String.format("[Generator-Recipes] Recipe for generator '%s' couldn't be loaded!", gene.getName()));
                continue;
            }

            YLogger.info(String.format("[Generator-Recipes] Recipe for generator '%s' successfully loaded!", gene.getName()));
        }


        YLogger.info("[Generator-Recipes] Generator recipes successfully loaded!");
    }

    // Remove generator recipes
    public void removeRecipes() {
        for (Generator gene : generators.values()) {
            GeneratorRecipe recipe = gene.getRecipe();

            if (recipe == null) continue;

            NamespacedKey namespacedKey = new NamespacedKey(YGenerators.getInstance(), gene.getName());
            Bukkit.removeRecipe(namespacedKey);
        }
    }

    // Gets whether generator is disabled in provided location
    public boolean isDisabledInLocation(String geneName, Location loc) {
        // Return true if generators are globally disabled in location's world
        if (disabledWorlds.contains(loc.getWorld().getName())) return true;

        // Get generator by name
        Generator gene = get(geneName);
        // Return false if generator doesn't exist
        if (gene == null) return false;

        // Return whether generator is disabled in provided location
        return gene.isDisabledInLocation(loc);
    }
}
