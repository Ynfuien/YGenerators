package pl.ynfuien.ygenerators.managers;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ygenerators.utils.Logger;
import pl.ynfuien.ygenerators.utils.Messages;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.Arrays;
import java.util.HashMap;

public class Lang {
    private static String prefix;
    private static FileConfiguration lang;

    public static void reloadLang(FileConfiguration lang) {
        Lang.lang = lang;
        prefix = Message.PREFIX.get();
    }

    // Gets message by message enum
    @Nullable
    public static String get(Message message) {
        return get(message.getName());
    }
    // Gets message by path
    @Nullable
    public static String get(String path) {
        return lang.getString(path);
    }
    // Gets message by path
    @Nullable
    public static String get(String path, HashMap<String, Object> placeholders) {
        // Return message with used placeholders
        return Util.replacePlaceholders(lang.getString(path), placeholders);
    }

    // Send message from message enum
    public static void sendMessage(CommandSender sender, Message message) {
        sendMessage(sender, message.getName());
    }
    // Send message from path
    public static void sendMessage(CommandSender sender, String path) {
        sendMessage(sender, path, new HashMap<>());
    }
    // Send message from path
    public static void sendMessage(CommandSender sender, String path, HashMap<String, Object> placeholders) {
        // Get message
        String message = lang.getString(path);

        // Return and log error if message doesn't exist
        if (message == null) {
            Logger.logError(String.format("There is no message '%s'!", path));
            return;
        }

        // Return if message is empty
        if (message.isEmpty()) return;

        // Get message with used placeholders
        message = Util.replacePlaceholders(message, placeholders);

        Messages.send(sender, prefix + message);
    }

    // In polish we have something like this:
    // Została 1 strona
    // Zostały 2/3/4 strony
    // Zostało 5-21 stron
    // Zostały 22-24 strony
    // Zostało 25-31 stron
    // etc.
    // When in english is just:
    // 1 page is left
    // 2-99999 pages are left
    // So that's why this method is there
    public static WordType getWordType(double number) {
        // If number is one
        if (number == 1) return WordType.SINGULAR;
        // If number have decimal numbers
        if (number != (int) number) return WordType.PLURAR;

        // If number is lower than 10 or higher than 20
        if (number < 10 || number > 20) {
            // If number ends with 2, 3 or 4
            String string = String.valueOf((int) number);
            if (Arrays.asList("2", "3", "4").contains(string.substring(string.length() -1))) {
                return WordType.PLURAR_2_4;
            }
        }


        return WordType.PLURAR;
    }

    public enum WordType {
        SINGULAR,
        PLURAR,
        PLURAR_2_4
    }

    // Messages enum
    public enum Message {
        PREFIX,
        NO_PERMISSION,
        ONLY_PLAYER,
        PLUGIN_IS_RELOADING,
        PLUGIN_IS_DISABLING,
        HELP_NO_COMMANDS,
        HELP_TOP,
        HELP_COMMAND_TEMPLATE,
        COMMANDS_USAGE_GENERATOR,
        COMMANDS_USAGE_PLAYER,
        COMMANDS_USAGE_AMOUNT,
        COMMANDS_USAGE_DURABILITY,
        COMMAND_HELP_DESCRIPTION,
        COMMAND_RELOAD_DESCRIPTION,
        COMMAND_GIVE_DESCRIPTION,
        COMMAND_GIVE_FAIL_NO_GENERATOR,
        COMMAND_GIVE_FAIL_GENERATOR_DOESNT_EXIST,
        COMMAND_GIVE_FAIL_NO_PLAYER,
        COMMAND_GIVE_FAIL_PLAYER_ISNT_ONLINE,
        COMMAND_GIVE_FAIL_INCORRECT_AMOUNT,
        COMMAND_GIVE_FAIL_INCORRECT_DURABILITY,
        COMMAND_GIVE_ACCEPT_HIGH_AMOUNT,
        COMMAND_GIVE_SUCCESS_SELF,
        COMMAND_GIVE_SUCCESS_NOSELF,
        COMMAND_GIVE_SUCCESS_NOSELF_MANY,
        COMMAND_GIVE_SUCCESS_NOSELF_DURABILITY,
        COMMAND_RELOAD_FAIL,
        COMMAND_RELOAD_SUCCESS,
        COMMAND_DOUBLEDROP_TIME_ACTIVE,
        COMMAND_DOUBLEDROP_TIME_ACTIVE_MULTIPLAYER,
        COMMAND_DOUBLEDROP_TIME_INACTIVE,
        COMMAND_DOUBLEDROP_USAGE,
        COMMAND_DOUBLEDROP_FAIL_INCORRECT_TIME,
        COMMAND_DOUBLEDROP_ADD_SUCCESS,
        COMMAND_DOUBLEDROP_ADD_FAIL_NO_TIME,
        COMMAND_DOUBLEDROP_REMOVE_SUCCESS,
        COMMAND_DOUBLEDROP_REMOVE_SUCCESS_DEACTIVATE,
        COMMAND_DOUBLEDROP_REMOVE_FAIL_NO_TIME,
        COMMAND_DOUBLEDROP_SET_SUCCESS,
        COMMAND_DOUBLEDROP_SET_SUCCESS_DEACTIVATE,
        COMMAND_DOUBLEDROP_SET_FAIL_NO_TIME,
        COMMAND_DOUBLEDROP_SETMULTIPLAYER_SUCCESS,
        COMMAND_DOUBLEDROP_SETMULTIPLAYER_FAIL_INCORRECT_MULTIPLAYER,
        COMMAND_DOUBLEDROP_SETMULTIPLAYER_FAIL_NO_MULTIPLAYER,
        GENERATOR_DENY_DISABLED_WORLD,
        GENERATOR_DENY_UNKNOWN_NAME,
        GENERATOR_DENY_PLACE_ABOVE,
        GENERATOR_DENY_PLACE_UNDER,
        GENERATOR_DENY_LIMIT_GLOBAL,
        GENERATOR_DENY_LIMIT_SINGLE,
        GENERATOR_DENY_COOLDOWN,
        GENERATOR_DENY_BREAK,
        GENERATOR_ALERT_BROKEN,
        GENERATOR_ALERT_LOW_DURABILITY,
        GENERATOR_ALERT_DURABILITY_WORD_SINGULAR,
        GENERATOR_ALERT_DURABILITY_WORD_PLURAL,
        GENERATOR_ALERT_DURABILITY_WORD_PLURAL_2_4,
        GENERATOR_INFO,
        GENERATOR_INFO_INFINITE,
        DOUBLEDROP_TIME,
        DOUBLEDROP_TIME_INFINITY,
        DOUBLEDROP_END,
        DOUBLEDROP_PLACEHOLDER_ACTIVE,
        DOUBLEDROP_PLACEHOLDER_INACTIVE,
        DOUBLEDROP_PLACEHOLDER_STATUS,
        DOUBLEDROP_PLACEHOLDER_STATUS_MULTIPLAYER,
        ;

        // Gets message name
        public String getName() {
            return name().toLowerCase().replace("_", "-");
        }

        // Gets message
        public String get() {
            return Lang.get(getName());
        }
        // Gets message
        public String get(HashMap<String, Object> placeholders) {
            return Lang.get(getName(), placeholders);
        }

        // Sends message
        public void send(CommandSender sender, HashMap<String, Object> placeholders) {
            Lang.sendMessage(sender, getName(), placeholders);
        }
        // Sends message
        public void send(CommandSender sender) {
            Lang.sendMessage(sender, getName());
        }
    }
}
