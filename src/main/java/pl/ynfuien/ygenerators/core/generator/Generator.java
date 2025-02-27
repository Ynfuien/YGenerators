package pl.ynfuien.ygenerators.core.generator;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ydevlib.utils.DoubleFormatter;
import pl.ynfuien.ygenerators.core.Generators;

import java.util.*;

public class Generator {
    private final Generators generators;
    private final String name;
    private String displayName;

    private GeneratorItem item;

    private boolean doubledropUseMultiplayer = true;
    private double doubledropDurabilityDecrease = 1;

    private boolean canBeBroken = false;
    private double durability = -1;
    private int cooldown = 0;
    private boolean craftingRepair = false;
    private int maxInChunk = -1;

    private List<String> disabledWorlds = new ArrayList<>();
    private Material defaultBlock;
    private final HashMap<Material, Double> blocks = new HashMap<>();
    private GeneratorRecipe recipe = null;

    private final HashMap<String, Object> placeholders = new HashMap<>();
    private final static DoubleFormatter df = DoubleFormatter.DEFAULT;

    public Generator(Generators generators, String name) {
        this.generators = generators;
        this.name = name;
        this.displayName = StringUtils.capitalize(name);
    }

    public boolean load(ConfigurationSection config) {
        // Return if config doesn't have default-block or item key
        for (String key : Arrays.asList("item", "default-block")) {
            if (config.contains(key)) continue;

            logError(String.format("Missing key '%s'!", key));
            return false;
        }

        if (config.contains("display-name")) displayName = config.getString("display-name");
        if (config.contains("can-be-broken")) canBeBroken = config.getBoolean("can-be-broken");
        if (config.contains("durability")) durability = config.getInt("durability");

        if (config.contains("cooldown")) cooldown = config.getInt("cooldown");
        if (cooldown < 0) {
            logError("Cooldown can't be lower than 0!");
            return false;
        }

        // Placeholders
        placeholders.put("name", name);
        placeholders.put("display-name", displayName);
        placeholders.put("cooldown", cooldown);
        placeholders.put("full-durability", df.format(durability));

        // Generator item
        ConfigurationSection itemConfig = config.getConfigurationSection("item");
        item = new GeneratorItem(this);
        if (!item.load(itemConfig)) {
            logError("Item couldn't be loaded!");
            return false;
        }

        defaultBlock = Material.matchMaterial(config.getString("default-block"));
        if (defaultBlock == null) {
            logError("Default block is incorrect!");
            return false;
        }
        if (!defaultBlock.isBlock()) {
            logError("Default block isn't a block!");
            return false;
        }

        if (config.contains("doubledrop.use-multiplayer")) {
            doubledropUseMultiplayer = config.getBoolean("doubledrop.use-multiplayer");
        }

        if (config.contains("doubledrop.durability-decrease")) {
            doubledropDurabilityDecrease = config.getDouble("doubledrop.durability-decrease");
        }

        if (config.contains("crafting-repair")) craftingRepair = config.getBoolean("crafting-repair");
        if (config.contains("max-in-chunk")) maxInChunk = config.getInt("max-in-chunk");
        if (config.contains("disabled-worlds")) disabledWorlds = config.getStringList("disabled-worlds");

        // Blocks
        if (config.contains("blocks")) {
            ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
            if (blocksSection == null) {
                logError("Blocks must be a section with blocks!");
                return false;
            }

            Set<String> blocksSet = blocksSection.getKeys(false);

            for (String block : blocksSet) {
                Material material = Material.matchMaterial(block);
                if (material == null) {
                    logError(String.format("[%s] Provided material is incorrect!", block));
                    continue;
                }

                // Skip if block isn't a block
                if (!material.isBlock()) {
                    logError(String.format("[%s] Provided material isn't block!", material));
                    continue;
                }

                // Skip if block is air
                if (material.isAir()) {
                    logError(String.format("[%s] Provided material is air!", material));
                    continue;
                }

                double chance = blocksSection.getDouble(block);
                // Skip if chance is lower or equal to 0
                if (chance <= 0) {
                    logError(String.format("[%s] Chance can't be lower or equal to 0!", block));
                    continue;
                }

                blocks.put(material, chance);
            }
        }

        // Recipe
        if (!config.contains("recipe")) return true;

        ConfigurationSection recipeConfig = config.getConfigurationSection("recipe");
        if (recipeConfig == null) return true;

        recipe = new GeneratorRecipe(this);
        if (!recipe.load(recipeConfig)) {
            recipe = null;

            logError("Recipe couldn't be loaded!");
            return false;
        }

        return true;
    }

    public boolean isDisabledInLocation(Location loc) {
        String worldName = loc.getWorld().getName();

        if (generators.getDisabledWorlds().contains(worldName)) return true;
        return disabledWorlds.contains(worldName);
    }

    public HashMap<String, Object> getDefaultPlaceholders() {
        return new HashMap<>(placeholders);
    }

    public HashMap<String, Object> getPlaceholders(double remainingDurability) {
        HashMap<String, Object> phs = new HashMap<>(placeholders);
        phs.put("remaining-durability", df.format(remainingDurability));

        return phs;
    }

    private void logError(String message) {
        YLogger.warn(String.format("[Generator-%s] %s", name, message));
    }

    @NotNull
    public Generators getGenerators() {
        return generators;
    }

    public String getName() {
        return name;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public GeneratorItem getItem() {
        return item;
    }

    public double getDurability() {
        return durability;
    }

    public boolean canBeBroken() {
        return canBeBroken;
    }

    public int getCooldown() {
        return cooldown;
    }

    @NotNull
    public Material getDefaultBlock() {
        return defaultBlock;
    }

    public boolean getCraftingRepair() {
        return craftingRepair;
    }

    public int getMaxInChunk() {
        return maxInChunk;
    }

    @NotNull
    public List<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    public boolean getDoubledropUseMultiplayer() {
        return doubledropUseMultiplayer;
    }

    public double getDoubledropDurabilityDecrease() {
        return doubledropDurabilityDecrease;
    }

    @NotNull
    public HashMap<Material, Double> getBlocks() {
        return blocks;
    }

    @Nullable
    public GeneratorRecipe getRecipe() {
        return recipe;
    }
}
