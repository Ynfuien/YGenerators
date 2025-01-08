package pl.ynfuien.ygenerators.listeners;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.data.Generators;
import pl.ynfuien.ygenerators.data.InteractionOptions;
import pl.ynfuien.ygenerators.data.generator.Generator;
import pl.ynfuien.ygenerators.generators.Database;
import pl.ynfuien.ygenerators.generators.PlacedGenerator;
import pl.ynfuien.ygenerators.hooks.superiorskyblock2.SuperiorSkyblock2Hook;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.utils.Messages;
import pl.ynfuien.ygenerators.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerInteractListener implements Listener {
    // This event handles interaction with generator to pick up it

    private final YGenerators instance;
    private final Generators generators;
    private final Database database;
    public PlayerInteractListener(YGenerators instance) {
        this.instance = instance;
        generators = instance.getGenerators();
        database = instance.getDatabase();
    }

    public static List<UUID> pickupCooldown = new ArrayList<>();
    public static List<UUID> checkStatusCooldown = new ArrayList<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        // Get pick up interaction options
        InteractionOptions pickUpInteraction = generators.getPickUp();

        // Return if pick up is disabled
        if (pickUpInteraction == null) return;

        // If interaction isn't pick up interaction
        if (!pickUpInteraction.isInteractionCorrect(e)) {
            // Check status interaction
            checkStatusInteraction(e);
            return;
        }

        // Get player
        Player p = e.getPlayer();

        // Get clicked block
        Block b = e.getClickedBlock();

        // Get location
        Location location = b.getLocation();

        // Return if clicked block isn't generator
        if (!database.has(location)) return;

        // If SuperiorSkyblock2 is enabled
        if (SuperiorSkyblock2Hook.isEnabled()) {
            // Get island at block
            Island island = SuperiorSkyblockAPI.getIslandAt(location);
            // If there is an island at block
            if (island != null) {
                // Get pick up generators privilege
                IslandPrivilege privilege = IslandPrivilege.getByName("PICK_UP_GENERATORS");

                // Return if player doesn't have privilege
                if (!island.hasPermission(p, privilege)) {
                    return;
                }
            }
        }

        // Get UUID
        UUID uuid = p.getUniqueId();
        // Return if player has cooldown
        if (pickupCooldown.contains(uuid)) return;
        pickupCooldown.add(uuid);

        // Create task to remove player's uuid from cooldown list after 3 ticks
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            pickupCooldown.remove(uuid);
        }, 3);

        // Get placed generator
        PlacedGenerator generator = database.get(location);

        // Cancel event
        e.setCancelled(true);

        // Remove placed generator from database
        database.remove(location);
        // Destroy generator and give player it's item
        generator.destroy(p);
    }

    private void checkStatusInteraction(PlayerInteractEvent e) {
        // Get check status interaction options
        InteractionOptions checkStatusInteraction = generators.getCheckStatus();

        // If check status isn't enabled
        if (checkStatusInteraction == null) return;

        // If interaction isn't check status interaction
        if (!checkStatusInteraction.isInteractionCorrect(e)) return;

        // Get player
        Player p = e.getPlayer();

        // Get clicked block location
        Location location = e.getClickedBlock().getLocation();

        // Return if clicked block isn't generator
        if (!database.has(location)) return;

        // Get UUID
        UUID uuid = p.getUniqueId();
        // Return if player has cooldown
        if (checkStatusCooldown.contains(uuid)) return;
        checkStatusCooldown.add(uuid);

        // Create task to remove player's uuid from cooldown list after 5 ticks
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            checkStatusCooldown.remove(uuid);
        }, 5);

        // Get placed generator
        PlacedGenerator gene = database.get(location);
        // Get generator
        Generator generator = gene.getGenerator();

        // Create hashmap for placeholders in message
        HashMap<String, Object> placeholders = new HashMap<>();

        // Get durability
        double durability = gene.getDurability();

        // Add placeholders
        placeholders.put("name", generator.getName());
        placeholders.put("displayname", generator.getDisplayname());
        placeholders.put("cooldown", generator.getCooldown());
        placeholders.put("remaining-durability", Util.formatDouble(durability));
        placeholders.put("full-durability", Util.formatDouble(generator.getDurability()));

        // Get message
        Lang.Message message = Lang.Message.GENERATOR_INFO;
        // Get another message if generator is infinite
        if (durability == -1) message = Lang.Message.GENERATOR_INFO_INFINITE;

        // Send message
        Messages.sendActionBar(p, message.get(placeholders));

        // Cancel event
        e.setCancelled(true);
    }
}
