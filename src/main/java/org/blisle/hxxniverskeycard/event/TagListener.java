package org.blisle.hxxniverskeycard.event;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.blisle.hxxniverskeycard.connection.SQLiteDatabaseManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;


public class TagListener implements Listener {
    private final SQLiteDatabaseManager databaseManager;
    private long lastEventTime = 0;
    private static final long EVENT_COOLDOWN = 500;

    public TagListener(SQLiteDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEntityEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEventTime < EVENT_COOLDOWN) {
            return;
        }
        lastEventTime = currentTime;

        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }

        Player player = event.getPlayer();
        Player target = (Player) event.getRightClicked();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() != Material.NETHER_STAR && itemInHand.getType() != Material.BLAZE_ROD) {
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        NBTItem nbtItem = new NBTItem(itemInHand);
        if (!nbtItem.hasKey("RoleRegister")) {
            return;
        }

        String role = nbtItem.getString("Role");
        if (role == null) {
            player.sendMessage("이 아이템에는 역할 정보가 없습니다.");
            return;
        }

        if(itemInHand.getType() == Material.NETHER_STAR && !role.isEmpty()){
            try {
                databaseManager.updateMemberRole(target.getName(), role);
                player.sendMessage(target.getName() + "의 역할이 " + role + "로 변경되었습니다.");
            } catch (SQLException e) {
                player.sendMessage("역할 변경에 실패했습니다: " + e.getMessage());
            }
        }
        if(itemInHand.getType() == Material.BLAZE_ROD){
            try {
                databaseManager.updateMemberRole(target.getName(), "시민");
                player.sendMessage(target.getName() + "의 역할이 시민으로 변경되었습니다.");
            } catch (SQLException e) {
                player.sendMessage("역할 변경에 실패했습니다: " + e.getMessage());
            }
        }

        event.setCancelled(true);
    }
}
