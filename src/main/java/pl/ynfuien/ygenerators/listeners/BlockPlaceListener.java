package pl.ynfuien.ygenerators.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.generators.GeneratorsDatabase;
import pl.ynfuien.ygenerators.generators.PlacedGenerator;
import pl.ynfuien.ygenerators.utils.NBTTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BlockPlaceListener implements Listener {
    // This listener handles placing generators

    private final YGenerators instance;
    private final Generators generators;
    private final GeneratorsDatabase generatorsDatabase;
    public BlockPlaceListener(YGenerators instance) {
        this.instance = instance;
        generators = instance.getGenerators();
        generatorsDatabase = instance.getDatabase();
    }

    // List for deny messages cooldown
    private List<UUID> denyMessageCooldown = new ArrayList<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        // Get item
        ItemStack item = e.getItemInHand();

        // Get generator name from item
        String geneName = (String) NBTTags.get(item, PersistentDataType.STRING, "generator");

        // Return if item isn't generator
        if (geneName == null) return;

        // Get player
        Player p = e.getPlayer();

        // Cancel event if generator with that name doesn't exist
        if (!generators.has(geneName)) {
            // Cancel event
            e.setCancelled(true);

            // Send deny message
            sendDenyMessage(p, Lang.Message.GENERATOR_DENY_UNKNOWN_NAME);
            return;
        }

        // Get block
        Block b = e.getBlock();
        // Get location
        Location location = b.getLocation();

        // Cancel event if generator is disabled in block's location
        if (generators.isDisabledInLocation(geneName, location)) {
            // Cancel event
            e.setCancelled(true);

            // Send deny message
            sendDenyMessage(p, Lang.Message.GENERATOR_DENY_DISABLED_WORLD);
            return;
        }

        // If option generators.place-on-top in config is enabled
        if (!instance.getConfig().getBoolean("generators.place-on-top")) {
            // Cancel event if block above is generator
            Location locAbove = b.getRelative(BlockFace.UP).getLocation();
            if (generatorsDatabase.has(locAbove)) {
                e.setCancelled(true);

                sendDenyMessage(p, Lang.Message.GENERATOR_DENY_PLACE_UNDER);
                return;
            }

            // Cancel event if block under is generator
            Location locUnder = b.getRelative(BlockFace.DOWN).getLocation();
            if (generatorsDatabase.has(locUnder)) {
                e.setCancelled(true);

                sendDenyMessage(p, Lang.Message.GENERATOR_DENY_PLACE_ABOVE);
                return;
            }
        }

        // Get generator
        Generator generator = generators.get(geneName);


        // Get global limit for generators in chunk
        int globalMaxInChunk = generators.getMaxInChunk();

        // Get generator's  limit for it in chunk
        int geneMaxInChunk = generator.getMaxInChunk();

        // Get chunk key for chunk for location of placed block
        long chunkKey = location.getChunk().getChunkKey();

        HashMap<String, Object> placeholders = new HashMap<>();

        // If global limit is enabled
        if (globalMaxInChunk > -1) {
            int generatorsInChunk = 0;
            for (Location loc : generatorsDatabase.getAllLocations()) {
                // Skip value if chunk isn't loaded
                if (!loc.isChunkLoaded()) continue;

                // Increase value of generatorsInChunk if location's chunk's key is same as placed block's chunk key
                if (loc.getChunk().getChunkKey() == chunkKey) generatorsInChunk++;
            }

            // If count of generators in chunk is higher than limit
            if (generatorsInChunk >= globalMaxInChunk) {
                // Cancel event
                e.setCancelled(true);
                // Add limit placeholder
                placeholders.put("limit", globalMaxInChunk);
                // Send deny message
                sendDenyMessage(p, Lang.Message.GENERATOR_DENY_LIMIT_GLOBAL, placeholders);
                return;
            }
        }

        // If single generator limit is enabled
        if (geneMaxInChunk > -1) {
            int generatorsInChunk = 0;
            for (PlacedGenerator gene : generatorsDatabase.getAllPlacedGenerators()) {
                // Skip placed generator if it isn't this generator
                if (!gene.getGenerator().equals(generator)) continue;

                // Get location
                Location loc = gene.getLocation();

                // Skip value if chunk isn't loaded
                if (!loc.isChunkLoaded()) continue;

                // Increase value of generatorsInChunk if location's chunk's key is same as placed block's chunk key
                if (loc.getChunk().getChunkKey() == chunkKey) generatorsInChunk++;
            }

            // If count of generators in chunk is higher than limit
            if (generatorsInChunk >= geneMaxInChunk) {
                // Cancel event
                e.setCancelled(true);
                // Add limit placeholder
                placeholders.put("limit", geneMaxInChunk);
                // Send deny message
                sendDenyMessage(p, Lang.Message.GENERATOR_DENY_LIMIT_SINGLE, placeholders);
                return;
            }
        }

        // Get player's uuid
        UUID uuid = p.getUniqueId();
        // Return if player pick up generator a while ago
        if (PlayerInteractListener.pickupCooldown.contains(uuid)) {
            e.setCancelled(true);
            sendDenyMessage(p, Lang.Message.GENERATOR_DENY_COOLDOWN, placeholders);
            return;
        }

        // Get generator durability
        double durability = (double) NBTTags.get(item, PersistentDataType.DOUBLE, "durability");

        // Create placed generator
        PlacedGenerator placedGenerator = new PlacedGenerator(generator, durability, location);

        // Add placed generator to database
        generatorsDatabase.add(placedGenerator);

        // Set block above generator to default block
        b.getRelative(BlockFace.UP).setType(generator.getDefaultBlock());
    }

    private void sendDenyMessage(Player p, Lang.Message message) {
        sendDenyMessage(p, message, null);
    }

    private void sendDenyMessage(Player p, Lang.Message message, HashMap<String, Object> placeholders) {
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
        message.send(p, placeholders);
    }
}
