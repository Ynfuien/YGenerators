package pl.ynfuien.ygenerators.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.generators.Database;

public class BlockPistonExtendListener implements Listener {
    // This listener handles pushing by piston:
    // - generators
    // - generator's generated blocks

    private final Database database;
    public BlockPistonExtendListener(YGenerators instance) {
        database = instance.getDatabase();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        // Loop through pushed blocks
        for (Block b : e.getBlocks()) {
            // Get block's location
            Location loc = b.getLocation();

            // If in location is generator
            if (database.has(loc)) {
                // Cancel event and return
                e.setCancelled(true);
                return;
            }

            // Get location under block
            Location locUnder = b.getRelative(BlockFace.DOWN).getLocation();
            // If location under block is generator
            if (database.has(locUnder)) {
                // Cancel event and return
                e.setCancelled(true);
                return;
            }
        }
    }
}
