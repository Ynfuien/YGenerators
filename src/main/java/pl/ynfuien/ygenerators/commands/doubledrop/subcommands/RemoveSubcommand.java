package pl.ynfuien.ygenerators.commands.doubledrop.subcommands;

import org.bukkit.command.CommandSender;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.commands.doubledrop.DoubledropCommand;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.Generators;

import java.util.HashMap;
import java.util.List;

public class RemoveSubcommand implements Subcommand {
    private final Generators generators;

    public RemoveSubcommand(Generators generators) {
        this.generators = generators;
    }

    @Override
    public String permission() {
        return "ygenerators.command.doubledrop."+name();
    }

    @Override
    public String name() {
        return "remove";
    }

    @Override
    public void run(CommandSender sender, String[] args, HashMap<String, Object> placeholders) {
        if (args.length == 0) {
            Lang.Message.COMMAND_DOUBLEDROP_REMOVE_FAIL_NO_TIME.send(sender, placeholders);
            return;
        }

        String arg1 = args[0].toLowerCase();

        int multiplayer = 1; // Minutes

        if (arg1.endsWith("h")) multiplayer = 60; // Hours
        else if (arg1.endsWith("d")) multiplayer = 60 * 24; // Days

        // Cut off last char if it is one of these below
        String lastChar = arg1.substring(arg1.length() - 1);
        if (List.of("m", "h", "d").contains(lastChar)) {
            arg1 = arg1.substring(0, arg1.length() - 1);
        }

        // Parse number
        int time;
        try {
            time = Integer.parseInt(arg1);
        } catch (NumberFormatException e) {
            Lang.Message.COMMAND_DOUBLEDROP_FAIL_INCORRECT_TIME.send(sender, placeholders);
            return;
        }

        // Return if time is lower than 0
        if (time < 0) {
            Lang.Message.COMMAND_DOUBLEDROP_FAIL_INCORRECT_TIME.send(sender, placeholders);
            return;
        }

        // Remove time
        Doubledrop doubledrop = generators.getDoubledrop();
        doubledrop.removeTime(time * multiplayer);

        // Deactivated message
        if (doubledrop.getTimeLeft() == 0) {
            Lang.Message.COMMAND_DOUBLEDROP_REMOVE_SUCCESS_DEACTIVATE.send(sender, placeholders);
            return;
        }

        placeholders.put("time", time + lastChar);
        placeholders.put("time-left", doubledrop.getFormattedTimeLeft());

        Lang.Message.COMMAND_DOUBLEDROP_REMOVE_SUCCESS.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return DoubledropCommand.getCompletionsForTimeArgument(args);
    }
}
