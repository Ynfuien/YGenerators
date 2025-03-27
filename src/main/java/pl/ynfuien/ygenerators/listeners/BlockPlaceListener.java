package pl.ynfuien.ygenerators.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.api.event.GeneratorPlaceEvent;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.core.generator.GeneratorItem;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerator;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BlockPlaceListener implements Listener {
    // This listener handles:
    // - placing a generator
    // - placing a block above the generator

    private final YGenerators instance;
    private final Generators generators;
    private final PlacedGenerators placedGenerators;

    public BlockPlaceListener(YGenerators instance) {
        this.instance = instance;
        generators = instance.getGenerators();
        placedGenerators = instance.getPlacedGenerators();
    }

    private final List<UUID> denyMessageCooldown = new ArrayList<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Block under is a generator
        Location locUnder = block.getRelative(BlockFace.DOWN).getLocation();
        if (placedGenerators.has(locUnder)) {
            event.setCancelled(true);
            sendDenyMessage(player, Lang.Message.GENERATOR_DENY_PLACE_ABOVE);

            return;
        }

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String name = pdc.get(GeneratorItem.NSKey.GENERATOR, PersistentDataType.STRING);
        if (name == null) return;

        // Cancel event if generator with that name doesn't exist
        Generator generator = generators.get(name);
        if (generator == null) {
            event.setCancelled(true);

            sendDenyMessage(player, Lang.Message.GENERATOR_DENY_UNKNOWN_NAME);
            return;
        }

        Location location = block.getLocation();

        // Cancel event if generator is disabled in that location
        if (generators.isDisabledInLocation(name, location)) {
            event.setCancelled(true);

            sendDenyMessage(player, Lang.Message.GENERATOR_DENY_DISABLED_WORLD);
            return;
        }

        // Check for placing generators on top of each other
        if (!instance.getConfig().getBoolean("generators.place-on-top")) {
            // Block above is a generator
            Location locAbove = block.getRelative(BlockFace.UP).getLocation();
            if (placedGenerators.has(locAbove)) {
                event.setCancelled(true);
                sendDenyMessage(player, Lang.Message.GENERATOR_DENY_PLACE_UNDER);

                return;
            }
        }


        Chunk chunk = location.getChunk();

        HashMap<String, Object> placeholders = new HashMap<>();

        // If limit for all generators is enabled
        int maxInChunk = generators.getMaxInChunk();
        if (maxInChunk > -1) {
            // Count already placed generators in this chunk
            int generatorsInChunk = 0;
            for (Location loc : placedGenerators.getAllLocations()) {
                if (!loc.isChunkLoaded()) continue;

                if (loc.getChunk().equals(chunk)) generatorsInChunk++;
            }

            // Cancel event if limit is already reached
            if (generatorsInChunk >= maxInChunk) {
                event.setCancelled(true);

                placeholders.put("limit", maxInChunk);
                sendDenyMessage(player, Lang.Message.GENERATOR_DENY_LIMIT_ALL, placeholders);
                return;
            }
        }

        // If limit for this generator is enabled
        maxInChunk = generator.getMaxInChunk();
        if (maxInChunk > -1) {
            // Count already placed generators in this chunk
            int generatorsInChunk = 0;
            for (PlacedGenerator gene : placedGenerators.getAllPlacedGenerators()) {
                if (!gene.getGenerator().equals(generator)) continue;

                Location loc = gene.getLocation();
                if (!loc.isChunkLoaded()) continue;

                if (loc.getChunk().equals(chunk)) generatorsInChunk++;
            }

            // Cancel event if limit is already reached
            if (generatorsInChunk >= maxInChunk) {
                event.setCancelled(true);

                placeholders.put("limit", maxInChunk);
                sendDenyMessage(player, Lang.Message.GENERATOR_DENY_LIMIT_SINGLE, placeholders);
                return;
            }
        }

        // Cancel if player picked up generator a second ago
        UUID uuid = player.getUniqueId();
        if (PlayerInteractListener.pickupCooldown.contains(uuid)) {
            event.setCancelled(true);
            sendDenyMessage(player, Lang.Message.GENERATOR_DENY_COOLDOWN, placeholders);

            return;
        }

        // Get generator durability
        Double durability = pdc.get(GeneratorItem.NSKey.DURABILITY, PersistentDataType.DOUBLE);
        if (durability == null) {
            event.setCancelled(true);
            sendDenyMessage(player, Lang.Message.GENERATOR_DENY_DURABILITY_NOT_SET);

            return;
        }

        // API Event
        GeneratorPlaceEvent apiEvent = new GeneratorPlaceEvent(player, generator, block, item, durability);
        Bukkit.getPluginManager().callEvent(apiEvent);
        if (apiEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        PlacedGenerator placedGenerator = new PlacedGenerator(generator, location, durability);
        placedGenerators.add(placedGenerator);
    }

    private void sendDenyMessage(Player player, Lang.Message message) {
        sendDenyMessage(player, message, null);
    }

    private void sendDenyMessage(Player player, Lang.Message message, HashMap<String, Object> placeholders) {
        UUID uuid = player.getUniqueId();

        if (denyMessageCooldown.contains(uuid)) return;
        denyMessageCooldown.add(uuid);

        Bukkit.getScheduler().runTaskLater(instance, () -> {
            denyMessageCooldown.remove(uuid);
        }, 30);

        message.send(player, placeholders);
    }
}
