package org.blisle.hxxniverskeycard.connection;

import org.blisle.hxxniverskeycard.HxxniversKeyCard;
import org.bukkit.Location;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private Connection connection;
    private final HxxniversKeyCard plugin;

    public DatabaseManager(HxxniversKeyCard plugin) {
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

    public void insertPlayer(UUID playerId, String playerName) {
        String insertMember = "INSERT INTO member (uuid, name, role_id) SELECT ?, ?, ? WHERE NOT EXISTS (" +
                "SELECT 1 FROM member " +
                "WHERE uuid = ?" +
                ");";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(insertMember)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, playerName);
            statement.setInt(3, 1);
            statement.setString(4, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    public boolean roleExists(String role) throws SQLException {
        String checkRole = "SELECT 1 FROM role WHERE name = ?;";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(checkRole)) {
            statement.setString(1, role);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public boolean keyCardExists(String name, String roleName) throws SQLException {
        String getRoleIdQuery = "SELECT id FROM role WHERE name = ?";
        String checkKeyCardQuery = "SELECT 1 FROM keycard WHERE name = ? AND role_id = ?";

        try (Connection connection = connect();
             PreparedStatement getRoleIdStmt = connection.prepareStatement(getRoleIdQuery);
             PreparedStatement checkKeyCardStmt = connection.prepareStatement(checkKeyCardQuery)) {

            getRoleIdStmt.setString(1, roleName);
            try (ResultSet rsRole = getRoleIdStmt.executeQuery()) {
                if (rsRole.next()) {
                    int roleId = rsRole.getInt("id");
                    checkKeyCardStmt.setString(1, name);
                    checkKeyCardStmt.setInt(2, roleId);
                    try (ResultSet rsKeyCard = checkKeyCardStmt.executeQuery()) {
                        return rsKeyCard.next();
                    }
                }
            }
        } catch (SQLException e) {
            throw new SQLException(e.getMessage(), e);
        }
        return false;
    }

    public void insertRole(String role) throws SQLException {
        String insertRole = "INSERT INTO role (name) SELECT ? WHERE NOT EXISTS (" +
                "SELECT 1 FROM role " +
                "WHERE name = ?" +
                ");";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(insertRole)) {
            statement.setString(1, role);
            statement.setString(2, role);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public void insertKeyCard(String name, String roleName) throws SQLException {
        String getRoleIdQuery = "SELECT id FROM role WHERE name = ?";
        String insertKeyCard = "INSERT INTO keycard (name, role_id) SELECT ?, ? WHERE NOT EXISTS (" +
                "SELECT 1 FROM keycard " +
                "WHERE name = ?" +
                "AND role_id = ?" +
                ");";
        try (Connection connection = connect();
             PreparedStatement insertKeyCardStmt = connection.prepareStatement(insertKeyCard);
             PreparedStatement getRoleIdStmt = connection.prepareStatement(getRoleIdQuery)) {

            getRoleIdStmt.setString(1, roleName);
            try (ResultSet rsRole = getRoleIdStmt.executeQuery()) {
                if (rsRole.next()) {
                    int roleId = rsRole.getInt("id");
                    insertKeyCardStmt.setString(1, name);
                    insertKeyCardStmt.setInt(2, roleId);
                    insertKeyCardStmt.setString(3, name);
                    insertKeyCardStmt.setInt(4, roleId);
                    insertKeyCardStmt.executeUpdate();
                } else throw new SQLException();
            } catch (SQLException e) {
                throw new SQLException("등록되어 있지 않은 역할입니다.");
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

    public void insertDoorData(Location placedLocation, Location aboveLocation, String permission) {
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

    public boolean hasPermission(String permission, String roleName) throws SQLException {
        String getRoleIdQuery = "SELECT id FROM role WHERE name = ?";
        String getDoorRolesQuery = "SELECT role_id FROM keycard WHERE name = ?";

        try (Connection connection = connect();
             PreparedStatement getRoleIdStmt = connection.prepareStatement(getRoleIdQuery);
             PreparedStatement getDoorRolesStmt = connection.prepareStatement(getDoorRolesQuery)) {

            getRoleIdStmt.setString(1, roleName);
            ResultSet roleRs = getRoleIdStmt.executeQuery();
            if (!roleRs.next()) {
                return false;
            }
            int roleId = roleRs.getInt("id");

            getDoorRolesStmt.setString(1, permission);
            ResultSet doorRolesRs = getDoorRolesStmt.executeQuery();
            while (doorRolesRs.next()) {
                if (doorRolesRs.getInt("role_id") == roleId) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }


    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
