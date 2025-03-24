package pl.ynfuien.ygenerators.hooks.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.hooks.placeholderapi.placeholders.DoubledropPlaceholders;
import pl.ynfuien.ygenerators.hooks.placeholderapi.placeholders.GeneratorPlaceholders;
import pl.ynfuien.ygenerators.hooks.placeholderapi.placeholders.GeneratorsPlaceholders;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final YGenerators instance;

    private final Placeholder[] placeholders;

    public PlaceholderAPIHook(YGenerators instance) {
        this.instance = instance;

        placeholders = new Placeholder[] {
            new DoubledropPlaceholders(instance),
            new GeneratorPlaceholders(instance),
            new GeneratorsPlaceholders(instance)
        };
    }

    @Override @NotNull
    public String getAuthor() {
        return "Ynfuien";
    }

    @Override @NotNull
    public String getIdentifier() {
        return "ygenerators";
    }

    @Override @NotNull
    public String getVersion() {
        return instance.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

//    Generator
    // %ygenerators_generator_<generator>_blocks_<block>_normal%
    // %ygenerators_generator_<generator>_blocks_<block>_doubledrop%
    // %ygenerators_generator_<generator>_blocks_<block>_current%

    // %ygenerators_generator_<generator>_default-block%
    // %ygenerators_generator_<generator>_durability%
    // %ygenerators_generator_<generator>_cooldown%

    // %ygenerators_generator_<generator>_item_material%
    // %ygenerators_generator_<generator>_item_displayname%
    // %ygenerators_generator_<generator>_item_enchanted%
    // %ygenerators_generator_<generator>_item_lore%

//    Generators
    // %ygenerators_generators_all%
    // %ygenerators_generators_inworld%
    // %ygenerators_generators_inworld_<world>%

//    Double drop
    // %ygenerators_doubledrop_active%
    // %ygenerators_doubledrop_active_raw%
    // %ygenerators_doubledrop_multiplayer%
    // %ygenerators_doubledrop_time_left%
    // %ygenerators_doubledrop_status%
    // %ygenerators_doubledrop_status_multiplayer%
    // %ygenerators_doubledrop_status_multiplayer_always%
    @Override
    public String onRequest(OfflinePlayer p, @NotNull String params) {
        Placeholder placeholder = null;

        // Loop through placeholders and get that provided by name
        for (Placeholder ph : placeholders) {
            if (params.startsWith(ph.name() + "_") || params.equalsIgnoreCase(ph.name())) {
                placeholder = ph;
                break;
            }
        }

        // If provided placeholder is incorrect
        if (placeholder == null) return "incorrect placeholder";
        String phName = placeholder.name();

        // Get placeholder properties from params
        String id = !params.equalsIgnoreCase(phName) ? params.substring(phName.length() + 1) : "";
        // Get placeholder result
        String result = placeholder.getPlaceholder(id, p);

        // If result is null
        if (result == null) return "incorrect property";

        // Return result
        return result;
    }
}