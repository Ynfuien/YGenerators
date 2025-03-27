package pl.ynfuien.ygenerators.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerator;

public class GeneratorCheckStatusEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final PlacedGenerator placedGenerator;

    private boolean cancelled;


    public GeneratorCheckStatusEvent(@NotNull Player player, @NotNull PlacedGenerator placedGenerator) {
        super(false);

        this.player = player;
        this.placedGenerator = placedGenerator;
    }

    /**
     * @return Player that is checking the status of the generator
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * @return PlacedGenerator instance that is being checked
     */
    public PlacedGenerator getPlacedGenerator() {
        return placedGenerator;
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
