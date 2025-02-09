package pl.ynfuien.ygenerators.core.placedgenerators;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.storage.Database;

import java.util.*;

public class PlacedGenerators {
    private final YGenerators instance;
    private final Database database;

    private final HashMap<Location, PlacedGenerator> placedGenerators = new HashMap<>();

    private BukkitTask interval = null;

    public PlacedGenerators(YGenerators instance) {
        this.instance = instance;
        this.database = instance.getDatabase();
    }

    public boolean load() {
        logInfo("Loading generators from the database...");

        HashMap<Location, PlacedGenerator> generators = database.getGenerators();
        if (generators == null) {
            logError("Generators couldn't be loaded from the database! Plugin will be disabled.");
            return false;
        }

        placedGenerators.putAll(generators);

        logInfo(String.format("Successfully loaded generators from the database! You have %d placed generator(s) on the server!", placedGenerators.size()));
        return true;
    }

    private void logInfo(String message) {
        YLogger.info("[Generators-Database] " + message);
    }

    private void logError(String message) {
        YLogger.error("[Generators-Database] " + message);
    }

    // Starts interval which updates database every x seconds
    public void startUpdateInterval(int period) {
        // Cancel current interval if it is running
        if (interval != null) interval.cancel();

        // Run new interval
        interval = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            saveToFile();
        }, period, period);
    }

    // Stops update interval
    public void stopUpdateInterval() {
        // Cancel interval if it is running
        if (interval != null) interval.cancel();
    }

    // Saves database to file
    public boolean saveToFile() {
        // File name
        String fileName = "database.yml";

//        // Get database config
//        FileConfiguration database = configManager.getConfig("database.yml");

        // Create list for placed generators
        List<String> generatorsList = new ArrayList<>();

        // Loop through placed generators
        for (PlacedGenerator gene : placedGenerators.values()) {
            // Get placed generator location
            Location loc = gene.getLocation();
            // Create args array
            String[] args = new String[] {
                    gene.getGenerator().getName(),
                    String.valueOf(gene.getDurability()),
                    loc.getWorld().getName(),
                    String.valueOf(loc.getX()),
                    String.valueOf(loc.getY()),
                    String.valueOf(loc.getZ())
            };
            // Join args into string
            String generator = String.join("|", args);

            // Add generator to list
            generatorsList.add(generator);
        }

//        // Set list in database config
//        database.set("generators", generatorsList);
//
//        // Save database to file
//        try {
//            database.save(new File(instance.getDataFolder(), fileName));
//        } catch (IOException e) {
//            YLogger.error("An error occurred while saving generators database to file! Error:");
//            e.printStackTrace();
//            return false;
//        }

        return true;
    }

    // Gets placed generator
    public PlacedGenerator get(Location location) {
        return placedGenerators.get(location);
    }

    // Gets all placed generators
    public HashMap<Location, PlacedGenerator> getAll() {
        return placedGenerators;
    }

    // Gets all placed generator locations
    public Set<Location> getAllLocations() {
        return placedGenerators.keySet();
    }

    // Gets all placed generators
    public Collection<PlacedGenerator> getAllPlacedGenerators() {
        return placedGenerators.values();
    }

    // Gets whether in location is placed generator
    public boolean has(Location location) {
        return placedGenerators.containsKey(location);
    }

    // Adds placed generator to hashmap
    public void add(PlacedGenerator placedGenerator) {
        placedGenerators.put(placedGenerator.getLocation(), placedGenerator);
    }

    // Removes placed generator from hashmap and returns it
    public PlacedGenerator remove(Location location) {
        return placedGenerators.remove(location);
    }
}
