package org.blisle.hxxniverskeycard;

import org.blisle.hxxniverskeycard.commands.KeycardCommand;
import org.blisle.hxxniverskeycard.commands.TagCommand;
import org.blisle.hxxniverskeycard.connection.DatabaseManager;
import org.blisle.hxxniverskeycard.event.DoorEventListener;
import org.blisle.hxxniverskeycard.event.PlayerJoinListener;
import org.blisle.hxxniverskeycard.event.TagListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class HxxniversKeyCard extends JavaPlugin {
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        getLogger().info("[키카드 플러그인] 키카드 역할 플러그인 시작");
        this.databaseManager = new DatabaseManager(this);
        try {
            getLogger().info("[키카드 플러그인] 데이터베이스 연동 시작");
            databaseManager.connect();
            databaseManager.initialize();
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("[키카드 플러그인] 데이터베이스 연동 실패");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getCommand("키카드").setExecutor(new KeycardCommand(databaseManager));
        this.getCommand("태그").setExecutor(new TagCommand(databaseManager));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(databaseManager), this);
        getServer().getPluginManager().registerEvents(new TagListener(databaseManager), this);
        getServer().getPluginManager().registerEvents(new DoorEventListener(this, databaseManager), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("[후스텔라] 키카드 역할 플러그인 종료");
        try {
            databaseManager.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

