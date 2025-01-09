package pl.ynfuien.ygenerators.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class NBTTags {
    private static Plugin instance;

    public static void setInstance(Plugin instance) {
        NBTTags.instance = instance;
    }


    // Set provided value for provided nbt tag in item stack and return it.
    // Original item stack object also will be changed
    public static ItemStack set(ItemStack itemStack, PersistentDataType type, String key, Object value) {
        // Get item meta from item stack
        ItemMeta im = itemStack.getItemMeta();
        // Set item meta to changed item meta
        itemStack.setItemMeta(set(im, type, key, value));

        // Return item stack
        return itemStack;
    }

    // Set provided value for provided nbt tag in item meta and return it.
    // Original item meta object also will be changed
    public static ItemMeta set(ItemMeta itemMeta, PersistentDataType type, String key, Object value) {
        // Create namespaced key from plugin instance and provided key
        NamespacedKey namespacedKey = new NamespacedKey(instance, key);
        // Set value for provided key with provided type
        itemMeta.getPersistentDataContainer().set(namespacedKey, type, value);
        // Return changed item meta
        return itemMeta;
    }


    // Get nbt tag value from item stack by key and type
    public static Object get(ItemStack itemStack, PersistentDataType type, String key) {
        return get(itemStack.getItemMeta(), type, key);
    }

    // Get nbt tag value from item meta by key and type
    public static Object get(ItemMeta itemMeta, PersistentDataType type, String key) {
        // Create namespaced key from plugin instance and provided key
        NamespacedKey namespacedKey = new NamespacedKey(instance, key);
        // Get persistent data container from item meta
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        try {
            // Get value by namespaced key and return it
            return container.get(namespacedKey, type);
        } catch (NullPointerException|IllegalArgumentException e) {
            // Return null if value doesn't exist or if type is incorrect
            return null;
        }
    }

    // Return true if item stack has provided NBT tag
    public static boolean has(ItemStack itemStack, PersistentDataType type, String key) {
        return has(itemStack.getItemMeta(), type, key);
    }

    // Return true if item meta has provided NBT tag
    public static boolean has(ItemMeta itemMeta, PersistentDataType type, String key) {
        // Create namespaced key from plugin instance and provided key
        NamespacedKey namespacedKey = new NamespacedKey(instance, key);
        // Get persistent data container from item meta
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        // Return whether container has namespaced key with provided type or not
        return container.has(namespacedKey, type);
    }
}
