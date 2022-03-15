package pl.ynfuien.ygenerators.utils;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.data.Doubledrop;
import pl.ynfuien.ygenerators.generators.Database;

import java.text.DecimalFormat;
import java.util.HashMap;

public class Util {
    private static boolean reloading = false;

    // Get true if papi is enabled
    public static boolean isPapiEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    // Get true if papi is enabled
    public static boolean isSS2Enabled() {
//        return Bukkit.getPluginManager().isPluginEnabled("SuperiorSkyblock2");
        return Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2") != null;
    }

    // Gets whether server support RGB
    public static boolean isRGBSupported() {
        // Versions that support rgb
        String[] versions = {"1.16", "1.17", "1.18", "1.19"};
        // Server version
        String serverVersion = Bukkit.getMinecraftVersion();
        // Loop through versions and check if server version is any of versions in array
        for (String version : versions) {
            if (serverVersion.startsWith(version)) {
                return true;
            }
        }

        return false;
    }

    // Reload plugin's configurations
    public static boolean reloadPlugin() {
        reloading = true;

        // Get plugin instance
        YGenerators instance = YGenerators.getInstance();

        // Get double drop
        Doubledrop doubledrop = instance.getGenerators().getDoubledrop();
        // Cancel interval
        doubledrop.cancelInterval();

        // Get generators database
        Database database = instance.getDatabase();
        // Stop database update interval
        database.stopUpdateInterval();
        // Save generators
        database.saveToFile();

        // Reload all configs
        instance.getConfigManager().reloadConfigs();

        // Reload generators
        instance.reloadGenerators();
        // Reload lang
        instance.reloadLang();

        // Load generators database from file
        database.loadFromFile();
        // Start database update interval
        database.startUpdateInterval(instance.getConfig().getInt("database-update-interval"));

        reloading = false;
        return true;
    }
    // Get true if plugin is currently reloading
    public static boolean isReloading() {
        return reloading;
    }

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


    // Formats double to string
    public static String formatDouble(double number) {
        return formatDouble(number, -1);
    }

    // Formats double to string
    public static String formatDouble(double number, int decimalPlaces) {
        return formatDouble(number, decimalPlaces, -1);
    }

    // Formats double to string
    public static String formatDouble(double number, int decimalPlaces, int maxDecimalPlaces) {
        // If number has decimal places
        if ((int) number != number) {
            // If decimal places amount is <= -1
            if (decimalPlaces < 0 && maxDecimalPlaces < 0) {
                return String.valueOf(number);
            }

            DecimalFormat df;
            if (maxDecimalPlaces > -1) {
                df = new DecimalFormat("#." + "#".repeat(maxDecimalPlaces));
            } else {
                df = new DecimalFormat("0." + "0".repeat(decimalPlaces));
            }

            return df.format(number);
        }

        // Return number without decimal places
        return  String.valueOf((int) number);
    }

    // Replaces plugin placeholders in provided text
    public static String replacePlaceholders(String text, HashMap<String, Object> placeholders) {
        // Return null if provided text is null
        if (text == null) return null;
        // Return unchanged text if placeholders hashmap is null
        if (placeholders == null) return text;

        // Loop through placeholders
        for (String placeholder : placeholders.keySet()) {
            // Get placeholder's value
            String value = String.valueOf(placeholders.get(placeholder));
            // Add '{}' to placeholder
            String ph = new StringBuilder(placeholder.length() + 2).append("{").append(placeholder).append("}").toString();
            // Replace placeholder occurrences in text
            text = text.replace(ph, value);
        }

        // Return result
        return text;
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
            Logger.logWarning("Error 1:");
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
