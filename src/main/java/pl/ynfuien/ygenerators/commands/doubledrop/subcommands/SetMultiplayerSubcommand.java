package pl.ynfuien.ygenerators.commands.doubledrop.subcommands;

import org.bukkit.command.CommandSender;
import pl.ynfuien.ydevlib.utils.DoubleFormatter;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.commands.doubledrop.DoubledropCommand;
import pl.ynfuien.ygenerators.data.Doubledrop;
import pl.ynfuien.ygenerators.data.Generators;

import java.util.HashMap;
import java.util.List;

public class SetMultiplayerSubcommand implements Subcommand {
    private final Generators generators;
    private final static DoubleFormatter df = new DoubleFormatter();

    public SetMultiplayerSubcommand(Generators generators) {
        this.generators = generators;
    }

    @Override
    public String permission() {
        return "ygenerators.command.doubledrop."+name();
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
        double multiplayer;
        try {
            multiplayer = Double.parseDouble(arg1);
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
        Doubledrop doubledrop = generators.getDoubledrop();
        doubledrop.setMultiplayer(multiplayer);

        // Success message
        Lang.Message.COMMAND_DOUBLEDROP_SET_MULTIPLAYER_SUCCESS.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return DoubledropCommand.getCompletionsForTimeArgument(args);
    }
}
