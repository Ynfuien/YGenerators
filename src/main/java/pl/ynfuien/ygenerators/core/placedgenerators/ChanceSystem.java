package pl.ynfuien.ygenerators.core.placedgenerators;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ChanceSystem {
    // Choices which block generate
    @Nullable
    public static Material getBlockToGenerate(@NotNull HashMap<Material, Double> blocks, double multiplayer) {
        if (blocks.size() == 0) return null;

        // Sum all block chances
        double allBlockChances = blocks.values().stream().mapToDouble(Double::doubleValue).sum();

//        YLogger.warn("allBlockChances: " + allBlockChances + ", with multi: " + (allBlockChances * multiplayer));

        // Return no block to generate if chance of is false
        if (!chanceOf(allBlockChances * multiplayer)) return null;

        // Get random number between 0 and summed block chances
        double random = getRandomNumber(0, allBlockChances);

//        YLogger.warn("random: " + random);

        double currentChance = 0;
        // Loop through blocks
        for (Material block : blocks.keySet()) {
            // Get block chance
            double chance = blocks.get(block);

            // Add chance to current chance
            currentChance += chance;

            // Return current block if current chance is lower than random number
            if (random < currentChance) {
                return block;
            }
        }

        // Return no block to generate
        return null;
    }

    // Chance of provided chance to return true
    public static boolean chanceOf(double chance) {
        return Math.random() < (chance / 100);
    }

    // Gets random number between two double values
    public static double getRandomNumber(double min, double max) {
        return (Math.random() * (max - min)) + min;
    }
}
