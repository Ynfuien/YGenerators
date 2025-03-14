package pl.ynfuien.ygenerators;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import pl.ynfuien.ydevlib.messages.LangBase;
import pl.ynfuien.ydevlib.messages.Messenger;
import pl.ynfuien.ydevlib.messages.colors.ColorFormatter;

import java.util.Arrays;
import java.util.HashMap;

public class Lang extends LangBase {
    public enum Message implements LangBase.Message {
        PREFIX,
        NO_PERMISSION,
        ONLY_PLAYER,
        COMMAND_MAIN_USAGE,
        COMMAND_GIVE_USAGE,
        COMMAND_RELOAD_FAIL,
        COMMAND_RELOAD_SUCCESS,
        COMMAND_GIVE_FAIL_NO_GENERATOR,
        COMMAND_GIVE_FAIL_GENERATOR_DOESNT_EXIST,
        COMMAND_GIVE_FAIL_NO_PLAYER,
        COMMAND_GIVE_FAIL_PLAYER_ISNT_ONLINE,
        COMMAND_GIVE_FAIL_INCORRECT_AMOUNT,
        COMMAND_GIVE_FAIL_INCORRECT_DURABILITY,
        COMMAND_GIVE_ACCEPT_HIGH_AMOUNT,
        COMMAND_GIVE_SUCCESS_SELF,
        COMMAND_GIVE_SUCCESS_PLAYER,
        COMMAND_GIVE_SUCCESS_PLAYER_MANY,
        COMMAND_GIVE_SUCCESS_PLAYER_DURABILITY,
        COMMAND_DOUBLEDROP_USAGE,
        COMMAND_DOUBLEDROP_TIME_ACTIVE,
        COMMAND_DOUBLEDROP_TIME_ACTIVE_MULTIPLAYER,
        COMMAND_DOUBLEDROP_TIME_INACTIVE,
        COMMAND_DOUBLEDROP_FAIL_INCORRECT_TIME,
        COMMAND_DOUBLEDROP_ADD_SUCCESS,
        COMMAND_DOUBLEDROP_ADD_FAIL_NO_TIME,
        COMMAND_DOUBLEDROP_REMOVE_SUCCESS,
        COMMAND_DOUBLEDROP_REMOVE_SUCCESS_DEACTIVATE,
        COMMAND_DOUBLEDROP_REMOVE_FAIL_NO_TIME,
        COMMAND_DOUBLEDROP_SET_SUCCESS,
        COMMAND_DOUBLEDROP_SET_SUCCESS_DEACTIVATE,
        COMMAND_DOUBLEDROP_SET_FAIL_NO_TIME,
        COMMAND_DOUBLEDROP_SET_MULTIPLAYER_SUCCESS,
        COMMAND_DOUBLEDROP_SET_MULTIPLAYER_FAIL_INCORRECT_MULTIPLAYER,
        COMMAND_DOUBLEDROP_SET_MULTIPLAYER_FAIL_NO_MULTIPLAYER,
        GENERATOR_DENY_DISABLED_WORLD,
        GENERATOR_DENY_UNKNOWN_NAME,
        GENERATOR_DENY_DURABILITY_NOT_SET,
        GENERATOR_DENY_PLACE_ABOVE,
        GENERATOR_DENY_PLACE_UNDER,
        GENERATOR_DENY_LIMIT_ALL,
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

        /**
         * Gets name/path of this message.
         */
        @Override
        public String getName() {
            return name().toLowerCase().replace('_', '-');
        }

        /**
         * Gets original unformatted message.
         */
        public String get() {
            return Lang.get(getName());
        }

        /**
         * Gets message with parsed:
         * - {prefix} placeholder
         * - additional provided placeholders
         */
        public String get(HashMap<String, Object> placeholders) {
            return Lang.get(getName(), placeholders);
        }

        /**
         * Gets message with parsed:
         * - PlaceholderAPI
         * - {prefix} placeholder
         * - additional provided placeholders
         */
        public String get(CommandSender sender, HashMap<String, Object> placeholders) {
            return ColorFormatter.parsePAPI(sender, Lang.get(getName(), placeholders));
        }

        /**
         * Gets message as component with parsed:
         * - MiniMessage
         * - PlaceholderAPI
         * - {prefix} placeholder
         * - additional provided placeholders
         */
        public Component getComponent(CommandSender sender, HashMap<String, Object> placeholders) {
            return Messenger.parseMessage(sender, Lang.get(getName()), placeholders);
        }

        /**
         * Sends this message to provided sender.<br/>
         * Parses:<br/>
         * - MiniMessage<br/>
         * - PlaceholderAPI<br/>
         * - {prefix} placeholder
         */
        public void send(CommandSender sender) {
            this.send(sender, new HashMap<>());
        }

        /**
         * Sends this message to provided sender.<br/>
         * Parses:<br/>
         * - MiniMessage<br/>
         * - PlaceholderAPI<br/>
         * - {prefix} placeholder<br/>
         * - additional provided placeholders
         */
        public void send(CommandSender sender, HashMap<String, Object> placeholders) {
            Lang.sendMessage(sender, this, placeholders);
        }
    }

    // In polish there is something like this:
    // Została 1 strona
    // Zostały 2/3/4 strony
    // Zostało 5-21 stron
    // Zostały 22-24 strony
    // Zostało 25-31 stron
    // etc.
    // When in english it's just:
    // 1 page is left
    // 2-99999 pages are left
    // So that's why this thing is there
    public static WordType getWordType(double number) {
        // If number is one
        if (number == 1) return WordType.SINGULAR;
        // If number have decimal numbers
        if (number != (int) number) return WordType.PLURAL;

        // If number is lower than 10 or higher than 20
        if (number < 10 || number > 20) {
            // If number ends with 2, 3 or 4
            String string = String.valueOf((int) number);
            if (Arrays.asList("2", "3", "4").contains(string.substring(string.length() -1))) {
                return WordType.PLURAL_2_4;
            }
        }

        return WordType.PLURAL;
    }

    public enum WordType {
        SINGULAR,
        PLURAL,
        PLURAL_2_4
    }
}
