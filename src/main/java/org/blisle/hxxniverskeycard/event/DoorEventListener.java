package org.blisle.hxxniverskeycard.event;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.blisle.hxxniverskeycard.Hxxnivers_key_card;
import org.blisle.hxxniverskeycard.connection.SQLiteDatabaseManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Openable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class DoorEventListener implements Listener {
    private final Hxxnivers_key_card plugin;
    private final SQLiteDatabaseManager databaseManager;
    private long lastEventTime = 0;
    private static final long EVENT_COOLDOWN = 500;

    public DoorEventListener(Hxxnivers_key_card plugin, SQLiteDatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        if (itemInHand.getType() == Material.IRON_DOOR) {
            NBTItem nbtItem = new NBTItem(itemInHand);
            if (nbtItem.hasKey("Permission")) {
                String permission = nbtItem.getString("Permission");

                Block placedBlock = event.getBlock();
                placedBlock.setMetadata("Permission", new FixedMetadataValue(plugin, permission));

                Block aboveBlock = placedBlock.getRelative(BlockFace.UP);
                aboveBlock.setMetadata("Permission", new FixedMetadataValue(plugin, permission));

                Location placedLocation = event.getBlock().getLocation();
                Location aboveLocation = aboveBlock.getLocation();
                databaseManager.insertDoorData(placedLocation, aboveLocation, permission);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block placedBlock = event.getBlock();
        if (placedBlock.getType() == Material.IRON_DOOR) {
            Location placedLocation = placedBlock.getLocation();
            databaseManager.deleteDoorDate(placedLocation);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEventTime < EVENT_COOLDOWN) {
            return;
        }
        lastEventTime = currentTime;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.IRON_DOOR) {
                ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
                NBTItem nbtItem = new NBTItem(itemInHand);
                if (itemInHand.getType() != Material.IRON_DOOR && nbtItem.hasKey("Permission")) {
                    String permission = nbtItem.getString("Permission");
                    for (MetadataValue value : block.getMetadata("Permission")) {
                        if (value.asString().equals(permission)) {
                            BlockState state = block.getState();
                            if (state.getBlockData() instanceof Openable) {
                                Openable openable = (Openable) state.getBlockData();
                                openable.setOpen(!openable.isOpen());
                                state.setBlockData(openable);
                                state.update();
                                event.getPlayer().sendMessage("문이 열렸습니다!");
                                return;
                            }
                        }
                    }
                    event.getPlayer().sendMessage("키카드가 맞지 않습니다.");
                }
            }
        }
    }
}
