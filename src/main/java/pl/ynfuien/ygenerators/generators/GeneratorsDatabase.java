package pl.ynfuien.ygenerators.generators;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.storage.Database;

import java.util.*;

public class GeneratorsDatabase {
    private YGenerators instance;
    private final Database database;

    private HashMap<Location, PlacedGenerator> placedGenerators = new HashMap<>();

    private BukkitTask interval = null;

    public GeneratorsDatabase(YGenerators instance) {
        this.instance = instance;
        this.database = instance.getDatabase();
    }

    public boolean loadFromFile() {
        logInfo("Loading generators from database...");

        // Get generators database config
//        FileConfiguration config = configManager.getConfig("database.yml");

        // Get generators list
//        List<String> database = config.getStringList("generators");

        // Get generators
        Generators generators = YGenerators.getInstance().getGenerators();


//        for (String dbGenerator : database) {
//            // Skip dbGenerator if it doesn't match regex '.+\|[-\d\.]+\|.+\|[-\d]+\|[-\d]+\|[-\d]+'
//            // Example correct value: stone|2000|world|0|100|0
//            if (!dbGenerator.matches(".+\\|[-\\d\\.]+\\|.+\\|[-\\d\\.]+\\|[-\\d\\.]+\\|[-\\d\\.]+")) {
//                logError(String.format("Generator '%s' has incorrect format and won't be loaded!", dbGenerator));
//                continue;
//            }
//
//            // Split string at every '|'
//            String[] split = dbGenerator.split("\\|");
//
//            // Generator name
//            String dbName = split[0];
//            // Durability left
//            String dbDurability = split[1];
//            // World
//            String dbWorld = split[2];
//
//
//            // Skip dbGenerator if generator with that name doesn't exist
//            if (!generators.has(dbName)) {
//                logError(String.format("Generator with name '%s' doesn't exist! This generator won't be loaded. (%s)", dbName, dbGenerator));
//                continue;
//            }
//
//            // Get generator with that name
//            Generator generator = generators.get(dbName);
//
//
//            // Get durability from string
//            double durability;
//            try {
//                durability = Double.parseDouble(dbDurability);
//            } catch (NumberFormatException e) {
//                // Skip dbGenerator if durability is incorrect
//                logError(String.format("Durability '%s' in generator '%s' is incorrect! This generator won't be loaded.", dbDurability, dbGenerator));
//                continue;
//            }
//
//            // Set durability to -1 if it is lower than -1
//            if (durability < -1) durability = -1;
//
//
//            // Get world
//            World world = Bukkit.getWorld(dbWorld);
//
//            // Skip dbGenerator if world doesn't exist
//            if (world == null) {
//                logError(String.format("World '%s', which is in generator '%s', doesn't exist! This generator won't be loaded.", dbWorld, dbGenerator));
//                continue;
//            }
//
//            Location location = new Location(world, 0, 0, 0);
//
//            HashMap<Character, String> coordinates = new HashMap<>() {{
//                put('x', split[3]);
//                put('y', split[4]);
//                put('z', split[5]);
//            }};
//
//            boolean skip = false;
//            for (char coordinate : coordinates.keySet()) {
//                String stringValue = coordinates.get(coordinate);
//
//                // Get coordinate value from string
//                double value;
//                try {
//                    value = Double.parseDouble(stringValue);
//                } catch (NumberFormatException e) {
//                    // Skip dbGenerator if y coordinate is incorrect
//                    logError(String.format("%s coordinate '%s', which is in generator '%s', is incorrect! This generator won't be loaded.", String.valueOf(coordinate).toUpperCase(), stringValue, dbGenerator));
//                    skip = true;
//                    break;
//                }
//
//                // Set x coordinate
//                if (coordinate == 'x') {
//                    location.setX(value);
//                    continue;
//                }
//                // Set y coordinate
//                if (coordinate == 'y') {
//                    location.setY(value);
//                    continue;
//                }
//                // Set z coordinate
//                location.setZ(value);
//            }
//
//            if (skip) continue;
//
//            // Create placed generator
//            PlacedGenerator placedGenerator = new PlacedGenerator(generator, durability, location);
//            // Put placed generator in hashmap
//            placedGenerators.put(location, placedGenerator);
//        }

        logInfo(String.format("Successfully loaded generators from database! You have %d placed generator(s) on the server!", placedGenerators.size()));
        return true;
    }

    private void logInfo(String message) {
        YLogger.info("[Generators-Database] " + message);
    }

    private void logError(String message) {
        YLogger.warn("[Generators-Database] " + message);
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
