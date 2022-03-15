package pl.ynfuien.ygenerators.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ygenerators.utils.Logger;

public class InteractionOptions {
    private final boolean sneak;
    private final boolean emptyHand;
    private Action click = Action.RIGHT_CLICK_BLOCK;

    public InteractionOptions(ConfigurationSection config) {
        // Sneak
        sneak = config.getBoolean("sneak");
        // Empty hand
        emptyHand = config.getBoolean("empty-hand");

        // Click
        // l - left
        // r - right
        String clickType = config.getString("click");
        if (clickType.startsWith("l")) {
            click = Action.LEFT_CLICK_BLOCK;
            return;
        }

        // Log error if click type isn't left or right
        if (!clickType.startsWith("r")) {
            logError("Click type is incorrect! Will be used right click type.");
        }
    }

    private void logError(String message) {
        Logger.logWarning("[InteractionOptions] " + message);
    }

    // Gets whether player must sneak to interact
    public boolean getSneak() {
        return sneak;
    }

    // Gets whether player must have empty hand to interact
    public boolean getEmptyHand() {
        return emptyHand;
    }

    // Gets click type
    @NotNull
    public Action getClick() {
        return click;
    }

    // Check whether interaction from player is this interaction
    public boolean isInteractionCorrect(PlayerInteractEvent e) {
        // Return false if click isn't the same
        if (!e.getAction().equals(click)) return false;

        // Return false if sneak is needed but player isn't sneaking
        if (sneak && !e.getPlayer().isSneaking()) return false;

        // Return false if empty hand is needed but player doesn't have empty hand or return true otherwise
        return emptyHand && e.getItem() == null;
    }
}
