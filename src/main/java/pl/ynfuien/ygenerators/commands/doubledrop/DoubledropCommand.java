package pl.ynfuien.ygenerators.commands.doubledrop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.commands.doubledrop.subcommands.AddSubcommand;
import pl.ynfuien.ygenerators.commands.doubledrop.subcommands.RemoveSubcommand;
import pl.ynfuien.ygenerators.commands.doubledrop.subcommands.SetSubcommand;
import pl.ynfuien.ygenerators.commands.doubledrop.subcommands.SetmultiplayerSubcommand;
import pl.ynfuien.ygenerators.data.Doubledrop;
import pl.ynfuien.ygenerators.managers.Lang;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DoubledropCommand implements CommandExecutor, TabCompleter {
    public static Subcommand[] subcommands = {
            new AddSubcommand(),
            new SetSubcommand(),
            new RemoveSubcommand(),
            new SetmultiplayerSubcommand()
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Return if plugin is reloading
        if (Util.isReloading()) {
            Lang.Message.PLUGIN_IS_RELOADING.send(sender);
            return true;
        }

        // No args //
        if (args.length == 0) {
            sendTime(sender);
            return true;
        }

        // Get subcommands player cas use
        List<Subcommand> canuse = Arrays.asList(Arrays.stream(subcommands).filter(sub -> sender.hasPermission(sub.permission())).toArray(Subcommand[]::new));

        // If sender can't use any subcommand then send time left
        if (canuse.size() == 0) {
            sendTime(sender);
            return true;
        }

        // Get first arg
        String arg1 = args[0].toLowerCase();

        // Get provided subcommand
        Subcommand subcommandProvided = null;
        for (Subcommand subcmd : canuse) {
            if (subcmd.name().equals(arg1)) {
                subcommandProvided = subcmd;
                break;
            }
        }

        // If subcommand is null
        if (subcommandProvided == null) {
            Lang.Message.COMMAND_DOUBLEDROP_USAGE.send(sender);
            return true;
        }

        // Get args left
        String[] argsLeft = Arrays.copyOfRange(args, 1, args.length);
        // Run provided subcommand
        subcommandProvided.run(sender, argsLeft);
        return true;
    }

    private void sendTime(CommandSender sender) {
        // Get double drop
        Doubledrop doubledrop = YGenerators.getInstance().getGenerators().getDoubledrop();

        // Send dd inactive message if double drop isn't active
        if (!doubledrop.isActive()) {
            Lang.Message.COMMAND_DOUBLEDROP_TIME_INACTIVE.send(sender);
            return;
        }

        // Get time lft
        long timeLeft = doubledrop.getTimeLeft();
        // Get multiplayer
        double multiplayer = doubledrop.getMultiplayer();

        // Get active message
        Lang.Message message = Lang.Message.COMMAND_DOUBLEDROP_TIME_ACTIVE;

        // Create hashmap for placeholders in message
        HashMap<String, Object> placeholders = new HashMap<>();

        // Multiplayer is different than 2
        if (multiplayer != 2) {
            // Set message to active multiplayer message
            message = Lang.Message.COMMAND_DOUBLEDROP_TIME_ACTIVE_MULTIPLAYER;

            // Add placeholder for multiplayer
            placeholders.put("multiplayer", Util.formatDouble(multiplayer));
        }

        // Add placeholder for time
        placeholders.put("time", doubledrop.getFormattedTimeLeft());

        // Send message
        message.send(sender, placeholders);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        // Return empty array if plugin is reloading
        if (Util.isReloading()) {
            return completions;
        }

//        // Return empty list if no args are provided
//        if (args.length == 0) return completions;

        // Get commands the sender has permissions for
        List<Subcommand> canUse = Arrays.stream(subcommands).filter(cmd -> sender.hasPermission(cmd.permission())).collect(Collectors.toList());
        // Check if sender can't use any of command and return empty list
        if (canUse.size() == 0) return completions;

        // Get first arg
        String arg1 = args[0].toLowerCase();
        // If args length is 1
        if (args.length == 1) {
            // Loop commands the sender can use
            for (Subcommand cmd : canUse) {
                // Get command name
                String name = cmd.name();
                // Check if name starts with first arg
                if (name.startsWith(args[0])) {
                    // Add name to completions
                    completions.add(name);
                }
            }

            // Return completions
            return completions;
        }

        // If args length is higher than 1

        // Get provided command in first arg
        Subcommand subCommand = canUse.stream().filter(cmd -> cmd.name().equals(arg1)).findAny().orElse(null);
        // Return empty list if provided command isn't available
        if (subCommand == null) return completions;

        // Get completions from provided command
        List<String> subCommandTabs = subCommand.getTabCompletions(sender, Arrays.copyOfRange(args, 1, args.length));

        // Return completions if these aren't null
        if (subCommandTabs != null) {
            return subCommandTabs;
        }

        // Return empty list
        return completions;
    }
}
