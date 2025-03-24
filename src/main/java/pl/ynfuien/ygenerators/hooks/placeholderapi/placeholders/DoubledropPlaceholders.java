package pl.ynfuien.ygenerators.hooks.placeholderapi.placeholders;

import org.bukkit.OfflinePlayer;
import pl.ynfuien.ydevlib.utils.DoubleFormatter;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.hooks.placeholderapi.Placeholder;

import java.util.HashMap;

public class DoubledropPlaceholders implements Placeholder {
    private final Doubledrop doubledrop;

    private final static DoubleFormatter df = DoubleFormatter.DEFAULT;

    public DoubledropPlaceholders(YGenerators instance) {
        this.doubledrop = instance.getDoubledrop();
    }

    @Override
    public String name() {
        return "doubledrop";
    }

    @Override
    public String getPlaceholder(String id, OfflinePlayer player) {
        double multiplayer = doubledrop.getMultiplayer();
        id = id.toLowerCase();

        // Placeholder: %ygenerators_doubledrop_active%
        // Returns: Lang DOUBLEDROP_PLACEHOLDER_INACTIVE / DOUBLEDROP_PLACEHOLDER_ACTIVE
        if (id.equals("active")) {
            Lang.Message status = Lang.Message.DOUBLEDROP_PLACEHOLDER_INACTIVE;
            if (doubledrop.isActive()) {
                status = Lang.Message.DOUBLEDROP_PLACEHOLDER_ACTIVE;
            }

            return status.get();
        }

        // Placeholder: %ygenerators_doubledrop_active_raw%
        // Returns: true / false
        if (id.equals("active_raw")) {
            return String.valueOf(doubledrop.isActive());
        }

        // Placeholder: %ygenerators_doubledrop_multiplayer%
        // Returns: double drop multiplayer
        if (id.equals("multiplayer")) {
            return df.format(multiplayer);
        }

        // Placeholder: %ygenerators_doubledrop_time_left%
        // Returns: double drop time left in minutes
        if (id.equals("time_left")) {
            return String.valueOf(doubledrop.getTimeLeft());
        }

        // Placeholders:
        // - %ygenerators_doubledrop_status%
        // - %ygenerators_doubledrop_status_multiplayer%
        // - %ygenerators_doubledrop_status_multiplayer_always%
        // Returns: Lang DOUBLEDROP_PLACEHOLDER_STATUS / DOUBLEDROP_PLACEHOLDER_STATUS_MULTIPLAYER / DOUBLEDROP_PLACEHOLDER_INACTIVE
        if (id.startsWith("status")) {
            // Get id after "status"
            id = id.substring(6);

            HashMap<String, Object> placeholders = new HashMap<>();
            placeholders.put("time", doubledrop.getFormattedTimeLeft());
            placeholders.put("multiplayer", df.format(multiplayer));

            // Return inactive message if double drop is... inactive
            if (!doubledrop.isActive()) return Lang.Message.DOUBLEDROP_PLACEHOLDER_INACTIVE.get();

            // Get normal status message
            Lang.Message statusMessage = Lang.Message.DOUBLEDROP_PLACEHOLDER_STATUS;

            // If id was only "status"
            if (id.isEmpty()) return statusMessage.get(placeholders);

            // If id was "status_multiplayer"
            if (id.equals("_multiplayer")) {
                if (multiplayer != 2) {
                    statusMessage = Lang.Message.DOUBLEDROP_PLACEHOLDER_STATUS_MULTIPLAYER;
                }

                return statusMessage.get(placeholders);
            }

            // If id was "status_multiplayer_always"
            if (id.equals("_multiplayer_always")) {
                return Lang.Message.DOUBLEDROP_PLACEHOLDER_STATUS_MULTIPLAYER.get(placeholders);
            }

            return null;
        }

        return null;
    }
}
