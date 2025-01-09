package pl.ynfuien.ygenerators.commands.main.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReloadSubcommand implements Subcommand {
    @Override
    public String permission() {
        return "ygenerators.command."+name();
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public void run(CommandSender sender, String[] args, HashMap<String, Object> placeholders) {
        // Reload plugin
        boolean success = Util.reloadPlugin();

        // Check if reload was success
        if (success) {
            // Send success message to console if sender is player
            if (sender instanceof Player) {
                Lang.Message.COMMAND_RELOAD_SUCCESS.send(Bukkit.getConsoleSender(), placeholders);
            }
            // Send success message to sender
            Lang.Message.COMMAND_RELOAD_SUCCESS.send(sender, placeholders);
            return;
        }

        // Send fail message to console if sender is player
        if (sender instanceof Player) {
            Lang.Message.COMMAND_RELOAD_FAIL.send(Bukkit.getConsoleSender(), placeholders);
        }
        // Send fail message to sender
        Lang.Message.COMMAND_RELOAD_FAIL.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
