package pl.ynfuien.ygenerators.core;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class BlockLottery {
    @Nullable
    public static Material drawABlock(HashMap<Material, Double> blocks, double multiplayer) {
        if (blocks.isEmpty()) return null;

        double summedPercentages = blocks.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.random() >= (summedPercentages * multiplayer / 100)) return null;

        double random = getRandomNumber(0, summedPercentages);

        double currentValue = 0;
        for (Material block : blocks.keySet()) {
            double chance = blocks.get(block);
            currentValue += chance;

            if (random < currentValue) return block;
        }

        return null;
    }

    // Ima need to do it by an example, cause all this chance stuff is giving me a headache,
    // and I'd like to make it right. So, we have these values:
    //   DIAMOND_ORE: 1
    //   EMERALD_ORE: 2
    //   GOLD_ORE: 3
    //   IRON_ORE: 5
    //   STONE: 10
    //
    // Summing all the percentages it will be 21.
    // Then random number between 0 and 100, let's say 18.
    // We loop through the blocks, add their percentage value to the X,
    // and check whether random number is lower than current X.
    // X = 0
    // Diamond: X += 1
    // Emerald: X += 2
    // X == 3
    // Then X += 3 + 5
    // X == 11
    // Still random (18) is higher than X (11).
    // After Stone X is 21, so it's gonna be stone

    public static double getRandomNumber(double min, double max) {
        return (Math.random() * (max - min)) + min;
    }
}
