package pl.ynfuien.ygenerators.commands.main.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.ynfuien.ydevlib.utils.DoubleFormatter;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.commands.Subcommand;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.core.generator.GeneratorItem;
import pl.ynfuien.ygenerators.utils.Items;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GiveSubcommand implements Subcommand {
    private final YGenerators instance;
    private final Generators generators;

    private final List<UUID> confirmationCooldown = new ArrayList<>();
    private final static DoubleFormatter df = DoubleFormatter.DEFAULT;

    public GiveSubcommand(YGenerators instance) {
        this.instance = instance;
        this.generators = instance.getGenerators();
    }

    @Override
    public String permission() {
        return "ygenerators.command."+name();
    }

    @Override
    public String name() {
        return "give";
    }

    @Override
    public void run(CommandSender sender, String[] args, HashMap<String, Object> placeholders) {
        if (args.length == 0) {
            Lang.Message.COMMAND_GIVE_FAIL_NO_GENERATOR.send(sender, placeholders);
            return;
        }

        boolean isSenderPlayer = sender instanceof Player;
        String geneName = args[0].toLowerCase();
        placeholders.put("name", geneName);

        if (!generators.has(geneName)) {
            Lang.Message.COMMAND_GIVE_FAIL_GENERATOR_DOESNT_EXIST.send(sender, placeholders);
            return;
        }

        Generator generator = generators.get(geneName);
        GeneratorItem generatorItem = generator.getItem();

        //// give <generator>
        //// Give one generator to the player or return if sender isn't a player
        if (args.length == 1) {
            if (!isSenderPlayer) {
                Lang.Message.COMMAND_GIVE_FAIL_NO_PLAYER.send(sender, placeholders);
                return;
            }

            Player p = (Player) sender;
            ItemStack item = generatorItem.getItemStack(p);
            Items.giveItems(p, item);

            Lang.Message.COMMAND_GIVE_SUCCESS_SELF.send(sender, placeholders);
            return;
        }


        //// give <generator> <player>
        //// Give generator to another player
        String playerName = args[1];
        Player p = Bukkit.getPlayer(playerName);

        if (p == null || !p.isOnline()) {
            Lang.Message.COMMAND_GIVE_FAIL_PLAYER_ISNT_ONLINE.send(sender, placeholders);
            return;
        }

        placeholders.put("player", p.getName());

        if (args.length == 2) {
            ItemStack item = generatorItem.getItemStack(p);
            Items.giveItems(p, item);

            Lang.Message.COMMAND_GIVE_SUCCESS_NOSELF.send(sender, placeholders);
            return;
        }

        //// give <generator> <player> <amount>
        //// Give specified amount of generators to another player
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            Lang.Message.COMMAND_GIVE_FAIL_INCORRECT_AMOUNT.send(sender, placeholders);
            return;
        }

        if (amount < 1) {
            Lang.Message.COMMAND_GIVE_FAIL_INCORRECT_AMOUNT.send(sender, placeholders);
            return;
        }

        placeholders.put("amount", amount);

        // Require a confirmation command if:
        // - provided amount is higher than 128
        // - generator item can't be stackable
        // - sender is player
        if (amount > 128 && !generatorItem.stackable() && isSenderPlayer) {
            UUID uuid = ((Player) sender).getUniqueId();
            if (!confirmationCooldown.contains(uuid)) {
                confirmationCooldown.add(uuid);

                // Remove uuid after 8 seconds
                Bukkit.getAsyncScheduler().runDelayed(instance, (task) -> {
                    synchronized (confirmationCooldown) {
                        confirmationCooldown.remove(uuid);
                    }
                }, 8, TimeUnit.SECONDS);

                // Send deny message about accepting command
                Lang.Message.COMMAND_GIVE_ACCEPT_HIGH_AMOUNT.send(sender, placeholders);
                return;
            }
        }


        if (args.length == 3) {
            ItemStack[] items = generatorItem.getItemStacks(p, amount);
            Items.giveItems(p, items);

            Lang.Message.COMMAND_GIVE_SUCCESS_NOSELF_MANY.send(sender, placeholders);
            return;
        }


        //// give <generator> <player> <amount> <durability>
        //// Give specified amount of generators to another player with specified durability
        double durability;
        try {
            durability = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            Lang.Message.COMMAND_GIVE_FAIL_INCORRECT_DURABILITY.send(sender, placeholders);
            return;
        }

        placeholders.put("durability", df.format(durability));

        ItemStack[] items = generatorItem.getItemStacks(p, amount, durability);
        Items.giveItems(p, items);

        Lang.Message.COMMAND_GIVE_SUCCESS_NOSELF_DURABILITY.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length > 4) return completions;

        String arg1 = args[0].toLowerCase();

        // Generator names
        if (args.length == 1) {
            for (Generator gene : generators.getAll().values()) {
                String name = gene.getName();
                if (name.startsWith(arg1)) completions.add(name);
            }

            return completions;
        }

        String arg2 = args[1].toLowerCase();

        // Players
        if (args.length == 2) {
            Player player = sender instanceof Player p ? p : null;

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (player != null && !player.canSee(p)) continue;

                String name = p.getName();
                if (name.toLowerCase().startsWith(arg2)) completions.add(name);
            }

            return completions;
        }

        String arg3 = args[2].toLowerCase();

        // Item amount
        if (args.length == 3) {
            // Example values
            for (String number : Arrays.asList("1", "2", "4", "8", "16", "32", "64")) {
                if (number.startsWith(arg3)) completions.add(number);
            }

            return completions;
        }

        String arg4 = args[3].toLowerCase();

        // Generator durability
        if ("-1".startsWith(arg4)) completions.add("-1");

        Generator generator = generators.get(arg1);
        if (generator == null) return completions;

        String durability = String.valueOf(generator.getDurability());
        if (durability.startsWith(arg4)) completions.add(durability);

        return completions;
    }
}
