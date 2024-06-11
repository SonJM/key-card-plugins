package org.blisle.hxxniverskeycard.commands;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import org.blisle.hxxniverskeycard.connection.SQLiteDatabaseManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TagCommand implements CommandExecutor {
    private final SQLiteDatabaseManager databaseManager;

    public TagCommand(SQLiteDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("플레이어만 사용 가능한 명령어 입니다.");
            return true;
        }

        if (strings.length == 0) {
            player.sendMessage("태그 관련 명령어: /태그 [생성|등록|등록취소|설정]");
            return true;
        }
        String subCommand = strings[0];
        switch (subCommand) {
            case "생성":
                if (strings.length != 2) {
                    player.sendMessage("명령어 사용법: /태그 생성 <역할>");
                    return true;
                }
                String role = strings[1];
                String insertRole = "INSERT INTO role (name) SELECT ? WHERE NOT EXISTS (" +
                        "SELECT 1 FROM role " +
                        "WHERE name = ?" +
                        ");";
                try (Connection connection = databaseManager.connect();
                     PreparedStatement statement = connection.prepareStatement(insertRole)) {
                    statement.setString(1, role);
                    statement.setString(2, role);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage("역할 " + role + " 생성됨.");
                break;

            case "등록":
                if (strings.length != 2) {
                    player.sendMessage("명령어 사용법: /태그 등록 <역할>");
                    return true;
                }
                String registerRole = strings[1];
                ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
                ItemMeta meta = netherStar.getItemMeta();
                if (meta != null) {
                    meta.displayName(Component.text(registerRole + " 역할 등록"));
                    netherStar.setItemMeta(meta);
                }

                NBTItem nbtItem = new NBTItem(netherStar);
                nbtItem.setString("RoleRegister", "true");
                nbtItem.setString("Role", registerRole);
                netherStar = nbtItem.getItem();

                player.getInventory().addItem(netherStar);
                player.sendMessage(registerRole + " 역할 등록 아이템 지급");
                break;

                //Todo: 등록취소에 대한 로직 이해를 다시 해봐야할듯
            case "등록취소":
                if (strings.length != 1) {
                    player.sendMessage("명령어 사용법: /태그 등록취소");
                    return true;
                }
                ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD);
                ItemMeta blazeMeta = blazeRod.getItemMeta();
                if (blazeMeta != null) {
                    blazeMeta.displayName(Component.text("역할 등록 취소"));
                    blazeRod.setItemMeta(blazeMeta);
                }

                NBTItem nbtItem2 = new NBTItem(blazeRod);
                nbtItem2.setString("RoleRegister", "true");
                nbtItem2.setString("Role", "시민");
                blazeRod = nbtItem2.getItem();

                player.getInventory().addItem(blazeRod);
                player.sendMessage("역할 취소용 블레이즈 막대 지급.");
                break;

            case "설정":
                if (strings.length != 3) {
                    player.sendMessage("명령어 사용법: /태그 설정 <플레이어> <역할>");
                    return true;
                }
                String targetPlayerName = strings[1];
                String targetRole = strings[2];
                try {
                    databaseManager.updateMemberRole(targetPlayerName, targetRole);
                } catch (SQLException e){
                    commandSender.sendMessage("등록되어 있지 않은 역할입니다.");
                }

                player.sendMessage(targetPlayerName + " 플레이어의 역할이 " + targetRole + "으로 설정됨.");
                break;

            default:
                player.sendMessage("태그 명렁어: /태그 [생성|등록|등록취소|설정]");
                break;
        }
        return true;
    }
}
