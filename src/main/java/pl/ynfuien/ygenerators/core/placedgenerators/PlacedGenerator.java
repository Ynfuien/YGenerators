package pl.ynfuien.ygenerators.core.placedgenerators;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.hooks.superiorskyblock2.SuperiorSkyblock2Hook;
import pl.ynfuien.ygenerators.utils.Items;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.HashMap;

public class PlacedGenerator {
    private final Generator generator;
    private final Location location;
    private double durability;

    private final Block blockAbove;
    private final Material defaultBlock;
    private final HashMap<Material, Double> blocks;
    private final Doubledrop doubledrop;

    public PlacedGenerator(Generator generator, Location location, double durability) {
        this.generator = generator;
        this.location = location;
        this.durability = durability;

        blockAbove = location.getBlock().getRelative(BlockFace.UP);

        defaultBlock = generator.getDefaultBlock();
        blocks = generator.getBlocks();
        doubledrop = generator.getGenerators().getInstance().getDoubledrop();

        generateDefaultBlock();
    }

    /**
     * Generates the default block
     */
    public void generateDefaultBlock() {
        generateBlock(defaultBlock);
    }

    /**
     * Generates a block based on given chances
     */
    public void generateBlock() {
        // Multiplayer
        double multiplayer = 1;
        if (doubledrop.isActive() && generator.getDoubledropUseMultiplayer()) multiplayer = doubledrop.getMultiplayer();

        // Block to set
        Material blockToGenerate = ChanceSystem.getBlockToGenerate(blocks, multiplayer);
        if (blockToGenerate == null) blockToGenerate = defaultBlock;

        generateBlock(blockToGenerate);
    }

    /**
     * Generates given type of block
     * @param type A block type to generate
     */
    public void generateBlock(Material type) {
        if (!canGenerate()) return;
        if (!type.isBlock()) return;

        blockAbove.setType(type);

        // Handle block placing on SuperiorSkyblock2 islands
        if (SuperiorSkyblock2Hook.isEnabled()) {
            Island island = SuperiorSkyblockAPI.getIslandAt(location);
            if (island == null) return;

            island.handleBlockPlace(blockAbove);
        }
    }

    private boolean canGenerate() {
        return blockAbove.isEmpty() || blockAbove.isLiquid();
    }

    public void destroy() {
        destroy(null);
    }
    public void destroy(Player player) {
        Block block = location.getBlock();

        // Break
        if (!block.isEmpty()) Util.breakNaturally(block);

        // Give item to the player
        if (player != null) {
            ItemStack item = generator.getItem().getItemStack(player, durability);
            Items.giveItems(player, item);
        }

        // Destroy block above if it is a default block
        if (blockAbove.getType().equals(defaultBlock)) Util.breakNaturally(blockAbove);
    }

    public void decreaseDurability(double amount) {
        if (durability == -1) return;

        durability -= amount;
        if (durability < 0) durability = 0;
    }

    public Generator getGenerator() {
        return generator;
    }

    public Location getLocation() {
        return location;
    }

    public double getDurability() {
        return durability;
    }

    public boolean isInfinite() {
        return durability == -1;
    }

    public void setDurability(double durability) {
        this.durability = durability;
    }
}
