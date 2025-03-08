package pl.ynfuien.ygenerators.core.generator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import pl.ynfuien.ydevlib.messages.Messenger;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ydevlib.messages.colors.ColorFormatter;
import pl.ynfuien.ydevlib.utils.DoubleFormatter;
import pl.ynfuien.ygenerators.YGenerators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GeneratorItem {
    private final Generator generator;
    private Material material;

    private String displayName = null;
    private boolean enchanted = false;
    private List<String> lore = new ArrayList<>();

    private boolean stackable = true;
    private boolean canBeUsedInCrafting = false;

    private final HashMap<String, Object> placeholders;

    private final static DoubleFormatter df = DoubleFormatter.DEFAULT;

    public GeneratorItem(Generator generator) {
        this.generator = generator;
        this.placeholders = generator.getDefaultPlaceholders();
    }

    public boolean load(ConfigurationSection config) {
        if (!config.contains("material")) {
            logError("Missing key 'material'");
            return false;
        }

        material = Material.matchMaterial(config.getString("material"));
        if (material == null) {
            logError("Provided material is incorrect!");
            return false;
        }

        if (!material.isBlock() || material.isAir()) {
            logError("Provided material isn't block!");
            return false;
        }

        if (config.contains("display-name")) displayName = config.getString("display-name");
        if (config.contains("enchanted")) enchanted = config.getBoolean("enchanted");
        if (config.contains("lore")) lore = config.getStringList("lore");
        if (config.contains("stackable")) stackable = config.getBoolean("stackable");
        if (config.contains("can-be-used-in-crafting")) canBeUsedInCrafting = config.getBoolean("can-be-used-in-crafting");

        return true;
    }

    protected void logError(String message) {
        YLogger.warn(String.format("[GeneratorItem-%s] %s", generator.getName(), message));
    }

    public Generator getGenerator() {
        return generator;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEnchanted() {
        return enchanted;
    }

    public List<String> getLore() {
        return lore;
    }

    public boolean isStackable() {
        return stackable;
    }

    public boolean canBeUsedInCrafting() {
        return canBeUsedInCrafting;
    }

    public ItemStack getItemStack() {
        return getItemStack(null);
    }

    public ItemStack getItemStack(Player player) {
        return getItemStack(player, generator.getDurability());
    }

    public ItemStack getItemStack(Player player, double durability) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        placeholders.put("remaining-durability", df.format(durability));
        if (displayName != null) {
            Component parsed = Messenger.parseMessage(player, displayName, placeholders);
            meta.displayName(parsed.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        // Set item lore
        List<Component> itemLore = new ArrayList<>();
        for (String line : lore) {
            Component parsed = Messenger.parseMessage(player, line, placeholders);
            itemLore.add(ColorFormatter.negateUnsetDecoration(parsed, TextDecoration.ITALIC));
        }
        meta.lore(itemLore);

        // Set glow effect
        if (enchanted) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.ARROW_INFINITE, 0, true);
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(NSKey.GENERATOR, PersistentDataType.STRING, generator.getName());
        pdc.set(NSKey.DURABILITY, PersistentDataType.DOUBLE, durability);

        if (!stackable) pdc.set(NSKey.RANDOM_ID, PersistentDataType.STRING, UUID.randomUUID().toString());

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack[] getItemStacks(Player player, int amount) {
        return getItemStacks(player, amount, generator.getDurability());
    }

    public ItemStack[] getItemStacks(Player player, int amount, double durability) {
        ItemStack item = getItemStack(player, durability);
        ItemStack[] items = new ItemStack[amount];

        // If generator item is stackable
        if (stackable) {
            // Clone item stack to array
            for (int i = 0; i < items.length; i++) {
                items[i] = item.clone();
            }

            return items;
        }


        // Generate uuid for every generator
        for (int i = 0; i < amount; i++) {
            ItemStack clone = item.clone();

            ItemMeta meta = clone.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(NSKey.RANDOM_ID, PersistentDataType.STRING, UUID.randomUUID().toString());
            clone.setItemMeta(meta);

            items[i] = clone;
        }

        return items;
    }

    public List<Component> getParsedLore(Player player) {
        List<Component> result = new ArrayList<>();

        for (String line : lore) {
            result.add(Messenger.parseMessage(player, line, placeholders));
        }

        return result;
    }

    public static class NSKey {
        public static NamespacedKey GENERATOR;
        public static NamespacedKey DURABILITY;
        public static NamespacedKey RANDOM_ID;

        public static void setup(YGenerators instance) {
            GENERATOR = new NamespacedKey(instance, "generator");
            DURABILITY = new NamespacedKey(instance, "durability");
            RANDOM_ID = new NamespacedKey(instance, "random-id");
        }
    }

}
