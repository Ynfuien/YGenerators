package pl.ynfuien.ygenerators.hooks.placeholderapi.placeholders;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import pl.ynfuien.ygenerators.data.Doubledrop;
import pl.ynfuien.ygenerators.data.Generators;
import pl.ynfuien.ygenerators.data.generator.Generator;
import pl.ynfuien.ygenerators.data.generator.GeneratorItem;
import pl.ynfuien.ygenerators.hooks.placeholderapi.Placeholder;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.HashMap;

public class GeneratorPlaceholders implements Placeholder {
    private final Generators generators;
    public GeneratorPlaceholders(Generators generators) {
        this.generators = generators;
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
        if (gene == null) {
            return "generator doesn't exist";
        }

        // Set id to provided properties after generator name
        id = id.substring(gene.getName().length() + 1).toLowerCase();

        // Placeholder: %ygenerators_generator_<name>_default-block%
        // Returns: generator's default block
        if (id.equals("default-block")) {
            return gene.getDefaultBlock().name();
        }

        // Placeholder: %ygenerators_generator_<name>_durability%
        // Returns: generator's durability
        if (id.equals("durability")) {
            return Util.formatDouble(gene.getDurability());
        }

        // Placeholder: %ygenerators_generator_<name>_cooldown%
        // Returns: generator's cooldown in ticks
        if (id.equals("cooldown")) {
            return String.valueOf(gene.getCooldown());
        }

        // Placeholders:
        // - %ygenerators_generator_<name>_item_material%
        // - %ygenerators_generator_<name>_item_displayname%
        // - %ygenerators_generator_<name>_item_enchanted%
        // - %ygenerators_generator_<name>_item_lore%
        if (id.startsWith("item_")) {
            // Set id to property provided after "item_"
            id = id.substring(5);

            // Get generator item
            GeneratorItem item = gene.getItem();

            // Placeholder: %ygenerators_generator_<name>_item_material%
            // Returns: generator item's material
            if (id.equals("material")) {
                return item.material().name();
            }

            // Placeholder: %ygenerators_generator_<name>_item_displayname%
            // Returns: generator item's displayname
            if (id.equals("displayname")) {
                return item.getDisplayname(p.getPlayer());
            }

            // Placeholder: %ygenerators_generator_<name>_item_enchanted%
            // Returns: true / false
            if (id.equals("enchanted")) {
                return String.valueOf(item.enchanted());
            }

            // Placeholder: %ygenerators_generator_<name>_item_lore%
            // Returns: generator item's lore
            if (id.equals("lore")) {
                return String.join("\n", item.getLore(p.getPlayer()));
            }

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
                if (!id.startsWith(bName + "_")) {
                    continue;
                }

                // Set id to property after "<block>_"
                id = id.substring(bName.length() + 1);
                // Get block's chance
                double chance = blocks.get(b);

                // Placeholder: %ygenerators_generator_<name>_blocks_<block>_normal%
                // Returns: normal chance to generate block
                if (id.equals("normal")) {
                    return Util.formatDouble(chance, 2);
                }

                // Get doubledrop
                Doubledrop doubledrop = generators.getDoubledrop();
                // Get double drop multiplayer
                double multiplayer = doubledrop.getMultiplayer();

                // Placeholder: %ygenerators_generator_<name>_blocks_<block>_doubledrop%
                // Returns: chance to generate block with double drop
                if (id.equals("doubledrop")) {
                    return Util.formatDouble(chance * multiplayer, 2);
                }

                // Placeholder: %ygenerators_generator_<name>_blocks_<block>_current%
                // Returns: current chance to generate block
                if (id.equals("current")) {
                    if (doubledrop.isActive()) return Util.formatDouble(chance * multiplayer, 2);
                    return Util.formatDouble(chance);
                }

                return null;
            }

            return "generator doesn't have this block";
        }

        return null;
    }
}
