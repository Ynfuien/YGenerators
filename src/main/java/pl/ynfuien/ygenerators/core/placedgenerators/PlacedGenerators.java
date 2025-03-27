package pl.ynfuien.ygenerators.core.placedgenerators;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.storage.Database;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlacedGenerators {
    private final YGenerators instance;
    private Database database;

    private final HashMap<Location, PlacedGenerator> placedGenerators = new HashMap<>();
    private final Set<Location> modifiedGenerators = new HashSet<>();

    private ScheduledTask saveTask = null;

    public PlacedGenerators(YGenerators instance) {
        this.instance = instance;
    }

    /**
     * Internal method for loading this class.
     */
    public boolean load(Database database) {
        logInfo("Loading generators from the database...");
        this.database = database;

        HashMap<Location, PlacedGenerator> generators = database.getGenerators();
        if (generators == null) {
            logError("Generators couldn't be loaded from the database! Plugin will be disabled.");
            return false;
        }

        placedGenerators.clear();
        placedGenerators.putAll(generators);
        startUpdateInterval();

        logInfo(String.format("Successfully loaded generators from the database! You have %d placed generator(s) on the server!", placedGenerators.size()));
        return true;
    }

    private void logInfo(String message) {
        YLogger.info("[Generators-Database] " + message);
    }

    private void logError(String message) {
        YLogger.error("[Generators-Database] " + message);
    }

    /**
     * Starts database update interval.
     */
    public void startUpdateInterval() {
        if (saveTask != null) saveTask.cancel();

        int updateInterval = database.getUpdateInterval();
        saveTask = Bukkit.getAsyncScheduler().runAtFixedRate(
                instance,
                (task) -> save(),
                updateInterval, updateInterval,
                TimeUnit.SECONDS
        );
    }

    /**
     * Stops database update interval.
     */
    public void stopUpdateInterval() {
        if (saveTask == null) return;

        saveTask.cancel();
        saveTask = null;
    }

    /**
     * Saves modified generators to the database.
     */
    public boolean save() {
        if (modifiedGenerators.isEmpty()) return true;

        List<Location> toRemove = new ArrayList<>();
        List<PlacedGenerator> toUpdate = new ArrayList<>();

        synchronized (modifiedGenerators) {
            for (Location location : modifiedGenerators) {
                if (!placedGenerators.containsKey(location)) {
                    toRemove.add(location);
                    continue;
                }

                toUpdate.add(placedGenerators.get(location));
            }

            modifiedGenerators.clear();
        }

        boolean removeResult = database.removeGenerators(toRemove);
        boolean updateResult = database.updateGenerators(toUpdate);

        YLogger.debug(String.format("RemoveResult: %b (%d); UpdateResult: %b (%d);", removeResult, toRemove.size(), updateResult, toUpdate.size()));
        return true;
    }

    /**
     * @return Whether provided location has a placed generator
     */
    public boolean has(Location location) {
        return placedGenerators.containsKey(location);
    }

    /**
     * @return Placed generator in the provided location, or null if none found
     */
    public PlacedGenerator get(Location location) {
        modifiedGenerators.add(location);
        return placedGenerators.get(location);
    }

    /**
     * Adds a placed generator to the list.
     */
    public void add(PlacedGenerator placedGenerator) {
        Location location = placedGenerator.getLocation();
        placedGenerators.put(location, placedGenerator);

        modifiedGenerators.add(location);
    }

    /**
     * Removes placed generator in provided location from the list.
     */
    public PlacedGenerator remove(Location location) {
        modifiedGenerators.add(location);
        return placedGenerators.remove(location);
    }

    /**
     * @return The HashMap of all placed generators
     */
    public HashMap<Location, PlacedGenerator> getAll() {
        return placedGenerators;
    }

    /**
     * @return All locations of the placed generators
     */
    public Set<Location> getAllLocations() {
        return placedGenerators.keySet();
    }

    /**
     * @return All placed generators
     */
    public Collection<PlacedGenerator> getAllPlacedGenerators() {
        return placedGenerators.values();
    }
}
