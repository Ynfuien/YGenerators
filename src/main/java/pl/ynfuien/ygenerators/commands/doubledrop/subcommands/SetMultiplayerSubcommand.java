package pl.ynfuien.ygenerators.commands.doubledrop.subcommands;

import org.bukkit.command.CommandSender;
import pl.ynfuien.ydevlib.utils.DoubleFormatter;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.commands.doubledrop.DoubledropCommand;
import pl.ynfuien.ygenerators.core.Doubledrop;

import java.util.HashMap;
import java.util.List;

public class SetMultiplayerSubcommand implements Subcommand {
    private final Doubledrop doubledrop;
    private final static DoubleFormatter df = DoubleFormatter.DEFAULT;

    public SetMultiplayerSubcommand(Doubledrop doubledrop) {
        this.doubledrop = doubledrop;
    }

    @Override
    public String permission() {
        return "ygenerators.doubledrop."+name();
    }

    @Override
    public String name() {
        return "set-multiplayer";
    }

    @Override
    public void run(CommandSender sender, String[] args, HashMap<String, Object> placeholders) {
        if (args.length == 0) {
            Lang.Message.COMMAND_DOUBLEDROP_SET_MULTIPLAYER_FAIL_NO_MULTIPLAYER.send(sender, placeholders);
            return;
        }

        // Get argument
        String arg1 = args[0].toLowerCase();
        placeholders.put("multiplayer", arg1);

        // Parse number
        float multiplayer;
        try {
            multiplayer = Float.parseFloat(arg1);
            placeholders.put("multiplayer", df.format(multiplayer));
        } catch (NumberFormatException e) {
            Lang.Message.COMMAND_DOUBLEDROP_SET_MULTIPLAYER_FAIL_INCORRECT_MULTIPLAYER.send(sender, placeholders);
            return;
        }

        // Incorrect multiplayer
        if (multiplayer < 0) {
            Lang.Message.COMMAND_DOUBLEDROP_SET_MULTIPLAYER_FAIL_INCORRECT_MULTIPLAYER.send(sender, placeholders);
            return;
        }

        // Set multiplayer
        doubledrop.setMultiplayer(multiplayer);

        // Success message
        Lang.Message.COMMAND_DOUBLEDROP_SET_MULTIPLAYER_SUCCESS.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return DoubledropCommand.getCompletionsForTimeArgument(args);
    }
}
