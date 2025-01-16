package pl.ynfuien.ygenerators.core;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ydevlib.messages.YLogger;

import java.util.HashMap;

public class VanillaGenerators {
    private final boolean enabled;
    private boolean useDoubledrop = true;
    private Material defaultBlock;
    private final HashMap<Material, Double> blocks = new HashMap<>();

    public VanillaGenerators(ConfigurationSection config) {
        // Enabled
        enabled = config.getBoolean("enabled");

        // Return if feature isn't enabled
        if (!enabled) return;


        logInfo("Loading settings..");

        // Use double drop
        useDoubledrop = config.getBoolean("use-doubledrop");

        // Default block
        String providedBlock = config.getString("default-block");
        defaultBlock = Material.matchMaterial(providedBlock);

        // Log error if provided block is incorrect and option 'default' wasn't used
        if (defaultBlock == null && !providedBlock.equalsIgnoreCase("default")) {
            logError("Provided default-block is incorrect! Will be used 'default'.");
        }

        // Get blocks config section
        ConfigurationSection blocksConfigSection = config.getConfigurationSection("blocks");
        // Return and log error if config section is null
        if (blocksConfigSection == null) {
            logError("No blocks are provided!");
            return;
        }

        // Loop through blocks
        for (String block : blocksConfigSection.getKeys(false)) {
            // Get chance
            double chance = blocksConfigSection.getDouble(block);

            // Get material from provided block
            Material material = Material.matchMaterial(block);

            if (material == null) {
                logError(String.format("Provided block '%s' in 'blocks' is incorrect and won't be used!", block));
                continue;
            }

            // Put block in blocks
            blocks.put(material, chance);
        }

        logInfo("Successfully loaded settings!");
    }

    private void logInfo(String message) {
        YLogger.info("[Vanilla-Generators] " + message);
    }

    private void logError(String message) {
        YLogger.warn("[Vanilla-Generators] " + message);
    }

    // Gets whether feature is enabled
    public boolean isEnabled() {
        return enabled;
    }

    // Gets whether use double drop
    public boolean getUseDoubledrop() {
        return useDoubledrop;
    }

    // Gets default block
    @Nullable
    public Material getDefaultBlock() {
        return defaultBlock;
    }

    // Gets blocks
    @NotNull
    public HashMap<Material, Double> getBlocks() {
        return blocks;
    }
}
