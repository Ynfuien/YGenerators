package pl.ynfuien.ygenerators.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import pl.ynfuien.ydevlib.messages.YLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MysqlDatabase extends Database {
    @Override
    public boolean setup(ConfigurationSection config) {
        close();

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
            YLogger.error("Plugin couldn't connect to a database! Please check connection data, because some plugin's functionality requires the database!");
            return false;
        }

        generatorsTableName = config.getString("table");
        return true;
    }

    @Override
    public boolean createTables() {
        // Generators
        String query = String.format("CREATE TABLE `%s` (id INT NOT NULL AUTO_INCREMENT , name TEXT NOT NULL , durability FLOAT NOT NULL , world TEXT NOT NULL , x INT NOT NULL , y INT NOT NULL , z INT NOT NULL , PRIMARY KEY (id))", generatorsTableName);

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
