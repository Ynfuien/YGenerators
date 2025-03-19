package pl.ynfuien.ygenerators.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import pl.ynfuien.ygenerators.YGenerators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MysqlDatabase extends Database {
    public MysqlDatabase(YGenerators instance) {
        super(instance);
    }

    @Override
    protected boolean setupSpecific(ConfigurationSection config) {
        dbName = config.getString("name");

        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dbConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s", config.getString("host"), config.getString("port"), dbName));

        dbConfig.setUsername(config.getString("login"));
        dbConfig.setPassword(config.getString("password"));
        dbConfig.setMaximumPoolSize(config.getInt("max-connections"));


        try {
            dbSource = new HikariDataSource(dbConfig);
        } catch (Exception e) {
            logError("Plugin couldn't connect to a database! Check connection data, because plugin can't work without the database!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean createTables() {
        String tableName = generatorsTableName;

        try (Connection conn = dbSource.getConnection()) {
            // Generators table
            String query = String.format("CREATE TABLE IF NOT EXISTS `%s` (name TEXT NOT NULL, durability FLOAT NOT NULL, world TEXT NOT NULL, x INT NOT NULL, y INT NOT NULL, z INT NOT NULL)", tableName);

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
        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet result = stmt.executeQuery();
            result.next();
            if (result.getInt("count") != 0) return true;
        } catch (SQLException e) {
            logError(String.format("Couldn't check whether '%s' table has any data!", tableName));
            e.printStackTrace();
            return false;
        }

        query = String.format("INSERT INTO `%s`() VALUES ()", tableName);
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