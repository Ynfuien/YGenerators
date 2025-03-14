package pl.ynfuien.ygenerators.listeners;

import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerators;

public class EntityExplodeListener implements Listener {
    // This event handles explosion of:
    // - generator
    // - generator's generated block

    private final PlacedGenerators placedGenerators;

    public EntityExplodeListener(YGenerators instance) {
        placedGenerators = instance.getPlacedGenerators();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        // Remove block from blow-up blocks if it is a generator or a block above the generator
        e.blockList().removeIf(b -> placedGenerators.has(b.getLocation()) || placedGenerators.has(b.getRelative(BlockFace.DOWN).getLocation()));
    }

}
