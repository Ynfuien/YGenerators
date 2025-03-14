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
import pl.ynfuien.ydevlib.utils.DoubleFormatter;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerator;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerators;

import java.util.*;

public class BlockBreakListener implements Listener {
    // This listener handles breaking:
    // - generator
    // - generator's generated block
    // - iron, gold and copper ore if option `other-options.1-16-ores` in config.yml is enabled

    private final YGenerators instance;
    private final Generators generators;
    private final Doubledrop doubledrop;

    private final PlacedGenerators placedGenerators;
    private final static DoubleFormatter df = DoubleFormatter.DEFAULT;

    public BlockBreakListener(YGenerators instance) {
        this.instance = instance;
        generators = instance.getGenerators();
        doubledrop = instance.getDoubledrop();
        placedGenerators = instance.getPlacedGenerators();
    }

    // List for deny messages cooldown
    private final List<UUID> denyMessageCooldown = new ArrayList<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        Player player = event.getPlayer();

        handle116Ores(event);

        // Broken block is a generator
        if (placedGenerators.has(location)) {
            PlacedGenerator generator = placedGenerators.get(location);

            event.setCancelled(true);

            // Destroy the generator if player is in creative
            if (player.getGameMode().equals(GameMode.CREATIVE)) {
                placedGenerators.remove(location);
                generator.destroy(player);
                return;
            }

            // Deny message if generator can't be broken by hand
            if (!generator.getGenerator().canBeBroken()) {
                UUID uuid = player.getUniqueId();

                if (denyMessageCooldown.contains(uuid)) return;

                denyMessageCooldown.add(uuid);
                Bukkit.getScheduler().runTaskLater(instance, () -> {
                    denyMessageCooldown.remove(uuid);
                }, 30);

                Lang.Message.GENERATOR_DENY_BREAK.send(player);
                return;
            }

            // Destroy the generator
            placedGenerators.remove(location);
            generator.destroy(player);
            return;
        }


        Location locUnder = block.getRelative(BlockFace.DOWN).getLocation();
        if (!placedGenerators.has(locUnder)) return;

        // Block above the generator was broken
        PlacedGenerator placedGenerator = placedGenerators.get(locUnder);
        Generator generator = placedGenerator.getGenerator();


        // Create task for generating block
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            if (!placedGenerators.has(locUnder)) return;

            placedGenerator.generateBlock();
        }, generator.getCooldown());


        // Reduce generator's durability
        if (placedGenerator.isInfinite()) return;

        double amount = 1d;
        if (doubledrop.isActive()) amount = generator.getDoubledropDurabilityDecrease();
        placedGenerator.decreaseDurability(amount);

        HashMap<String, Object> placeholders = new HashMap<>(generator.getDefaultPlaceholders());

        double durability = placedGenerator.getDurability();

        // Generator has been used up
        if (durability == 0) {
            placedGenerators.remove(locUnder);
            placedGenerator.destroy();

            Lang.Message.GENERATOR_ALERT_BROKEN.send(player, placeholders);
            return;
        }

        // Send durability alerts
        List<Double> alertDurability = generators.getAlertDurability();

        if (alertDurability.contains(durability)) {
            // Multi-language support stuff
            Lang.WordType wordType = Lang.getWordType(durability);
            Lang.Message word = Lang.Message.GENERATOR_ALERT_DURABILITY_WORD_PLURAL;
            if (wordType.equals(Lang.WordType.PLURAL_2_4)) word = Lang.Message.GENERATOR_ALERT_DURABILITY_WORD_PLURAL_2_4;
            else if (wordType.equals(Lang.WordType.SINGULAR)) word = Lang.Message.GENERATOR_ALERT_DURABILITY_WORD_SINGULAR;

            placeholders.put("word", word.get());
            placeholders.put("durability-left", df.format(durability));
            Lang.Message.GENERATOR_ALERT_LOW_DURABILITY.send(player, placeholders);
        }
    }

    private static final List<Material> ORES = Arrays.asList(
            Material.IRON_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.COPPER_ORE,
            Material.DEEPSLATE_COPPER_ORE
    );
    private void handle116Ores(BlockBreakEvent event) {
        if (!instance.getConfig().getBoolean("other-options.1-16-ores")) return;

        Player player = event.getPlayer();
        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        Block block = event.getBlock();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Return if drops collection is empty
        if (block.getDrops(item).isEmpty()) return;

        Material type = block.getType();

        if (ORES.contains(type)) {
            block.setType(Material.AIR);

            Location loc = block.getLocation();
            loc.getWorld().dropItemNaturally(loc, new ItemStack(type));

            item.damage(1, player);
        }
    }
}
