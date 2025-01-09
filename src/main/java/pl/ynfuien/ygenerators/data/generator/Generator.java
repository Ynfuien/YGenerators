package pl.ynfuien.ygenerators.data.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ygenerators.data.Generators;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.*;

public class Generator {
    private final Generators generators;
    private final String name;
    private String displayname;
    private GeneratorItem item;
    private boolean canBeBroken = false;
    private boolean doubledropUseMultiplayer = true;
    private double doubledropDurabilityDecrease = 1;
    private double durability = -1;
    private int cooldown = 0;
    private boolean craftingRepair = false;
    private int maxInChunk = -1;
    private List<String> disabledWorlds = new ArrayList<>();
    private Material defaultBlock;
    private HashMap<Material, Double> blocks = new HashMap<>();
    private GeneratorRecipe recipe = null;

    public Generator(Generators generators, String name) {
        this.generators = generators;
        this.name = name;
        // Set displayname to name with uppercase first letter
        displayname = Util.uppercaseFirstLetter(name);
    }

    public boolean loadFromConfigSection(ConfigurationSection config) {
        // Return if config doesn't have default-block or item key
        for (String key : Arrays.asList("item", "default-block")) {
            if (config.contains(key)) continue;

            // Return
            logError(String.format("Missing key '%s'!", key));
            return false;
        }

        // Can be broken
        if (config.contains("displayname")) {
            displayname = config.getString("displayname");
        }


        // Generator item
        {
            // Get item config section
            ConfigurationSection itemConfigSection = config.getConfigurationSection("item");
            // Create new generator item object
            item = new GeneratorItem(this);
            // Load generator item from config section and get success state
            boolean success = item.loadFromConfigSection(itemConfigSection);
            // Return if loading failed
            if (!success) {
                logError("Item couldn't be loaded!");
                return false;
            }
        }

        // Can be broken
        if (config.contains("can-be-broken")) {
            canBeBroken = config.getBoolean("can-be-broken");
        }

        // Durability
        if (config.contains("durability")) {
            try {
                durability = Integer.parseInt(config.getString("durability"));
            } catch (NumberFormatException e) {
                logError("Durability number is incorect!");
                return false;
            }
        }

        // Cooldown
        if (config.contains("cooldown")) {
            try {
                cooldown = Integer.parseInt(config.getString("cooldown"));
            } catch (NumberFormatException e) {
                logError("Cooldown number is incorect!");
                return false;
            }

            if (cooldown < 0) {
                logError("Cooldown can't be lower than 0!");
                return false;
            }
        }

        // Default block
        defaultBlock = Material.matchMaterial(config.getString("default-block"));
        if (defaultBlock == null) {
            logError("Default block is incorrect!");
            return false;
        }

        // Use double drop multiplayer
        if (config.contains("doubledrop.use-multiplayer")) {
            // Whether use double drop
            doubledropUseMultiplayer = config.getBoolean("doubledrop.use-multiplayer");
        }

        // Double drop durability decrease
        if (config.contains("doubledrop.durability-decrease")) {
            doubledropDurabilityDecrease = config.getDouble("doubledrop.durability-decrease");
        }

        // Crafting repair
        if (config.contains("crafting-repair")) {
            craftingRepair = config.getBoolean("crafting-repair");
        }

        // Max generators in chunk
        if (config.contains("max-in-chunk")) {
            maxInChunk = config.getInt("max-in-chunk");
        }

        // Disabled worlds
        if (config.contains("disabled-worlds")) {
            disabledWorlds = config.getStringList("disabled-worlds");
        }

        // Blocks
        if (config.contains("blocks")) {
            ConfigurationSection blocksSection = config.getConfigurationSection("blocks");

            // If blocks is section
            if (blocksSection != null) {
                // Get blocks
                Set<String> blocksSet = blocksSection.getKeys(false);

                // Loop blocks
                for (String block : blocksSet) {
                    // Get block material
                    Material material;
                    try {
                        material = Material.valueOf(block.toUpperCase().trim());

                        // Skip if block isn't block
                        if (!material.isBlock()) {
                            logError(String.format("[%s] Provided material isn't block!", material));
                            continue;
                        }

                        // Skip if block is air
                        if (material.isAir()) {
                            logError(String.format("[%s] Provided material is air!", material));
                            continue;
                        }
                    } catch (IllegalArgumentException e) {
                        logError(String.format("[%s] Provided material is incorrect!", block));
                        continue;
                    }

                    // Get chance for block
                    double chance = blocksSection.getDouble(block);
                    // Skip if chance is lower or equal to 0
                    if (chance <= 0) {
                        logError(String.format("[%s] Chance can't be lower or equal to 0!", block));
                        continue;
                    }

                    blocks.put(material, chance);
                }
            }
        }

        // Recipe
        if (config.contains("recipe")) {
            // Get config section for recipe
            ConfigurationSection recipeConfigSection = config.getConfigurationSection("recipe");
            // If config section isn't null
            if (recipeConfigSection != null) {
                // Create new recipe
                recipe = new GeneratorRecipe(this);
                // Load recipe from config section and get result status
                boolean success = recipe.loadFromConfigSection(recipeConfigSection);
                // Return if loading failed
                if (!success) {
                    recipe = null;
                    logError("Recipe couldn't be loaded!");
                    return false;
                }
            }
        }

        return true;
    }

    // Gets whether generator is disabled in provided location
    public boolean isDisabledInLocation(Location loc) {
        // Get location's world name
        String worldName = loc.getWorld().getName();

        // Return true if world is disabled for all generators
        if (generators.getDisabledWorlds().contains(worldName)) return true;
        // Return true if world is disabled for this generator
        if (disabledWorlds.contains(worldName)) return true;

        return false;
    }

    // Logs error
    private void logError(String message) {
        YLogger.warn(String.format("[Generator-%s] %s", name, message));
    }

    // Gets generators instance
    @NotNull
    public Generators getGenerators() {
        return generators;
    }

    // Gets generator name
    @NotNull
    public String getName() {
        return name;
    }

    // Gets generator displayname
    @NotNull
    public String getDisplayname() {
        return displayname;
    }

    // Gets generator item
    @NotNull
    public GeneratorItem getItem() {
        return item;
    }

    // Gets durability
    public double getDurability() {
        return durability;
    }

    // Gets can be broken
    public boolean canBeBroken() {
        return canBeBroken;
    }

    // Gets cooldown
    public int getCooldown() {
        return cooldown;
    }

    // Gets default block
    @NotNull
    public Material getDefaultBlock() {
        return defaultBlock;
    }

    // Gets crafting repair
    public boolean getCraftingRepair() {
        return craftingRepair;
    }

    // Gets max generators in chunk
    public int getMaxInChunk() {
        return maxInChunk;
    }

    // Gets disabled worlds
    @NotNull
    public List<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    // Gets use doubledrop
    public boolean getDoubledropUseMultiplayer() {
        return doubledropUseMultiplayer;
    }

    // Gets doubledrop affect durability
    public double getDoubledropDurabilityDecrease() {
        return doubledropDurabilityDecrease;
    }

    // Gets blocks
    @NotNull
    public HashMap<Material, Double> getBlocks() {
        return blocks;
    }

    // Gets recipe
    @Nullable
    public GeneratorRecipe getRecipe() {
        return recipe;
    }
}
