package pl.ynfuien.ygenerators.core.placedgenerators;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
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

    public boolean load(Database database) {
        logInfo("Loading generators from the database...");
        this.database = database;

        HashMap<Location, PlacedGenerator> generators = database.getGenerators();
        if (generators == null) {
            logError("Generators couldn't be loaded from the database! Plugin will be disabled.");
            return false;
        }

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

    public void startUpdateInterval() {
        if (saveTask != null) saveTask.cancel();

        int updateInterval = database.getUpdateInterval();
        saveTask = Bukkit.getAsyncScheduler().runAtFixedRate(instance, (task) -> {
            save();
        }, updateInterval, updateInterval, TimeUnit.SECONDS);
    }

    public void stopUpdateInterval() {
        if (saveTask != null) saveTask.cancel();
    }

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

    public boolean has(Location location) {
        return placedGenerators.containsKey(location);
    }

    public PlacedGenerator get(Location location) {
        modifiedGenerators.add(location);
        return placedGenerators.get(location);
    }

    public void add(PlacedGenerator placedGenerator) {
        Location location = placedGenerator.getLocation();
        placedGenerators.put(location, placedGenerator);

        modifiedGenerators.add(location);
    }

    public PlacedGenerator remove(Location location) {
        modifiedGenerators.add(location);
        return placedGenerators.remove(location);
    }

    public HashMap<Location, PlacedGenerator> getAll() {
        return placedGenerators;
    }

    public Set<Location> getAllLocations() {
        return placedGenerators.keySet();
    }

    public Collection<PlacedGenerator> getAllPlacedGenerators() {
        return placedGenerators.values();
    }
}
