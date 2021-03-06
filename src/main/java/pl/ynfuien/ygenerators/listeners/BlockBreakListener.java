package pl.ynfuien.ygenerators.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.data.Generators;
import pl.ynfuien.ygenerators.data.generator.Generator;
import pl.ynfuien.ygenerators.generators.Database;
import pl.ynfuien.ygenerators.generators.PlacedGenerator;
import pl.ynfuien.ygenerators.managers.Lang;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.*;

public class BlockBreakListener implements Listener {
    // This listener handles breaking:
    // - generator
    // - generator's generated block
    // - iron, gold and copper ore if option `other-options.1-16-ores` in config.yml is enabled

    private final YGenerators instance;
    private final Generators generators;
    private final Database database;
    public BlockBreakListener(YGenerators instance) {
        this.instance = instance;
        generators = instance.getGenerators();
        database = instance.getDatabase();
    }

    // List for deny messages cooldown
    private List<UUID> denyMessageCooldown = new ArrayList<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        // Get block
        Block b = e.getBlock();
        // Get block's location
        Location location = b.getLocation();
        // Get player
        Player p = e.getPlayer();

        // Handle 1-16 ores
        handle116Ores(e);

        // If block is generator
        if (database.has(location)) {
            // Get placed generator
            PlacedGenerator generator = database.get(location);

            // Cancel event
            e.setCancelled(true);

            // If player's gamemode is creative
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                // Remove generator from database
                database.remove(location);
                // Destroy generator and give player it's item stack
                generator.destroy(p);
                return;
            }

            // Return if generator can't be broken
            if (!generator.getGenerator().canBeBroken()) {
                // Get player's uuid
                UUID uuid = p.getUniqueId();

                // Return if cooldown list has player's uuid
                if (denyMessageCooldown.contains(uuid)) return;
                // Add uuid to cooldown list
                denyMessageCooldown.add(uuid);
                // Remove uuid from cooldown list after cooldown
                Bukkit.getScheduler().runTaskLater(instance, () -> {
                    denyMessageCooldown.remove(uuid);
                }, 30);

                // Send deny message to player
                Lang.Message.GENERATOR_DENY_BREAK.send(p);
                return;
            }

            // If generator can be broken
            // Remove generator from database
            database.remove(location);
            // Destroy generator and give player it's item stack
            generator.destroy(p);
            return;
        }

        // Get location under block
        Location locUnder = b.getRelative(BlockFace.DOWN).getLocation();
        // Return if block under isn't generator
        if (!database.has(locUnder)) return;


        // Get placed generator
        PlacedGenerator placedGenerator = database.get(locUnder);
        // Get generator
        Generator generator = placedGenerator.getGenerator();


        // Create task for generating block
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            // Return if placed generator was destroyed
            if (!database.has(locUnder)) return;

            // Generate block
            placedGenerator.generateBlock();
        }, generator.getCooldown());

        // Return if durability is -1 (Infinite generator)
        if (placedGenerator.getDurability() == -1) return;

        // Set amount of durability to decrease
        double amount = 1d;
        if (generators.getDoubledrop().isActive()) amount = generator.getDoubledropDurabilityDecrease();
        placedGenerator.decreaseDurability(amount);

        // Create hashmap for placeholders in messages
        HashMap<String, Object> placeholders = new HashMap<>();

        // Get durability
        double durability = placedGenerator.getDurability();

        // If durability is 0
        if (durability == 0) {
            // Remove generator from database
            database.remove(locUnder);
            // Destroy placed generator
            placedGenerator.destroy();

            // Add name placeholder
            placeholders.put("name", generator.getDisplayname());
            // Send message
            Lang.Message.GENERATOR_ALERT_BROKEN.send(p, placeholders);
            return;
        }

        // Get alert durability list
        List<Double> alertDurability = generators.getAlertDurability();

        // If alert durability list contains placed generator durability
        if (alertDurability.contains(durability)) {
            // Get word type
            Lang.WordType wordType = Lang.getWordType(durability);

            // Set word by word type
            Lang.Message word = Lang.Message.GENERATOR_ALERT_DURABILITY_WORD_PLURAR;
            if (wordType.equals(Lang.WordType.PLURAR_2_4)) word = Lang.Message.GENERATOR_ALERT_DURABILITY_WORD_PLURAR_2_4;
            else if (wordType.equals(Lang.WordType.SINGULAR)) word = Lang.Message.GENERATOR_ALERT_DURABILITY_WORD_SINGULAR;

            // Add word placeholder
            placeholders.put("word", word.get());
            // Add durability-left placeholder
            placeholders.put("durability-left", Util.formatDouble(durability));
            // Send message
            Lang.Message.GENERATOR_ALERT_LOW_DURABILITY.send(p, placeholders);
        }
    }

    private void handle116Ores(BlockBreakEvent e) {
        // Return if 1-16 ores option is disabled
        if (!instance.getConfig().getBoolean("other-options.1-16-ores")) return;

        // Get player
        Player p = e.getPlayer();

        // Return if player's gamemode is creative
        if (p.getGameMode().equals(GameMode.CREATIVE)) return;

        // Get block
        Block b = e.getBlock();

        // Get item in hand
        ItemStack item = p.getItemInHand();

        // Return if drops collection is empty
        if (b.getDrops(item).isEmpty()) return;
        // Get material
        Material type = b.getType();

        // If block is iron, gold or copper ire
        if (Arrays.asList(
                Material.IRON_ORE,
                Material.DEEPSLATE_IRON_ORE,
                Material.GOLD_ORE,
                Material.DEEPSLATE_GOLD_ORE,
                Material.COPPER_ORE,
                Material.DEEPSLATE_COPPER_ORE
        ).contains(type)) {
            // Set block to air
            b.setType(Material.AIR);
            // Get location
            Location loc = b.getLocation();
            // And drop block on ground
            loc.getWorld().dropItemNaturally(loc, new ItemStack(type));

            // Reduce item durability
            Util.reduceItemDurability(item);
        }
    }
}
