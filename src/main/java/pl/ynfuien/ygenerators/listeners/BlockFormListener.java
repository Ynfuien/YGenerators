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
    public void onBlockForm(BlockFormEvent event) {
        BlockState blockState = event.getNewState();

        Material blockType = blockState.getType();
        if (!blockType.equals(Material.COBBLESTONE) && !blockType.equals(Material.STONE)) return;

        VanillaGenerators vanilla = generators.getVanillaGenerators();
        if (!vanilla.isEnabled()) return;

        double multiplayer = 1;
        if (vanilla.getUseDoubledrop() && doubledrop.isActive()) multiplayer = doubledrop.getMultiplayer();

        Material defaultBlock = vanilla.getDefaultBlock();
        HashMap<Material, Double> blocks = vanilla.getBlocks();

        Material blockToGenerate = ChanceSystem.getBlockToGenerate(blocks, multiplayer);

        if (blockToGenerate == null) {
            if (defaultBlock == null) return;

            blockToGenerate = defaultBlock;
        }

//        if (blockType.equals(blockToGenerate)) return;

        blockState.setType(blockToGenerate);
    }
}
