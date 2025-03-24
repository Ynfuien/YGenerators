package pl.ynfuien.ygenerators.hooks.placeholderapi.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerators;
import pl.ynfuien.ygenerators.hooks.placeholderapi.Placeholder;

public class GeneratorsPlaceholders implements Placeholder {
    private final YGenerators instance;
    private final PlacedGenerators placedGenerators;

    public GeneratorsPlaceholders(YGenerators instance) {
        this.instance = instance;
        this.placedGenerators = instance.getPlacedGenerators();
    }

    @Override
    public String name() {
        return "generators";
    }

    @Override
    public String getPlaceholder(String id, OfflinePlayer p) {
        // Placeholder: %ygenerators_generators_all%
        // Returns: count of all generators placed on the server
        if (id.equals("all")) return String.valueOf(placedGenerators.getAll().size());

        // Placeholders:
        // - %ygenerators_generators_inworld%
        // - %ygenerators_generators_inworld_<world>%
        if (id.startsWith("inworld")) {
            // Placeholder: %ygenerators_generators_inworld%
            // Returns: count of all generators placed in current player's world
            if (id.equals("inworld")) {
                // Return if player isn't online
                if (!p.isOnline()) return "player is offline";

                World world = p.getPlayer().getWorld();
                long count = placedGenerators.getAllLocations().stream().filter(loc -> loc.getWorld().equals(world)).count();
                return String.valueOf(count);
            }

            // Return if provided property isn't correct
            if (!id.startsWith("inworld_")) return null;

            // Placeholder: %ygenerators_generators_inworld_<world>%
            // Returns: count of all generators placed in provided world

            // Get world name
            String worldName = id.substring(8);
            // Get world
            World world = Bukkit.getWorld(worldName);

            // Return if provided world doesn't exist
            if (world == null) return "world doesn't exist";

            long count = placedGenerators.getAllLocations().stream().filter(loc -> loc.getWorld().equals(world)).count();
            return String.valueOf(count);
        }

        return null;
    }
}
