package pl.ynfuien.ygenerators.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messages {
    // Formats colors and formats in text
    public static String formatColors(String text) {
        // If RGB is supported format hex colors
        if (Util.isRGBSupported()) {
            Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");

            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String color = text.substring(matcher.start(), matcher.end());
                text = text.replace(color, ChatColor.of(color.substring(1)).toString());
                matcher = pattern.matcher(text);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    // Formats PAPI placeholders for provided player
    public static String formatPapi(Player player, String text) {
        if (!Util.isPapiEnabled()) return text;
        if (player == null) return text;

        return PlaceholderAPI.setPlaceholders(player, text);
    }

    // Formats both colors and papi placeholders
    public static String format(Player player, String text) {
        text = formatColors(text);
        if (!Util.isPapiEnabled()) return text;

        return PlaceholderAPI.setPlaceholders(player, text);
    }

    // Formats colors and placeholders and sends message to CommandSender
    public static void send(CommandSender sender, String message) {
        // If command sender is player, parse placeholders in message
        if (sender instanceof Player) message = formatPapi((Player) sender, message);

        // Colour message
        message = formatColors(message);

        // Send message
        sender.sendMessage(message);
    }

    // Formats colors and placeholders and sends action bar to player
    public static void sendActionBar(Player p, String message) {
        // If command sender is player, parse placeholders in message
        message = formatPapi(p, message);

        // Colour message
        message = formatColors(message);

        // Send action bar
        p.sendActionBar(message);
    }
}
