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

    /**
     * Internal method for loading this class.
     */
    public boolean load(FileConfiguration config, FileConfiguration generatorsConfig) {
        if (config == null) return false;
        if (generatorsConfig == null) return false;

        ConfigurationSection settings = config.getConfigurationSection("generators");

        maxInChunk = settings.getInt("max-in-chunk");
        disabledWorlds = settings.getStringList("disabled-worlds");
        alertDurability = settings.getDoubleList("alert-durability");

        // Pick up
        ConfigurationSection pickUpSection = settings.getConfigurationSection("pick-up");
        pickUp = new InteractionOptions(pickUpSection);

        // Check status
        ConfigurationSection checkStatusSection = settings.getConfigurationSection("check-status");
        checkStatus = new InteractionOptions(checkStatusSection);

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
            boolean success = gene.load(geneConfig);

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

    /**
     * Gets a Generator instance by its name.
     * @return Generator or null if not found
     */
    @Nullable
    public Generator get(String name) {
        return generators.get(name);
    }

    /**
     * Checks whether generator with provided name exists.
     */
    public boolean has(String name) {
        return generators.containsKey(name);
    }

    /**
     * @return All generators in a hashmap
     */
    @NotNull
    public HashMap<String, Generator> getAll() {
        return generators;
    }

    /**
     * @return Plugin instance
     */
    public YGenerators getInstance() {
        return instance;
    }

    /**
     * @return Max amount of any generators placed in a single chunk. Loaded from config.yml
     */
    public int getMaxInChunk() {
        return maxInChunk;
    }

    /**
     * @return List of the disabled worlds in which generators can't be placed
     */
    @NotNull
    public List<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    @NotNull
    public List<Double> getAlertDurability() {
        return alertDurability;
    }

    /**
     * @return Pickup interaction for the generators, configured in config.yml
     */
    public InteractionOptions getPickUp() {
        return pickUp;
    }

    /**
     * @return Check status interaction for the generators, configured in config.yml
     */
    public InteractionOptions getCheckStatus() {
        return checkStatus;
    }

    /**
     * @return Loaded settings about vanilla generators
     */
    @NotNull
    public VanillaGenerators getVanillaGenerators() {
        return vanillaGenerators;
    }


    /**
     * Loads recipes for the generator items.
     */
    public void loadRecipes() {
        YLogger.info("[Generator-Recipes] Loading generator recipes...");

        // Loop through all generators
        for (Generator gene : generators.values()) {
            // Get generator recipe
            GeneratorRecipe recipe = gene.getRecipe();
            // Skip if generator doesn't have recipe
            if (recipe == null) continue;

            boolean success = recipe.registerRecipe();
            if (!success) {
                YLogger.warn(String.format("[Generator-Recipes] Recipe for generator '%s' couldn't be loaded!", gene.getName()));
                continue;
            }

            YLogger.info(String.format("[Generator-Recipes] Recipe for generator '%s' successfully loaded!", gene.getName()));
        }


        YLogger.info("[Generator-Recipes] Generator recipes successfully loaded!");
    }

    /**
     * Removes recipes of the generator items.
     */
    public void removeRecipes() {
        for (Generator gene : generators.values()) {
            GeneratorRecipe recipe = gene.getRecipe();

            if (recipe == null) continue;

            NamespacedKey namespacedKey = new NamespacedKey(instance, gene.getName());
            Bukkit.removeRecipe(namespacedKey);
        }
    }

    /**
     * @return Whether provided generator is disabled in provided location
     */
    public boolean isDisabledInLocation(String generatorName, Location location) {
        if (disabledWorlds.contains(location.getWorld().getName())) return true;

        Generator generator = get(generatorName);
        if (generator == null) return false;

        return generator.isDisabledInLocation(location);
    }
}
