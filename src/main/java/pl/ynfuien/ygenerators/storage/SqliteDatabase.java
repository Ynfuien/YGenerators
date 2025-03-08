package pl.ynfuien.ygenerators.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import pl.ynfuien.ygenerators.YGenerators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteDatabase extends Database {
    public SqliteDatabase(YGenerators instance) {
        super(instance);
    }

    @Override
    public boolean setup(ConfigurationSection config) {
        close();

        dbName = config.getString("path");

        HikariConfig dbConfig = new HikariConfig();
//        dbConfig.setDriverClassName("org.sqlite.JDBC");
        dbConfig.setJdbcUrl(String.format("jdbc:sqlite:%s/%s", YGenerators.getInstance().getDataFolder().getPath(), config.getString("path")));


        try {
            dbSource = new HikariDataSource(dbConfig);
        } catch (Exception e) {
            logError("Plugin couldn't connect to a database! Check connection data, because plugin can't work without the database!");
            return false;
        }

        generatorsTableName = config.getString("generators-table");
        doubledropTableName = config.getString("doubledrop-table");

        updateInterval = config.getInt("update-interval");
        return true;
    }

    @Override
    public boolean createTables() {
        String tableName = generatorsTableName;

        try (Connection conn = dbSource.getConnection()) {
            // Generators table
            String query = String.format("CREATE TABLE IF NOT EXISTS `%s` (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, durability FLOAT NOT NULL, world TEXT NOT NULL, x INT NOT NULL, y INT NOT NULL, z INT NOT NULL)", tableName);

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.execute();

            // Doubledrop table
            tableName = doubledropTableName;
            query = String.format("CREATE TABLE IF NOT EXISTS `%s` (time_left INT NOT NULL DEFAULT 0, multiplayer FLOAT NOT NULL DEFAULT 2)", doubledropTableName);

            stmt = conn.prepareStatement(query);
            stmt.execute();
        } catch (SQLException e) {
            logError(String.format("Couldn't create table '%s' in database '%s'", tableName, dbName));
            e.printStackTrace();
            return false;
        }

        // Doubledrop data row
        String query = String.format("SELECT COUNT(*) as count FROM `%s`", tableName);
//        String query = String.format("SELECT * FROM `%s`", tableName);
        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet result = stmt.executeQuery();
            if (result.getInt("count") != 0) return true;
        } catch (SQLException e) {
            logError(String.format("Couldn't check whether '%s' table has any data!", tableName));
            e.printStackTrace();
            return false;
        }

        query = String.format("INSERT INTO `%s` DEFAULT VALUES", tableName);
        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.execute();
        } catch (SQLException e) {
            logError(String.format("Couldn't save default data to the '%s' table!", tableName));
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
