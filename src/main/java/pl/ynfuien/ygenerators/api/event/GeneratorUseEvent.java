package pl.ynfuien.ygenerators.api.event;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerator;

public class GeneratorUseEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final PlacedGenerator placedGenerator;
    private final Block brokenBlock;

    private Material blockToGenerate;
    private int cooldown;
    private double usedDurability = 0;

    private boolean cancelled;


    public GeneratorUseEvent(@NotNull Player player, @NotNull PlacedGenerator placedGenerator) {
        super(false);

        this.player = player;
        this.placedGenerator = placedGenerator;
        this.brokenBlock = placedGenerator.getBlockAbove();

        Generator generator = placedGenerator.getGenerator();
        this.blockToGenerate = placedGenerator.drawABlock();
        this.cooldown = generator.getCooldown();

        if (placedGenerator.isInfinite()) return;
        usedDurability = 1d;
        Doubledrop doubledrop = generator.getGenerators().getInstance().getDoubledrop();
        if (doubledrop.isActive()) usedDurability = generator.getDoubledropDurabilityDecrease();
    }

    /**
     * @return Player that used the generator
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * @return PlacedGenerator instance of this generator
     */
    @NotNull
    public PlacedGenerator getPlacedGenerator() {
        return placedGenerator;
    }

    /**
     * @return Block instance that was broken
     */
    @NotNull
    public Block getBrokenBlock() {
        return brokenBlock;
    }

    /**
     * @return Block material that will be generated
     */
    @NotNull
    public Material getBlockToGenerate() {
        return blockToGenerate;
    }

    /**
     * @return Cooldown time after which new block will be generated
     */
    public int getCooldown() {
        return cooldown;
    }

    /**
     * @return An amount of durability that will be used
     */
    public double getUsedDurability() {
        return usedDurability;
    }

    /**
     * Sets what block to generate.
     */
    public void setBlockToGenerate(@NotNull Material blockToGenerate) {
        Preconditions.checkArgument(blockToGenerate != null, "BlockToGenerate cannot be null");

        this.blockToGenerate = blockToGenerate;
    }

    /**
     * Sets a cooldown for generating next block.
     */
    public void setCooldown(int cooldown) {
        if (cooldown < 0) cooldown = 0;
        this.cooldown = cooldown;
    }

    /**
     * Sets amount of durability that will be used from the generator.
     */
    public void setUsedDurability(double usedDurability) {
        this.usedDurability = usedDurability;
    }

    /**
     * @return Whether event should be cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether to cancel this event. Note that this won't send automatically any message to the player if event gets cancelled.
     * Also, cancelling this event will cancel underlying BlockBreakEvent.
     */
    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
