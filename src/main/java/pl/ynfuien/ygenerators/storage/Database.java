package pl.ynfuien.ygenerators.storage;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public abstract class Database {
    private final YGenerators instance;
    private final Generators generators;

    private ConfigurationSection config;
    protected HikariDataSource dbSource;
    protected String dbName;

    protected String generatorsTableName = "ygene_generators";
    protected String doubledropTableName = "ygene_doubledrop";

    protected int updateInterval = 60;

    public Database(YGenerators instance) {
        this.instance = instance;
        this.generators = instance.getGenerators();
    }

    public boolean setup(ConfigurationSection config) {
        setConfig(config);
        return setupSpecific(config);
    }

    protected abstract boolean setupSpecific(ConfigurationSection config);

    public void close() {
        if (dbSource != null) dbSource.close();
    }

    public Doubledrop.Values getDoubledrop() {
        String query = String.format("SELECT * FROM `%s`", doubledropTableName);

        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet result = stmt.executeQuery();
            result.next();

            return new Doubledrop.Values(result.getInt("time_left"), result.getFloat("multiplayer"));
        } catch (SQLException e) {
            logWarn(String.format("Couldn't retrieve data from table '%s'.", doubledropTableName));
            e.printStackTrace();
            return null;
        }
    }

    public boolean setDoubledrop(int timeLeft, float multiplayer) {
        String query = String.format("UPDATE `%s` SET time_left=?, multiplayer=?", doubledropTableName);

        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, timeLeft);
            stmt.setFloat(2, multiplayer);
            stmt.execute();

            return true;
        } catch (SQLException e) {
            logWarn(String.format("Couldn't save data to table '%s'.", doubledropTableName));
            e.printStackTrace();
            return false;
        }
    }

    public HashMap<Location, PlacedGenerator> getGenerators() {
        String query = String.format("SELECT name, durability, world, x, y, z FROM `%s`", generatorsTableName);

        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet result = stmt.executeQuery();


            HashMap<Location, PlacedGenerator> placedGenerators = new HashMap<>();
            while (result.next()) {
                String name = result.getString("name");
                if (!generators.has(name)) {
                    logWarn(String.format("[Generators] There is no generator with the name '%s' in the config! It won't be loaded.", name));
                    continue;
                }

                float durability = result.getFloat("durability");
                String worldName = result.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    logWarn(String.format("[Generators] There is no world with the name '%s'. Generator placed in this world won't be loaded.", worldName));
                    continue;
                }

                int x = result.getInt("x");
                int y = result.getInt("y");
                int z = result.getInt("z");
                Location location = new Location(world, x, y, z);

                PlacedGenerator placedGenerator = new PlacedGenerator(generators.get(name), location, durability);
                placedGenerators.put(location, placedGenerator);
            }

            return placedGenerators;
        } catch (SQLException e) {
            logError(String.format("Couldn't load generators from the database (table '%s')!", generatorsTableName));
            e.printStackTrace();
            return null;
        }
    }

    public boolean removeGenerators(List<Location> placedGenerators) {
        int removed = 0;

        for (Location location : placedGenerators) {
            String query = String.format("DELETE FROM `%s` WHERE world=? AND x=? AND y=? AND z=?", generatorsTableName);

            try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, location.getWorld().getName());
                stmt.setInt(2, location.getBlockX());
                stmt.setInt(3, location.getBlockY());
                stmt.setInt(4, location.getBlockZ());

                stmt.execute();
                removed++;
            } catch (SQLException e) {
                logError(String.format(
                        "Couldn't remove generator (%s, %s %s %s) from the table '%s')!",
                        location.getWorld().getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        generatorsTableName));
                e.printStackTrace();
            }
        }

        return removed == placedGenerators.size();
    }

    public boolean updateGenerators(List<PlacedGenerator> placedGenerators) {
        int updated = 0;

        for (PlacedGenerator generator : placedGenerators) {
            // Update existing generator
            String query = String.format("UPDATE `%s` SET name=?, durability=? WHERE world=? AND x=? AND y=? AND z=?", generatorsTableName);

            Location location = generator.getLocation();
            try (Connection conn = dbSource.getConnection(); PreparedStatement uStmt = conn.prepareStatement(query)) {
                uStmt.setString(1, generator.getGenerator().getName());
                uStmt.setFloat(2, (float) generator.getDurability());
                uStmt.setString(3, location.getWorld().getName());
                uStmt.setInt(4, location.getBlockX());
                uStmt.setInt(5, location.getBlockY());
                uStmt.setInt(6, location.getBlockZ());

                if (uStmt.executeUpdate() != 0) {
                    updated++;
                    continue;
                }

                // Or insert a new one, if it doesn't exist already
                query = String.format("INSERT INTO `%s`(name, durability, world, x, y, z) VALUES(?, ?, ?, ?, ?, ?)", generatorsTableName);
                PreparedStatement iStmt = conn.prepareStatement(query);

                iStmt.setString(1, generator.getGenerator().getName());
                iStmt.setFloat(2, (float) generator.getDurability());
                iStmt.setString(3, location.getWorld().getName());
                iStmt.setInt(4, location.getBlockX());
                iStmt.setInt(5, location.getBlockY());
                iStmt.setInt(6, location.getBlockZ());

                iStmt.execute();
                updated++;
            } catch (SQLException e) {
                logError(String.format(
                        "Couldn't save generator %s (%s, %s %s %s) to the table '%s')!",
                        generator.getGenerator().getName(),
                        location.getWorld().getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        generatorsTableName));
                e.printStackTrace();
            }
        }

        return updated == placedGenerators.size();
    }


    protected void logWarn(String message) {
        YLogger.warn("[Database] " + message);
    }

    protected void logError(String message) {
        YLogger.error("[Database] " + message);
    }

    public abstract boolean createTables();

    public void setConfig(ConfigurationSection config) {
        this.config = config;

        generatorsTableName = config.getString("generators-table");
        doubledropTableName = config.getString("doubledrop-table");
        updateInterval = config.getInt("update-interval");
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }
}
