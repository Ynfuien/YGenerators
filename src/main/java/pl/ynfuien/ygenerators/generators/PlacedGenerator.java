package pl.ynfuien.ygenerators.generators;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.utils.Items;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.HashMap;

public class PlacedGenerator {
    private Generator generator;
    private double durability;
    private Location location;

    private Block blockAbove;
    private Material defaultBlock;
    private HashMap<Material, Double> blocks;
    private Doubledrop doubledrop;

    // Constructor
    public PlacedGenerator(@NotNull Generator generator, double durability, @NotNull Location location) {
        this.generator = generator;
        this.durability = durability;
        this.location = location;

        // Get block above generator
        blockAbove = location.getBlock().getRelative(BlockFace.UP);

        // Get default block
        defaultBlock = generator.getDefaultBlock();
        // Get blocks
        blocks = generator.getBlocks();
        // Get double drop
        doubledrop = generator.getGenerators().getDoubledrop();
    }

    // Generates block
    public boolean generateBlock() {
        return generateBlock(false);
    }
    // Generates block
    public boolean generateBlock(boolean force) {
        // Return if generator can't generate block
        if (!force && !canGenerate()) return false;

        double multiplayer = 1;
        if (doubledrop.isActive() && generator.getDoubledropUseMultiplayer()) multiplayer = doubledrop.getMultiplayer();

        // Get block to generate
        Material blockToGenerate = ChanceSystem.getBlockToGenerate(blocks, multiplayer);

        // Set block to generate to default block if no block from blocks were set
        if (blockToGenerate == null) blockToGenerate = defaultBlock;
        // Set block above generator to block to generate
        blockAbove.setType(blockToGenerate);

        // Handle placing block in SuperiorSkyblock2 islands
        if (Util.isSS2Enabled()) {
            Island island = SuperiorSkyblockAPI.getIslandAt(location);
            if (island == null) return true;
            island.handleBlockPlace(blockAbove);
        }

        return true;
    }

    // Generates provided block
    public boolean generateBlock(Material block) {
        return generateBlock(block, false);
    }
    // Generates provided block
    public boolean generateBlock(Material block, boolean force) {
        // Return if provided material isn't block
        if (!block.isBlock()) return false;

        // Return if generator can't generate block
        if (!force && !canGenerate()) return false;

        // Set block type to provided block
        blockAbove.setType(block);

        // Handle placing block in SuperiorSkyblock2 islands
        if (Util.isSS2Enabled()) {
            Island island = SuperiorSkyblockAPI.getIslandAt(location);
            if (island == null) return true;
            island.handleBlockPlace(blockAbove);
        }
        return true;
    }

    // Check whether generator can generate block
    private boolean canGenerate() {
        // Return true if block above is air or liquid
        if (blockAbove.isEmpty() || blockAbove.isLiquid()) {
            return true;
        }

        return false;
    }

    // Destroy generator
    public void destroy() {
        destroy(null);
    }
    public void destroy(Player p) {
        // Get generator block
        Block b = location.getBlock();

        // If generator block isn't air
        if (!b.isEmpty()) {
            // Destroy generator block
            Util.breakNaturally(b);
        }

        // Check if player isn't null
        if (p != null) {
            // Get generator item
            ItemStack geneItem = generator.getItem().getItemStack(p, durability);
            // Give item to player
            Items.giveItems(p, geneItem);
        }

        // Destroy block above generator if it is default block
        Material type = blockAbove.getType();
        if (type.equals(defaultBlock)) {
            Util.breakNaturally(blockAbove);
        }
    }


    // Gets placed generator's generator
    public Generator getGenerator() {
        return generator;
    }

    // Gets placed generator's location
    public Location getLocation() {
        return location;
    }

    // Gets generator durability
    public double getDurability() {
        return durability;
    }

    // Sets generator durability
    public void setDurability(double durability) {
        this.durability = durability;
    }

    // Decreases generator durability
    public void decreaseDurability(double amount) {
        // Return if durability is -1 (infinite generator)
        if (durability == -1) return;
        // Decrease durability by amount
        durability -= amount;
        // Set durability to 0 if it is lower than 0
        if (durability < 0) durability = 0;
    }
}
