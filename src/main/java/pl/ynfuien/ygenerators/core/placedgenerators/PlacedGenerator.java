package pl.ynfuien.ygenerators.core.placedgenerators;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.ynfuien.ydevlib.utils.BlockBreaker;
import pl.ynfuien.ydevlib.utils.ItemGiver;
import pl.ynfuien.ygenerators.core.BlockLottery;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.hooks.superiorskyblock2.SuperiorSkyblock2Hook;

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
        generateBlock(drawABlock());
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

    public Material drawABlock() {
        double multiplayer = 1;
        if (doubledrop.isActive() && generator.getDoubledropUseMultiplayer()) multiplayer = doubledrop.getMultiplayer();

        Material blockToGenerate = BlockLottery.drawABlock(blocks, multiplayer);
        return blockToGenerate == null ? defaultBlock : blockToGenerate;
    }

    /**
     * @return Whether the block above the generator is empty
     */
    private boolean canGenerate() {
        return blockAbove.isEmpty() || blockAbove.isLiquid();
    }

    /**
     * Destroys this generator.
     */
    public void destroy() {
        destroy(null);
    }

    /**
     * Destroys this generator, giving it to the provided player.
     */
    public void destroy(Player player) {
        Block block = location.getBlock();

        // Break
        if (!block.isEmpty()) BlockBreaker.breakStrikingly(block);

        // Give item to the player
        if (player != null) {
            ItemStack item = generator.getItem().getItemStack(player, durability);
            ItemGiver.giveItems(player, item);
        }

        // Destroy block above if it is a default block
        if (blockAbove.getType().equals(defaultBlock)) BlockBreaker.breakStrikingly(blockAbove);
    }

    /**
     * Decreases durability by provided amount.
     */
    public void decreaseDurability(double amount) {
        if (durability == -1) return;

        durability -= amount;
        if (durability < 0) durability = 0;

        // Round to the second decimal place
        if (durability % 1 != 0) {
            durability = durability * 100d;
            long tmp = Math.round(durability);
            durability = (double) tmp / 100d;
        }
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

    public Block getBlockAbove() {
        return blockAbove;
    }

    public boolean isInfinite() {
        return durability == -1;
    }

    public boolean isUsedUp() {
        return durability == 0;
    }

    public void setDurability(double durability) {
        this.durability = durability;
    }
}
