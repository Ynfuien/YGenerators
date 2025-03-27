package pl.ynfuien.ygenerators.api.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerator;

public class GeneratorUsedUpEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final PlacedGenerator placedGenerator;
    private final Block brokenBlock;

    private boolean cancelled;


    public GeneratorUsedUpEvent(@NotNull Player player, @NotNull PlacedGenerator placedGenerator) {
        super(false);

        this.player = player;
        this.placedGenerator = placedGenerator;
        this.brokenBlock = placedGenerator.getBlockAbove();
    }

    /**
     * @return Player that used up the generator
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

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
