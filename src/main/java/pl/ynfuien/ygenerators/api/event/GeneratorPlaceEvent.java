package pl.ynfuien.ygenerators.api.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ygenerators.core.generator.Generator;

public class GeneratorPlaceEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Generator generator;
    private final Block block;
    private final ItemStack generatorItem;
    private double durability;

    private boolean cancelled;

    public GeneratorPlaceEvent(@NotNull Player player, @NotNull Generator generator, @NotNull Block block, @NotNull ItemStack generatorItem, double durability) {
        super(false);

        this.player = player;
        this.generator = generator;
        this.block = block;
        this.generatorItem = generatorItem;
        this.durability = durability;
    }

    /**
     * @return Player that placed the generator
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * @return Generator instance of placed generator item
     */
    public Generator getGenerator() {
        return generator;
    }

    /**
     * @return Block instance of this event
     */
    public Block getBlock() {
        return block;
    }

    /**
     * @return Generator item that was used
     */
    public ItemStack getGeneratorItem() {
        return generatorItem;
    }

    /**
     * @return Durability of this generator
     */
    public double getDurability() {
        return durability;
    }

    /**
     * Sets durability of the new placed generator.
     */
    public void setDurability(double durability) {
        this.durability = durability;
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
     * Also, cancelling this event will cancel underlying BlockPlaceEvent.
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
