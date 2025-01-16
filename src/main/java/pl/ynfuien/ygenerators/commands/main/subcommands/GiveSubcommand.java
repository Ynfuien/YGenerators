package pl.ynfuien.ygenerators.commands.main.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.utils.Items;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.*;

public class    GiveSubcommand implements Subcommand {
    @Override
    public String permission() {
        return "ygenerators.command."+name();
    }

    @Override
    public String name() {
        return "give";
    }

    private List<UUID> acceptCooldown = new ArrayList<>();

    @Override
    public void run(CommandSender sender, String[] args, HashMap<String, Object> placeholders) {
        // Return if generator name isn't provided
        if (args.length == 0) {
            Lang.Message.COMMAND_GIVE_FAIL_NO_GENERATOR.send(sender, placeholders);
            return;
        }

        boolean isSenderPlayer = sender instanceof Player;

        // Get generator name from first arg
        String geneName = args[0].toLowerCase();
        // Get generators
        Generators generators = YGenerators.getInstance().getGenerators();

        // Return if generator with provided name doesn't exist
        if (!generators.has(geneName)) {
            Lang.Message.COMMAND_GIVE_FAIL_GENERATOR_DOESNT_EXIST.send(sender, placeholders);
            return;
        }

        // Get provided generator by name
        Generator gene = generators.get(geneName);

        // Add generator name placeholder
        placeholders.put("name", gene.getName());

        // If provided is only generator name
        if (args.length == 1) {
            // Return if sender isn't player
            if (!isSenderPlayer) {
                Lang.Message.COMMAND_GIVE_FAIL_NO_PLAYER.send(sender, placeholders);
                return;
            }

            // Set player to sender
            Player p = (Player) sender;

            // Get generator item's item stack
            ItemStack item = gene.getItem().getItemStack(p);

            // Give player generator
            Items.giveItems(p, item);

            // Send success message
            Lang.Message.COMMAND_GIVE_SUCCESS_SELF.send(sender, placeholders);
            return;
        }


        // If args length is >= 2

        // Get provided player's name
        String player = args[1];
        // Get provided player
        Player p = Bukkit.getPlayer(player);

        // Return if provided player isn't online
        if (p == null || !p.isOnline()) {
            Lang.Message.COMMAND_GIVE_FAIL_PLAYER_ISNT_ONLINE.send(sender, placeholders);
            return;
        }

        // Add player placeholder
        placeholders.put("player", p.getName());

        // If args length is 2
        if (args.length == 2) {
            // Get generator item's item stack
            ItemStack item = gene.getItem().getItemStack(p);

            // Give player generator
            Items.giveItems(p, item);

            // Send success message
            Lang.Message.COMMAND_GIVE_SUCCESS_NOSELF.send(sender, placeholders);
            return;
        }


        // If args length is >= 3

        // Get provided generator item amount and return if is incorrect
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            Lang.Message.COMMAND_GIVE_FAIL_INCORRECT_AMOUNT.send(sender, placeholders);
            return;
        }

        // Return if amount is lower than 1
        if (amount < 1) {
            Lang.Message.COMMAND_GIVE_FAIL_INCORRECT_AMOUNT.send(sender, placeholders);
            return;
        }

        // Add amount placeholder
        placeholders.put("amount", String.valueOf(amount));

        // If:
        // - provided amount is higher than 128
        // - generator item can't be stackable
        // - sender is player
        if (amount > 128 && !gene.getItem().stackable() && isSenderPlayer) {
            // Get player uuid
            UUID uuid = ((Player) sender).getUniqueId();
            // If accept cooldown list doesn't contain player's uuid
            if (!acceptCooldown.contains(uuid)) {
                // Add uuid to list
                acceptCooldown.add(uuid);

                // Create scheduler for removing uuid from list after 5 seconds
                Bukkit.getScheduler().runTaskLater(YGenerators.getInstance(), () -> {
                    acceptCooldown.remove(uuid);
                }, 5 * 20);

                // Send deny message about accepting command
                Lang.Message.COMMAND_GIVE_ACCEPT_HIGH_AMOUNT.send(sender, placeholders);
                return;
            }
        }

        // If args length is 3
        if (args.length == 3) {
            // Get generator item's item stack
            ItemStack[] items = gene.getItem().getItemStacks(p, amount);

            // Give player generator
            Items.giveItems(p, items);

            // Send success message
            Lang.Message.COMMAND_GIVE_SUCCESS_NOSELF_MANY.send(sender, placeholders);
            return;
        }

        // If args length is >= 4

        // Get provided generator item amount and return if is incorrect
        double durability;
        try {
            durability = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            Lang.Message.COMMAND_GIVE_FAIL_INCORRECT_DURABILITY.send(sender, placeholders);
            return;
        }

        // Add amount placeholder
        placeholders.put("durability", Util.formatDouble(durability));

        // Get generator item's item stack
        ItemStack[] items = gene.getItem().getItemStacks(p, amount, durability);

        // Give player generator
        Items.giveItems(p, items);

        // Send success message
        Lang.Message.COMMAND_GIVE_SUCCESS_NOSELF_DURABILITY.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        // Create compltions list
        List<String> completions = new ArrayList<>();

        // Return enpty array if args length is higher than 4
        if (args.length > 4) return completions;

        // Get first arg
        String arg1 = args[0].toLowerCase();

        // Get generators
        Generators generators = YGenerators.getInstance().getGenerators();

        // If args length is 1
        if (args.length == 1) {
            // Loop through generators
            for (Generator gene : generators.getAll().values()) {
                // Get generator name
                String name = gene.getName();
                // Add generator name to completions if it starts with provided arg
                if (name.startsWith(arg1)) {
                    completions.add(name);
                }
            }

            return completions;
        }

        // Get second arg
        String arg2 = args[1].toLowerCase();

        // If args length is 2
        if (args.length == 2) {
            // Loop through online players
            for (Player p : Bukkit.getOnlinePlayers()) {
                // Get player's name
                String name = p.getName();
                // If player's name starts with provided second arg
                if (name.toLowerCase().startsWith(arg2)) {
                    // If sender is player
                    if (sender instanceof Player) {
                        // Add player's name to completions if sender can see him
                        if (((Player) sender).canSee(p)) {
                            completions.add(name);
                        }

                        continue;
                    }

                    // Sender is console, so can see anyone
                    completions.add(name);
                }
            }

            return completions;
        }

        // Get third arg
        String arg3 = args[2].toLowerCase();

        // If args length is 3
        if (args.length == 3) {
            // Loop through online players
            for (String number : Arrays.asList("1", "2", "4", "8", "16", "32", "64")) {
                // If number starts with third arg
                if (number.startsWith(arg3)) {
                    // Add number to completions
                    completions.add(number);
                }
            }

            return completions;
        }

        // Get fourth arg
        String arg4 = args[3].toLowerCase();

        // Get generator provided in first arg
        Generator gene = generators.get(arg1);

        // Add -1 to completions
        if ("-1".startsWith(arg4)) completions.add("-1");

        // If generator with provided name doesn't exist
        if (gene == null) return completions;

        // Get generator durability
        String durability = String.valueOf(gene.getDurability());

        // Add generator name to completions
        if (durability.startsWith(arg4)) completions.add(durability);

        return completions;
    }
}
