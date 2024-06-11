package org.blisle.hxxniverskeycard.event;

import org.blisle.hxxniverskeycard.service.RoleService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final RoleService roleService;

    public PlayerJoinListener(RoleService roleService) {
        this.roleService = roleService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        roleService.addPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}
