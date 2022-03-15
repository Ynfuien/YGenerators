package pl.ynfuien.ygenerators.commands.doubledrop.subcommands;

import org.bukkit.command.CommandSender;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.data.Doubledrop;
import pl.ynfuien.ygenerators.managers.Lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SetSubcommand implements Subcommand {
    @Override
    public String permission() {
        return "ygenerators.command.doubledrop."+name();
    }

    @Override
    public String name() {
        return "set";
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        // Return if no args are provided
        if (args.length == 0) {
            Lang.Message.COMMAND_DOUBLEDROP_SET_FAIL_NO_TIME.send(sender);
            return;
        }

        // Get first arg
        String arg1 = args[0].toLowerCase();


        // Get correct unit
        char unit = 'n';

        if (arg1.endsWith("h")) unit = 'h';
        if (arg1.endsWith("m")) unit = 'm';

        if (unit != 'n') {
            arg1 = arg1.substring(0, arg1.length() - 1);
        } else {
            unit = 'm';
        }


        // Get time from first arg
        long time;
        try {
            time = Long.parseLong(arg1);
        } catch (NumberFormatException e) {
            Lang.Message.COMMAND_DOUBLEDROP_FAIL_INCORRECT_TIME.send(sender);
            return;
        }

        // If time is higher than 0
        if (time > 0) {
            // Multiply time times 60 if unit is hour
            if (unit == 'h') time *= 60;
        }

        // Get double drop
        Doubledrop doubledrop = YGenerators.getInstance().getGenerators().getDoubledrop();
        // Set double drop time left
        doubledrop.setTimeLeft(time);

        // Send success deactivate message if time is 0
        if (time == 0) {
            Lang.Message.COMMAND_DOUBLEDROP_SET_SUCCESS_DEACTIVATE.send(sender);
            return;
        }

        // Create placeholders hashmap for message
        HashMap<String, Object> placeholders = new HashMap<>();

        // Add time left placeholder
        placeholders.put("time", doubledrop.getFormattedTimeLeft());

        // Send success message
        Lang.Message.COMMAND_DOUBLEDROP_SET_SUCCESS.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        // Create new list for completions
        List<String> completions = new ArrayList<>();

        // Return empty list if args length isn't 1
        if (args.length != 1) return completions;

        // Get first arg
        String arg1 = args[0].toLowerCase();

        // If arg length is 0
        if (arg1.length() == 0) {
            // Add completions
            completions.addAll(Arrays.asList("1h", "2h", "3h", "60m", "120m", "180m", "-1"));

            return completions;
        }

        // Return if arg starts with '-'
        if (arg1.startsWith("-")) return completions;


        try {
            Integer.valueOf(arg1);

            completions.add(arg1 + "h");
            completions.add(arg1 + "m");
        } catch (NumberFormatException ignored) {}

        return completions;
    }
}
