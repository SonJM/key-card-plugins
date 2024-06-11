package org.blisle.hxxniverskeycard.service;

import org.blisle.hxxniverskeycard.connection.SQLiteDatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class RoleService {
    private final SQLiteDatabaseManager databaseManager;

    public RoleService(SQLiteDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void addPlayer(UUID playerId, String playerName) {
        String insertMember = "INSERT INTO member (uuid, name, role_id) SELECT ?, ?, ? WHERE NOT EXISTS (" +
                "SELECT 1 FROM member " +
                "WHERE uuid = ?" +
                ");";
        try (Connection connection = databaseManager.connect();
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
}
