package pl.ynfuien.ygenerators.commands.main.subcommands;

import org.bukkit.command.CommandSender;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.Lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pl.ynfuien.ygenerators.commands.main.MainCommand.subcommands;

public class HelpSubcommand implements Subcommand {
    @Override
    public String permission() {
        return "ygenerators.command.main";
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return Lang.Message.COMMAND_HELP_DESCRIPTION.get();
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        // Get subcommands sender has permissions for
        List<Subcommand> canUse = Arrays.stream(subcommands).filter(cmd -> sender.hasPermission(cmd.permission())).collect(Collectors.toList());

        // Get messages prefix
        String prefix = Lang.Message.PREFIX.get();
        // Get help top message
        String helpTop = Lang.Message.HELP_TOP.get()
                .replace("{prefix}", prefix);
        // Send top message
        Messages.send(sender, helpTop);

        // If player can't use any command
        if (canUse.size() == 0) {
            // Send help no commands message
            Messages.send(sender, Lang.Message.HELP_NO_COMMANDS.get());
            return;
        }

        // Get help command template
        String template = Lang.Message.HELP_COMMAND_TEMPLATE.get();
        // Send help commands
        for (Subcommand cmd : canUse) {
            String name = cmd.name();
            String description = cmd.description();
            String usage = cmd.usage();

            String message = template.replace("{command}", "gene "+name + (usage != null ? " "+usage : ""));
            message = message.replace("{description}", description);
            Messages.send(sender, message);
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
