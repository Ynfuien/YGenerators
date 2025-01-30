package pl.ynfuien.ygenerators.core.generator;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ydevlib.messages.Messenger;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ydevlib.messages.colors.ColorFormatter;
import pl.ynfuien.ydevlib.utils.DoubleFormatter;
import pl.ynfuien.ygenerators.utils.NBTTags;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GeneratorItem {
    private final Generator generator;
    private Material material;

    private String displayname = null;
    private boolean enchanted = false;
    private List<String> lore = new ArrayList<>();

    private boolean stackable = true;
    private boolean canBeUsedInCrafting = false;

    private final static DoubleFormatter df = DoubleFormatter.DEFAULT;

    public GeneratorItem(Generator generator) {
        this.generator = generator;
    }

    public boolean loadFromConfigSection(ConfigurationSection config) {
        if (!config.contains("material")) {
            logError("Missing key 'material'");
            return false;
        }

        // Material
        material = Material.matchMaterial(config.getString("material"));
        // Return if it is incorrect
        if (material == null) {
            logError("Provided material is incorrect!");
            return false;
        }
        // Return if provided material isn't placeable block
        if (!material.isBlock()) {
            logError("Provided material isn't block!");
            return false;
        }

        // Displayname
        if (config.contains("displayname")) {
            displayname = config.getString("displayname");
        }

        // Enchanted
        if (config.contains("enchanted")) {
            enchanted = config.getBoolean("enchanted");
        }

        // Lore
        if (config.contains("lore")) {
            lore = config.getStringList("lore");
        }

        // Stackable
        if (config.contains("stackable")) {
            stackable = config.getBoolean("stackable");
        }

        // Stackable
        if (config.contains("can-be-used-in-crafting")) {
            canBeUsedInCrafting = config.getBoolean("can-be-used-in-crafting");
        }

        return true;
    }

    // Logs error with provided message
    protected void logError(String message) {
        YLogger.warn(String.format("[GeneratorItem-%s] %s", generator.getName(), message));
    }

    // Gets generator
    @NotNull
    public Generator generator() {
        return generator;
    }

    // Gets item material
    @NotNull
    public Material material() {
        return material;
    }

    // Gets item displayname
    @Nullable
    public String displayname() {
        return displayname;
    }

    // Gets whether item should be enchanted
    public boolean enchanted() {
        return enchanted;
    }

    // Gets item lore
    @Nullable
    public List<String> lore() {
        return lore;
    }

    // Gets whether item should be stackable
    public boolean stackable() {
        return stackable;
    }

    // Gets whether item can be used to craft vanilla items
    public boolean getCanBeUsedInCrafting() {
        return canBeUsedInCrafting;
    }

    // Gets generator item's item stack with full durability
    @NotNull
    public ItemStack getItemStack() {
        return getItemStack(null);
    }

    // Gets generator item's item stack with provided durability
    @NotNull
    public ItemStack getItemStack(double durability) {
        return getItemStack(null, durability);
    }

    // Gets generator item's item stack with full durability
    @NotNull
    public ItemStack getItemStack(Player p) {
        return getItemStack(p, generator.getDurability());
    }

    // Gets generator item's item stack with provided durability
    @NotNull
    public ItemStack getItemStack(Player p, double durability) {
        // Create generator item's item stack
        ItemStack item = new ItemStack(material);

        // Get item meta
        ItemMeta im = item.getItemMeta();

        // Set item displayname if it is provided
        if (displayname != null) {
            im.setDisplayName(formatText(p, displayname, durability));
        }

        // Set item lore
        List<String> itemLore = new ArrayList<>();
        for (String line : lore) {
            itemLore.add(formatText(p, line, durability));
        }
        im.setLore(itemLore);

        // Set item meta to item stack
        item.setItemMeta(im);

        // Set item enchant effect
        if (enchanted) {
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(im);
            item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 0);
        }

        // Set nbt tag for identify generator item
        NBTTags.set(item, PersistentDataType.STRING, "generator", generator.getName());
        // Set nbt tag to define remaining durability
        NBTTags.set(item, PersistentDataType.DOUBLE, "durability", durability);

        if (!stackable) {
            NBTTags.set(item, PersistentDataType.STRING, "id", UUID.randomUUID().toString());
        }

        // Return item
        return item;
    }

    // Gets provided amount of generator item's item stacks
    @NotNull
    public ItemStack[] getItemStacks(Player p, int amount) {
        return getItemStacks(p, amount, generator().getDurability());
    }

    // Gets provided amount of generator item's item stacks with provided durability
    @NotNull
    public ItemStack[] getItemStacks(Player p, int amount, double durability) {
        // Get generator item's item stack
        ItemStack item = getItemStack(p, durability);
        // Create array for item stacks
        ItemStack[] items = new ItemStack[amount];

        // If generator item is stackable
        if (stackable) {
            // Clone item stack to array
            for (int i = 0; i < items.length; i++) {
                items[i] = item.clone();
            }

            // Return items
            return items;
        }


        // Generate uuid for every generator
        for (int i = 0; i < amount; i++) {
            items[i] = NBTTags.set(item.clone(), PersistentDataType.STRING, "id", UUID.randomUUID().toString());
        }

        // Return items
        return items;
    }

    // Format text with generator placeholders, colors and papi placeholders
    private String formatText(Player p, String text) {
        return formatText(p, text, generator.getDurability());
    }

    // Format text with generator placeholders, colors and papi placeholders
    private String formatText(Player p, String text, double durability) {
        return ColorFormatter.LEGACY_SERIALIZER.serialize(Messenger.parseMessage(p, setPlaceholders(text, durability), null));
//        return Messages.format(p, setPlaceholders(text, durability));
    }

    // Set generator placeholders in text
    private String setPlaceholders(String text, double durability) {
        return text
                .replace("{name}", generator.getName())
                .replace("{displayname}", generator.getDisplayName())
                .replace("{cooldown}", String.valueOf(generator.getCooldown()))
                .replace("{remaining-durability}", df.format(durability))
                .replace("{full-durability}", df.format(generator.getDurability()));
    }

    // Gets item's displayname with set generator placeholders
    public String getDisplayname() {
        return getDisplayname(null);
    }

    // Gets item's displayname with set generator placeholders and papi for player
    public String getDisplayname(Player p) {
        return formatText(p, displayname);
    }

    // Gets item's displayname with set generator placeholders
    public List<String> getLore() {
        return getLore(null);
    }

    // Gets item's displayname with set generator placeholders and papi for player
    public List<String> getLore(Player p) {
        List<String> result = new ArrayList<>();

        for (String line : lore) {
            result.add(formatText(p, line));
        }

        return result;
    }
}
