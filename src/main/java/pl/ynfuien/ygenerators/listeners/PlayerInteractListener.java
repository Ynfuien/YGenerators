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
import pl.ynfuien.ydevlib.utils.DoubleFormatter;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.InteractionOptions;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerator;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerators;
import pl.ynfuien.ygenerators.hooks.superiorskyblock2.SuperiorSkyblock2Hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerInteractListener implements Listener {
    // This event handles interaction with generator to:
    // - pick it up
    // - check status

    private final YGenerators instance;
    private final Generators generators;
    private final PlacedGenerators placedGenerators;

    private static final DoubleFormatter df = DoubleFormatter.DEFAULT;
    public PlayerInteractListener(YGenerators instance) {
        this.instance = instance;
        generators = instance.getGenerators();
        placedGenerators = instance.getPlacedGenerators();
    }

    public static List<UUID> pickupCooldown = new ArrayList<>();
    public static List<UUID> checkStatusCooldown = new ArrayList<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        InteractionOptions pickUpInteraction = generators.getPickUp();

        // If interaction isn't a pick up interaction
        if (!pickUpInteraction.isInteractionCorrect(event)) {
            checkStatusInteraction(event);
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Location location = block.getLocation();

        if (!placedGenerators.has(location)) return;

        // If SuperiorSkyblock2 is enabled
        if (SuperiorSkyblock2Hook.isEnabled()) {
            Island island = SuperiorSkyblockAPI.getIslandAt(location);

            if (island != null) {
                IslandPrivilege privilege = IslandPrivilege.getByName("PICK_UP_GENERATORS");
                if (!island.hasPermission(player, privilege)) return;
            }
        }

        // Check for a cooldown
        UUID uuid = player.getUniqueId();
        if (pickupCooldown.contains(uuid)) return;
        pickupCooldown.add(uuid);

        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            pickupCooldown.remove(uuid);
        }, 5);


        PlacedGenerator placedGenerator = placedGenerators.get(location);

        placedGenerators.remove(location);
        placedGenerator.destroy(player);

        event.setCancelled(true);
    }

    private void checkStatusInteraction(PlayerInteractEvent event) {
        InteractionOptions checkStatusInteraction = generators.getCheckStatus();
        if (!checkStatusInteraction.isInteractionCorrect(event)) return;

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();

        if (!placedGenerators.has(location)) return;

        // Check for a cooldown
        UUID uuid = player.getUniqueId();
        if (checkStatusCooldown.contains(uuid)) return;
        checkStatusCooldown.add(uuid);

        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            checkStatusCooldown.remove(uuid);
        }, 5);

        // Send status action bar
        PlacedGenerator placedGenerator = placedGenerators.get(location);
        Generator generator = placedGenerator.getGenerator();

        double durability = placedGenerator.getDurability();

        HashMap<String, Object> placeholders = new HashMap<>(generator.getPlaceholders(durability));

        Lang.Message message = Lang.Message.GENERATOR_INFO;
        if (durability == -1) message = Lang.Message.GENERATOR_INFO_INFINITE;

        player.sendActionBar(message.getComponent(player, placeholders));
        event.setCancelled(true);
    }
}
