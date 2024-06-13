package org.blisle.hxxniverskeycard.event;

import org.blisle.hxxniverskeycard.connection.DatabaseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final DatabaseManager databaseManager;

    public PlayerJoinListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        databaseManager.insertPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}
