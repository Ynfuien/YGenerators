package pl.ynfuien.ygenerators.commands.doubledrop.subcommands;

import org.bukkit.command.CommandSender;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.data.Doubledrop;
import pl.ynfuien.ygenerators.managers.Lang;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SetmultiplayerSubcommand implements Subcommand {
    @Override
    public String permission() {
        return "ygenerators.command.doubledrop."+name();
    }

    @Override
    public String name() {
        return "setmultiplayer";
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
            Lang.Message.COMMAND_DOUBLEDROP_SETMULTIPLAYER_FAIL_NO_MULTIPLAYER.send(sender);
            return;
        }

        // Get first arg
        String arg1 = args[0].toLowerCase();

        // Get multiplayer from first arg
        double multiplayer;
        try {
            multiplayer = Double.parseDouble(arg1);
        } catch (NumberFormatException e) {
            Lang.Message.COMMAND_DOUBLEDROP_SETMULTIPLAYER_FAIL_INCORRECT_MULTIPLAYER.send(sender);
            return;
        }

        // If multiplayer is lower than 0
        if (multiplayer < 0) {
            Lang.Message.COMMAND_DOUBLEDROP_SETMULTIPLAYER_FAIL_INCORRECT_MULTIPLAYER.send(sender);
            return;
        }

        // Get double drop
        Doubledrop doubledrop = YGenerators.getInstance().getGenerators().getDoubledrop();
        // Set double drop multiplayer
        doubledrop.setMultiplayer(multiplayer);

        // Create placeholders hashmap for message
        HashMap<String, Object> placeholders = new HashMap<>();

        // Add time placeholder
        placeholders.put("multiplayer", Util.formatDouble(multiplayer));

        // Send success message
        Lang.Message.COMMAND_DOUBLEDROP_SETMULTIPLAYER_SUCCESS.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        // Create new list for completions
        List<String> completions = new ArrayList<>();

        // Return empty list if args length isn't 1
        if (args.length != 1) return completions;

        // Get first arg
        String arg1 = args[0].toLowerCase();

        // Loop through completions
        for (String completion : Arrays.asList("2", "4", "5", "8")) {
            if (completion.startsWith(arg1)) completions.add(completion);
        }

        return completions;
    }
}
