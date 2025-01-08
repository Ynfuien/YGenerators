package pl.ynfuien.ygenerators.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class Items {
    // Gives player items. Returns:
    // true - when all items were given to inventory
    // false - when some items were dropped on ground
    public static boolean giveItems(Player p, ItemStack item) {
        return giveItems(p, new ItemStack[] {item});
    }

    public static boolean giveItems(Player p, ItemStack[] items) {
        // Items that couldn't be added to player's inventory
        HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(items);

        if (remainingItems.size() == 0) {
            return true;
        }

        // Get player's world
        World world = p.getWorld();
        // Get player's eye location
        Location loc = p.getEyeLocation();
        // Loop through remaining items
        for (ItemStack remainingItem : remainingItems.values()) {
            world.dropItem(loc, remainingItem);
        }

        return false;
    }
}
