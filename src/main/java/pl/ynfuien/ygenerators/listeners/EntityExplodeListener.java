package pl.ynfuien.ygenerators.listeners;

import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.generators.GeneratorsDatabase;

public class EntityExplodeListener implements Listener {
    // This event handles explosion of:
    // - generator
    // - generator's generated block

    private final GeneratorsDatabase generatorsDatabase;
    public EntityExplodeListener(YGenerators instance) {
        generatorsDatabase = instance.getDatabase();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        // Remove block from blow-up blocks if it is a generator or generator's generated block
        e.blockList().removeIf(b -> generatorsDatabase.has(b.getLocation()) || generatorsDatabase.has(b.getRelative(BlockFace.DOWN).getLocation()));
    }

}
