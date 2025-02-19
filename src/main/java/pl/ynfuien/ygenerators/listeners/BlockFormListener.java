package pl.ynfuien.ygenerators.listeners;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.VanillaGenerators;
import pl.ynfuien.ygenerators.core.placedgenerators.ChanceSystem;

import java.util.HashMap;

public class BlockFormListener implements Listener {
    // This listener handles forming stone and cobblestone from vanilla generators

    private final YGenerators instance;
    private final Generators generators;
    private final Doubledrop doubledrop;

    public BlockFormListener(YGenerators instance) {
        this.instance = instance;
        generators = instance.getGenerators();
        doubledrop = instance.getDoubledrop();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockForm(BlockFormEvent e) {
        // Get new block state
        BlockState blockState = e.getNewState();

        // Get new block state type
        Material newBlockStateType = blockState.getType();
        // Return if type isn't cobblestone or stone
        if (!newBlockStateType.equals(Material.COBBLESTONE) && !newBlockStateType.equals(Material.STONE)) return;

        // Get vanilla generator settings
        VanillaGenerators vanilla = generators.getVanillaGenerators();
        // Return if feature isn't enabled
        if (!vanilla.isEnabled()) return;

        // Set multiplayer to 1
        double multiplayer = 1;
        // If vanilla generators use double drop
        if (vanilla.getUseDoubledrop()) {
            // If double drop is active, set multiplayer to double drop multiplayer
            if (doubledrop.isActive()) multiplayer = doubledrop.getMultiplayer();
        }

        // Get default block
        Material defaultBlock = vanilla.getDefaultBlock();
        // Get blocks
        HashMap<Material, Double> blocks = vanilla.getBlocks();

        // Get block to generate
        Material blockToGenerate = ChanceSystem.getBlockToGenerate(blocks, multiplayer);

        // If no block hit the chance
        if (blockToGenerate == null) {
            // Return if no block has to be generated
            if (defaultBlock == null) return;

            // Set block to generate to default block
            blockToGenerate = defaultBlock;
        }

        // Return if block to generate is the same as original block
        if (newBlockStateType.equals(blockToGenerate)) return;

        // Set new block state to generated block
        blockState.setType(blockToGenerate);
    }
}
