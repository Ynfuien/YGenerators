package pl.ynfuien.ygenerators.hooks.placeholderapi.placeholders;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import pl.ynfuien.ydevlib.utils.DoubleFormatter;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.core.generator.GeneratorItem;
import pl.ynfuien.ygenerators.hooks.placeholderapi.Placeholder;

import java.util.HashMap;

public class GeneratorPlaceholders implements Placeholder {
    private final Generators generators;
    private final Doubledrop doubledrop;

    private final static DoubleFormatter df = DoubleFormatter.DEFAULT;

    public GeneratorPlaceholders(YGenerators instance) {
        this.generators = instance.getGenerators();
        this.doubledrop = instance.getDoubledrop();
    }

    @Override
    public String name() {
        return "generator";
    }

    @Override
    public String getPlaceholder(String id, OfflinePlayer p) {
        Generator gene = null;
        // Loop through all generators to get that with provided name
        for (Generator generator : generators.getAll().values()) {
            String name = generator.getName();

            // If id starts with provided name
            if (id.startsWith(name + "_")) {
                gene = generator;
                break;
            }
        }

        // Return if generator with provided name doesn't exist
        if (gene == null) return "generator doesn't exist";

        // Set id to provided properties after generator name
        id = id.substring(gene.getName().length() + 1).toLowerCase();

        // Placeholder: %ygenerators_generator_<name>_default-block%
        // Returns: generator's default block
        if (id.equals("default-block")) return gene.getDefaultBlock().name();

        // Placeholder: %ygenerators_generator_<name>_durability%
        // Returns: generator's durability
        if (id.equals("durability")) return df.format(gene.getDurability());

        // Placeholder: %ygenerators_generator_<name>_cooldown%
        // Returns: generator's cooldown in ticks
        if (id.equals("cooldown")) return String.valueOf(gene.getCooldown());

        // Placeholders:
        // - %ygenerators_generator_<name>_item_material%
        // - %ygenerators_generator_<name>_item_display-name%
        // - %ygenerators_generator_<name>_item_enchanted%
        // - %ygenerators_generator_<name>_item_lore%
        if (id.startsWith("item_")) {
            // Set id to property provided after "item_"
            id = id.substring(5);

            // Get generator item
            GeneratorItem item = gene.getItem();

            // Placeholder: %ygenerators_generator_<name>_item_material%
            // Returns: generator item's material
            if (id.equals("material")) return item.getMaterial().name();

            // Placeholder: %ygenerators_generator_<name>_item_display-name%
            // Returns: generator item's display name
            if (id.equals("display-name")) return item.getDisplayName();

            // Placeholder: %ygenerators_generator_<name>_item_enchanted%
            // Returns: true / false
            if (id.equals("enchanted")) return String.valueOf(item.isEnchanted());

            // Placeholder: %ygenerators_generator_<name>_item_lore%
            // Returns: generator item's lore
            if (id.equals("lore")) return String.join("\n", item.getLore());

            return null;
        }

        // Placeholders:
        // - %ygenerators_generator_<name>_blocks_<block>_normal%
        // - %ygenerators_generator_<name>_blocks_<block>_doubledrop%
        // - %ygenerators_generator_<name>_blocks_<block>_current%
        if (id.startsWith("blocks_")) {
            // Set id to property after "blocks_"
            id = id.substring(7);

            // Get blocks hashmap
            HashMap<Material, Double> blocks = gene.getBlocks();
            // Loop through blocks
            for (Material b : blocks.keySet()) {
                // Get block name in lower case
                String bName = b.name().toLowerCase();

                // Skip block if id doesn't start with its name
                if (!id.startsWith(bName + "_")) continue;

                // Set id to property after "<block>_"
                id = id.substring(bName.length() + 1);
                // Get block's chance
                double chance = blocks.get(b);

                // Placeholder: %ygenerators_generator_<name>_blocks_<block>_normal%
                // Returns: normal chance to generate block
                if (id.equals("normal")) return df.format(chance);

                // Get double drop multiplayer
                double multiplayer = doubledrop.getMultiplayer();

                // Placeholder: %ygenerators_generator_<name>_blocks_<block>_doubledrop%
                // Returns: chance to generate block with double drop
                if (id.equals("doubledrop")) return df.format(chance * multiplayer);

                // Placeholder: %ygenerators_generator_<name>_blocks_<block>_current%
                // Returns: current chance to generate block
                if (id.equals("current")) {
                    if (doubledrop.isActive() && gene.getDoubledropUseMultiplayer()) return df.format(chance * multiplayer);
                    return df.format(chance);
                }

                return null;
            }

            return "generator doesn't have this block";
        }

        return null;
    }
}
