package pl.ynfuien.ygenerators.utils;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import pl.ynfuien.ydevlib.messages.YLogger;

public class Util {

    // Chance of provided chance to return true
    public static boolean chanceOf(double chance) {
        return Math.random() < (chance / 100);
    }

    // Gets random number between two double values
    public static double getRandomNumber(double min, double max) {
        return (Math.random() * (max - min)) + min;
    }


    // Gets string with uppercase first letter
    public static String uppercaseFirstLetter(String text) {
        if (text.length() < 1) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    // Breaks naturally
    public static void breakNaturally(Block b) {
        // Return if block is air
        if (b.isEmpty()) return;

        // Get material
        Material type = b.getType();

        // Set block to air
        b.setType(Material.AIR);

        // Return if block isn't placeable block
        if (!type.isBlock()) return;

        // Get location
        Location loc = b.getLocation();

        // Get world
        World world = loc.getWorld();

        // Spawn particle
        world.spawnParticle(Particle.BLOCK_CRACK, loc, 1, type.createBlockData());
        // Play effect
        try {
            world.playEffect(loc, Effect.STEP_SOUND, type);
        } catch (Exception e) {
            YLogger.warn("Error 1:");
            e.printStackTrace();
        }
    }

    public static ItemStack reduceItemDurability(ItemStack i) {
        // Get damageable item meta
        Damageable im = (Damageable) i.getItemMeta();

        // Get chance to increase damage of item
        double chance = 100d / (im.getEnchantLevel(Enchantment.DURABILITY) + 1d);
        // Return if chanceOf is false
        if (!chanceOf(chance)) return i;

        // Increase damage of item
        im.setDamage(im.getDamage() + 1);

        // Set item meta to item
        i.setItemMeta(im);

        // Return item
        return i;
    }
}
