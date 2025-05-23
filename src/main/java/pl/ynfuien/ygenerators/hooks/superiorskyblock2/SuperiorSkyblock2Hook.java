package pl.ynfuien.ygenerators.hooks.superiorskyblock2;

import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerators;

public class SuperiorSkyblock2Hook implements Listener {
    private static boolean enabled = false;
    private static PlacedGenerators placedGenerators;

    public static void load(YGenerators instance) {
        // Register event on ss2 initialize
        Bukkit.getPluginManager().registerEvents(new SuperiorSkyblock2Hook(), instance);

        placedGenerators = instance.getPlacedGenerators();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    @EventHandler
    public void onPluginInitialize(PluginInitializeEvent e) {
        // Register island privilege for generators pick up
        IslandPrivilege.register("PICK_UP_GENERATORS");
        enabled = true;
    }

    @EventHandler
    public void onIslandDisband(IslandDisbandEvent e) {
        // Get island
        Island island = e.getIsland();

        // Loop through placed generator locations
        for (Location location : placedGenerators.getAllLocations().toArray(Location[]::new)) {
            // Skip if it isn't in island
            if (!island.isInside(location)) continue;

            // Remove generator from database
            placedGenerators.remove(location);
        }
    }
}
