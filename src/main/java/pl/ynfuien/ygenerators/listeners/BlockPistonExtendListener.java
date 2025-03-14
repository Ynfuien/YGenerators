package pl.ynfuien.ygenerators.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerators;

public class BlockPistonExtendListener implements Listener {
    // This listener handles pushing by piston:
    // - generators
    // - generator's generated blocks

    private final PlacedGenerators placedGenerators;

    public BlockPistonExtendListener(YGenerators instance) {
        placedGenerators = instance.getPlacedGenerators();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            Location location = block.getLocation();

            // Generator was pushed
            if (placedGenerators.has(location)) {
                event.setCancelled(true);
                return;
            }

            // Block above the generator was pushed
            Location locUnder = block.getRelative(BlockFace.DOWN).getLocation();
            if (placedGenerators.has(locUnder)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
