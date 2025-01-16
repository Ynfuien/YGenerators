package pl.ynfuien.ygenerators.storage;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

public abstract class Database {
    protected HikariDataSource dbSource;
    protected String dbName;
    protected String generatorsTableName = "ygene_generators";
    protected String doubledropTableName = "ygene_doubledrop";


    public abstract boolean setup(ConfigurationSection config);

    public void close() {
        if (dbSource != null) dbSource.close();
    }

//    public boolean userExists(UUID uuid) {
//        String query = String.format("SELECT godmode FROM `%s` WHERE uuid=? LIMIT 1", generatorsTableName);
//
//        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
//            stmt.setString(1, uuid.toString());
//            ResultSet resultSet = stmt.executeQuery();
//
//            return resultSet.next();
//        } catch (SQLException e) {
//            YLogger.warn(String.format("Couldn't retrieve data from table '%s'.", generatorsTableName));
//            e.printStackTrace();
//            return false;
//        }
//    }

//    public User getUser(UUID uuid) {
//        String query = String.format("SELECT godmode, last_location, logout_location FROM `%s` WHERE uuid=? LIMIT 1", generatorsTableName);
//
//        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
//            stmt.setString(1, uuid.toString());
//            ResultSet resultSet = stmt.executeQuery();
//
//            if (resultSet.next()) return new User(resultSet.getBoolean("godmode"), resultSet.getString("last_location"), resultSet.getString("logout_location"));
//            return null;
//        } catch (SQLException e) {
//            YLogger.warn(String.format("Couldn't retrieve data from table '%s'.", generatorsTableName));
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public boolean setUser(UUID uuid, User user) {
//        String query = String.format("UPDATE `%s` SET godmode=?, last_location=?, logout_location=? WHERE uuid=?", generatorsTableName);
//
//        if (!userExists(uuid)) {
//            query = String.format("INSERT INTO `%s`(godmode, last_location, logout_location, uuid) VALUES(?, ?, ?, ?)", generatorsTableName);
//        }
//
//        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
//            stmt.setBoolean(1, user.isGodModeEnabled());
//            String lastLocation = null;
//            if (user.getLastLocation() != null) lastLocation = JSONObject.toJSONString(user.getLastLocation().serialize());
//            stmt.setString(2, lastLocation);
//            String logoutLocation = null;
//            if (user.getLastLocation() != null) logoutLocation = JSONObject.toJSONString(user.getLogoutLocation().serialize());
//            stmt.setString(3, logoutLocation);
//            stmt.setString(4, uuid.toString());
//            stmt.execute();
//
//        } catch (SQLException e) {
//            YLogger.warn(String.format("Couldn't save data to table '%s'.", generatorsTableName));
//            e.printStackTrace();
//            return false;
//        }
//
//        return true;
//    }

    public abstract boolean createTables();

    public boolean isSetup() {
        return dbSource != null;
    }
}
