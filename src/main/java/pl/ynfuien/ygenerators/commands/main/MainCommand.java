package pl.ynfuien.ygenerators.commands.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.commands.main.subcommands.GiveSubcommand;
import pl.ynfuien.ygenerators.commands.main.subcommands.HelpSubcommand;
import pl.ynfuien.ygenerators.commands.main.subcommands.ReloadSubcommand;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {
    public static Subcommand[] subcommands = {
            new ReloadSubcommand(),
            new GiveSubcommand(),
            new HelpSubcommand()
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Return if plugin is reloading
        if (Util.isReloading()) {
            Lang.Message.PLUGIN_IS_RELOADING.send(sender);
            return true;
        }

        // Get subcommands player cas use
        Subcommand[] canuse = Arrays.stream(subcommands).filter(sub -> sender.hasPermission(sub.permission())).toArray(Subcommand[]::new);

        Subcommand subcommandProvided = null;
        String arg1;
        // Get provided subcommand
        if (args.length > 0) {
            arg1 = args[0].toLowerCase();

            for (Subcommand subcmd : canuse) {
                if (subcmd.name().equals(arg1)) {
                    subcommandProvided = subcmd;
                    break;
                }
            }
        }

        if (subcommandProvided == null) {
            // Run help subcommand
            Subcommand help = Arrays.stream(subcommands).filter(sub -> sub.name().equals("help")).findAny().orElse(null);
            help.run(sender, args);
            return true;
        }

        // Get args left
        String[] argsLeft = Arrays.copyOfRange(args, 1, args.length);
        // Run provided subcommand
        subcommandProvided.run(sender, argsLeft);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // Create new list for completions
        List<String> completions = new ArrayList<>();

        // Return empty array if plugin is reloading
        if (Util.isReloading()) {
            return completions;
        }

        // Return empty list if no args are provided
        if (args.length == 0) return completions;

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
