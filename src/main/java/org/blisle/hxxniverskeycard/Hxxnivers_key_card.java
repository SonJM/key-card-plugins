package org.blisle.hxxniverskeycard;

import org.blisle.hxxniverskeycard.commands.KeycardCommand;
import org.blisle.hxxniverskeycard.commands.TagCommand;
import org.blisle.hxxniverskeycard.connection.SQLiteDatabaseManager;
import org.blisle.hxxniverskeycard.event.DoorEventListener;
import org.blisle.hxxniverskeycard.event.PlayerJoinListener;
import org.blisle.hxxniverskeycard.event.TagListener;
import org.blisle.hxxniverskeycard.service.RoleService;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Hxxnivers_key_card extends JavaPlugin {
    private SQLiteDatabaseManager databaseManager;

    @Override
    public void onEnable() {
        getLogger().info("[키카드 플러그인] 키카드 역할 플러그인 시작");
        this.databaseManager = new SQLiteDatabaseManager(this);
        RoleService roleService;
        try {
            getLogger().info("[키카드 플러그인] 데이터베이스 연동 시작");
            databaseManager.connect();
            databaseManager.initialize();

            roleService = new RoleService(databaseManager);
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("[키카드 플러그인] 데이터베이스 연동 실패");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getCommand("키카드").setExecutor(new KeycardCommand(databaseManager));
        this.getCommand("태그").setExecutor(new TagCommand(databaseManager));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(roleService), this);
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

