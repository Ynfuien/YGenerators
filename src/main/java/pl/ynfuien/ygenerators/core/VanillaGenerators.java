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
        enabled = config.getBoolean("enabled");
        if (!enabled) return;


        logInfo("Loading settings..");

        useDoubledrop = config.getBoolean("use-doubledrop");

        String providedBlock = config.getString("default-block");
        defaultBlock = Material.matchMaterial(providedBlock);
        if (defaultBlock == null && !providedBlock.equalsIgnoreCase("default")) {
            logError("Provided default-block is incorrect! Will be used 'default'.");
        }

        ConfigurationSection blocksConfigSection = config.getConfigurationSection("blocks");
        if (blocksConfigSection == null) {
            logError("No blocks are provided!");
            return;
        }

        for (String block : blocksConfigSection.getKeys(false)) {
            double chance = blocksConfigSection.getDouble(block);

            Material material = Material.matchMaterial(block);
            if (material == null || !material.isBlock()) {
                logError(String.format("Provided block '%s' in 'blocks' is incorrect and won't be used!", block));
                continue;
            }

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

    public boolean isEnabled() {
        return enabled;
    }

    public boolean getUseDoubledrop() {
        return useDoubledrop;
    }

    @Nullable
    public Material getDefaultBlock() {
        return defaultBlock;
    }

    @NotNull
    public HashMap<Material, Double> getBlocks() {
        return blocks;
    }
}
