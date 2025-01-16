package pl.ynfuien.ygenerators.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import pl.ynfuien.yadmincore.YAdminCore;
import pl.ynfuien.ydevlib.messages.YLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqliteDatabase extends Database {
    @Override
    public boolean setup(ConfigurationSection config) {
        close();

        dbName = config.getString("path");

        HikariConfig dbConfig = new HikariConfig();
//        dbConfig.setDriverClassName("org.sqlite.JDBC");
        dbConfig.setJdbcUrl(String.format("jdbc:sqlite:%s/%s", YAdminCore.getInstance().getDataFolder().getPath(), config.getString("path")));



        try {
            dbSource = new HikariDataSource(dbConfig);
        } catch (Exception e) {
            YLogger.error("Plugin couldn't connect to a database! Please check connection data, because some plugin's functionality requires the database!");
            return false;
        }

        generatorsTableName = config.getString("table");
        return true;
    }

    @Override
    public boolean createTables() {
        String query = String.format("CREATE TABLE IF NOT EXISTS `%s` (uuid TEXT NOT NULL, godmode BOOLEAN DEFAULT false, last_location TEXT DEFAULT null, logout_location TEXT DEFAULT null, UNIQUE (uuid))", generatorsTableName);

        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.execute();
        } catch (SQLException e) {
            YLogger.error(String.format("Couldn't create table '%s' in database '%s'", generatorsTableName, dbName));
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
