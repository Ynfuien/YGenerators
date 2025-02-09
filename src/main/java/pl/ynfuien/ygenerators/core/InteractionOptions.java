package pl.ynfuien.ygenerators.core;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.ynfuien.ydevlib.messages.YLogger;

public class InteractionOptions {
    private final boolean sneak;
    private final boolean emptyHand;
    private Action click = Action.RIGHT_CLICK_BLOCK;

    public InteractionOptions(ConfigurationSection config) {
        sneak = config.getBoolean("sneak");
        emptyHand = config.getBoolean("empty-hand");

        // Click
        // l - left
        // r - right
        String clickType = config.getString("click").toLowerCase();
        if (clickType.startsWith("l")) {
            click = Action.LEFT_CLICK_BLOCK;
            return;
        }

        // Log error if click type isn't left or right
        if (!clickType.startsWith("r")) {
            logError(String.format("Click type '%s' is incorrect! Right click type will be used.", clickType));
        }
    }

    private void logError(String message) {
        YLogger.warn("[InteractionOptions] " + message);
    }

    public boolean getSneak() {
        return sneak;
    }

    public boolean getEmptyHand() {
        return emptyHand;
    }

    public Action getClick() {
        return click;
    }

    // Check whether interaction from player is this interaction
    public boolean isInteractionCorrect(PlayerInteractEvent event) {
        // Click
        if (!event.getAction().equals(click)) return false;
        // Sneak
        if (sneak && !event.getPlayer().isSneaking()) return false;
        // Empty hand
        return emptyHand && event.getItem() == null;
    }
}
