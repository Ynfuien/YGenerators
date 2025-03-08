package pl.ynfuien.ygenerators.commands.doubledrop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ydevlib.utils.DoubleFormatter;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.commands.doubledrop.subcommands.AddSubcommand;
import pl.ynfuien.ygenerators.commands.doubledrop.subcommands.RemoveSubcommand;
import pl.ynfuien.ygenerators.commands.doubledrop.subcommands.SetMultiplayerSubcommand;
import pl.ynfuien.ygenerators.commands.doubledrop.subcommands.SetSubcommand;
import pl.ynfuien.ygenerators.commands.main.MainCommand;
import pl.ynfuien.ygenerators.core.Doubledrop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DoubledropCommand implements CommandExecutor, TabCompleter {
    private final YGenerators instance;
    private final Doubledrop doubledrop;

    private final Subcommand[] subcommands;
    private final static DoubleFormatter df = DoubleFormatter.DEFAULT;

    public DoubledropCommand(YGenerators instance) {
        this.instance = instance;
        this.doubledrop = instance.getDoubledrop();

        this.subcommands = new Subcommand[] {
                new AddSubcommand(doubledrop),
                new SetSubcommand(doubledrop),
                new RemoveSubcommand(doubledrop),
                new SetMultiplayerSubcommand(doubledrop)
        };
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        HashMap<String, Object> placeholders = new HashMap<>() {{put("command", label);}};

        if (args.length == 0) {
            sendTimeLeft(sender, placeholders);
            return true;
        }


        // Loop through and check every subcommand
        boolean hasPermissionForAnyCommand = false;
        String arg1 = args[0].toLowerCase();
        for (Subcommand subcommand : subcommands) {
            if (!sender.hasPermission(subcommand.permission())) continue;
            hasPermissionForAnyCommand = true;
            if (!subcommand.name().equals(arg1)) continue;

            String[] argsLeft = Arrays.copyOfRange(args, 1, args.length);
            subcommand.run(sender, argsLeft, placeholders);
            return true;
        }

        if (hasPermissionForAnyCommand) {
            Lang.Message.COMMAND_DOUBLEDROP_USAGE.send(sender, placeholders);
            return true;
        }

        sendTimeLeft(sender, placeholders);
        return true;
    }

    private void sendTimeLeft(CommandSender sender, HashMap<String, Object> placeholders) {
        double multiplayer = doubledrop.getMultiplayer();
        placeholders.put("multiplayer", df.format(multiplayer));

        // Inactive double drop
        if (!doubledrop.isActive()) {
            Lang.Message.COMMAND_DOUBLEDROP_TIME_INACTIVE.send(sender, placeholders);
            return;
        }

        // Active
        placeholders.put("time-left", doubledrop.getFormattedTimeLeft());
        if (multiplayer == 2) {
            Lang.Message.COMMAND_DOUBLEDROP_TIME_ACTIVE.send(sender, placeholders);
            return;
        }

        // Active with a different multiplayer
        Lang.Message.COMMAND_DOUBLEDROP_TIME_ACTIVE_MULTIPLAYER.send(sender, placeholders);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return MainCommand.tabCompleteSubcommands(sender, subcommands, args);
    }

    public static List<String> getCompletionsForTimeArgument(String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length != 1) return completions;

        String arg1 = args[0].toLowerCase();
        if (arg1.isEmpty()) {
            // Add completions
            completions.addAll(Arrays.asList("1h", "2h", "3h", "30m", "1d", "2d", "-1"));

            return completions;
        }

        if ("-1".startsWith(arg1)) {
            completions.add("-1");
            return completions;
        }


        try {
            Integer.valueOf(arg1);

            completions.add(arg1 + "m");
            completions.add(arg1 + "h");
            completions.add(arg1 + "d");
        } catch (NumberFormatException ignored) {}

        return completions;
    }
}
