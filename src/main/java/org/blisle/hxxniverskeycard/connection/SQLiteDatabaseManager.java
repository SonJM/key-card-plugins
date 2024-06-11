package org.blisle.hxxniverskeycard.connection;

import org.blisle.hxxniverskeycard.Hxxnivers_key_card;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.io.File;
import java.sql.*;

public class SQLiteDatabaseManager {
    private Connection connection;
    private final Hxxnivers_key_card plugin;

    public SQLiteDatabaseManager(Hxxnivers_key_card plugin) {
        this.plugin = plugin;
    }

    public Connection connect() throws SQLException {
        File dataFolder = new File(plugin.getDataFolder(), "database");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        String url = "jdbc:sqlite:" + new File(dataFolder, "keycard.db").getAbsolutePath();
        connection = DriverManager.getConnection(url);
        return connection;
    }

    public void initialize() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createRoleTableQuery = "CREATE TABLE IF NOT EXISTS role (" +
                    "id INTEGER PRIMARY KEY NOT NULL, " +
                    "name TEXT" +
                    ");";
            statement.execute(createRoleTableQuery);

            String createMemberTableQuery = "CREATE TABLE IF NOT EXISTS member (" +
                    "uuid TEXT PRIMARY KEY NOT NULL, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "role_id INTEGER NOT NULL DEFAULT 1, " +
                    "FOREIGN KEY (role_id) REFERENCES Role (id) ON UPDATE NO ACTION ON DELETE NO ACTION" +
                    ");";
            statement.execute(createMemberTableQuery);

            String createKeycardTableQuery = "CREATE TABLE IF NOT EXISTS keycard (" +
                    "id INTEGER PRIMARY KEY NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "role_id INTEGER NOT NULL, " +
                    "FOREIGN KEY (role_id) REFERENCES Role (id) ON UPDATE NO ACTION ON DELETE NO ACTION" +
                    ");";
            statement.execute(createKeycardTableQuery);

            String createDoorsTableQuery = "CREATE TABLE IF NOT EXISTS doors (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "x DOUBLE NOT NULL, " +
                    "y DOUBLE NOT NULL, " +
                    "z DOUBLE NOT NULL, " +
                    "permission TEXT NOT NULL" +
                    ");";
            statement.execute(createDoorsTableQuery);

            insertDefaultRoles();
        }
    }

    private void insertDefaultRoles() throws SQLException {
        String[] defaultRoles = {"시민", "경찰", "시청직원", "회사원", "은행원", "국회의원", "군인", "훈련병", "사업가", "vip"};

        String insertRoleQuery = "INSERT INTO role (name) SELECT ? WHERE NOT EXISTS (" +
                "SELECT 1 FROM role " +
                "WHERE name = ?" +
                ");";
        try (PreparedStatement statement = connection.prepareStatement(insertRoleQuery)) {
            for (String role : defaultRoles) {
                statement.setString(1, role);
                statement.setString(2, role);
                statement.executeUpdate();
            }
        }
    }

    public void updateMemberRole(String playerName, String roleName) throws SQLException{
        String getPlayerUUIDQuery = "SELECT uuid FROM member WHERE name = ?";
        String getRoleIdQuery = "SELECT id FROM role WHERE name = ?";
        String updateRoleQuery = "UPDATE member SET role_id = ? WHERE uuid = ?";

        try (Connection connection = connect();
             PreparedStatement getPlayerUUIDStmt = connection.prepareStatement(getPlayerUUIDQuery);
             PreparedStatement getRoleIdStmt = connection.prepareStatement(getRoleIdQuery);
             PreparedStatement updateRoleStmt = connection.prepareStatement(updateRoleQuery)) {

            getPlayerUUIDStmt.setString(1, playerName);
            try (ResultSet rsUUID = getPlayerUUIDStmt.executeQuery()) {
                if (rsUUID.next()) {
                    String uuid = rsUUID.getString("uuid");

                    getRoleIdStmt.setString(1, roleName);
                    try (ResultSet rsRole = getRoleIdStmt.executeQuery()) {
                        if (rsRole.next()) {
                            int roleId = rsRole.getInt("id");

                            updateRoleStmt.setInt(1, roleId);
                            updateRoleStmt.setString(2, uuid);
                            updateRoleStmt.executeUpdate();
                        }
                    } catch (SQLException e){
                        throw new SQLException("등록되어 있지 않은 역할입니다.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new SQLException("데이터베이스 연동에 실패했습니다.");
        }
    }

    public void insertDoorData(Location placedLocation, Location aboveLocation, String permission){
        String insertDoorQuery = "INSERT INTO doors (name, x, y, z, permission) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(insertDoorQuery)) {
            statement.setString(1, placedLocation.getWorld().getName());
            statement.setDouble(2, placedLocation.getX());
            statement.setDouble(3, placedLocation.getY());
            statement.setDouble(4, placedLocation.getZ());
            statement.setString(5, permission);
            statement.executeUpdate();

            statement.setString(1, aboveLocation.getWorld().getName());
            statement.setDouble(2, aboveLocation.getX());
            statement.setDouble(3, aboveLocation.getY());
            statement.setDouble(4, aboveLocation.getZ());
            statement.setString(5, permission);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteDoorDate(Location placedLocation) {
        String deleteDoorQuery = "DELETE FROM doors WHERE name = ? AND x = ? AND z = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(deleteDoorQuery)) {
            statement.setString(1, placedLocation.getWorld().getName());
            statement.setDouble(2, placedLocation.getX());
            statement.setDouble(3, placedLocation.getZ());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
