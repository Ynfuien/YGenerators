package pl.ynfuien.ygenerators.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerators;

public class BlockPistonRetractListener implements Listener {
    // This listener handles moving by piston:
    // - generators
    // - generator's generated blocks

    private final PlacedGenerators placedGenerators;
    public BlockPistonRetractListener(YGenerators instance) {
        placedGenerators = instance.getPlacedGenerators();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        // Loop through moved blocks
        for (Block b : e.getBlocks()) {
            // Get block's location
            Location loc = b.getLocation();

            // If in location is generator
            if (placedGenerators.has(loc)) {
                // Cancel event and return
                e.setCancelled(true);
                return;
            }

            // Get location under block
            Location locUnder = b.getRelative(BlockFace.DOWN).getLocation();
            // If location under block is generator
            if (placedGenerators.has(locUnder)) {
                // Cancel event and return
                e.setCancelled(true);
                return;
            }
        }
    }
}
