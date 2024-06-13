package org.blisle.hxxniverskeycard.commands;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import org.blisle.hxxniverskeycard.connection.DatabaseManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class KeycardCommand implements CommandExecutor {
    private final DatabaseManager databaseManager;

    public KeycardCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("플레이어만 사용 가능한 명령어 입니다.");
            return true;
        }

        if (strings.length == 0) {
            player.sendMessage("키카드 관련 명령어: /키카드 [생성|철문|등록|아이템설정]");
            return true;
        }

        String subCommand = strings[0];

        switch (subCommand) {
            case "생성":
                if (strings.length != 3) {
                    player.sendMessage("키카드 명령어: /키카드 생성 <이름> <태그>");
                    return true;
                }
                String name = strings[1];
                String tag = strings[2];
                try {
                    if(databaseManager.keyCardExists(name, tag)) player.sendMessage("이미 등록되어있는 키카드입니다.");
                    else {
                        databaseManager.insertKeyCard(name, tag);
                        player.sendMessage("키카드 " + name + " (태그: " + tag + ") 생성됨.");
                    }
                } catch (SQLException e) {
                    player.sendMessage(e.getMessage());
                }
                break;

            case "철문":
                if (strings.length != 2) {
                    player.sendMessage("키카드 명령어: /키카드 철문 <이름>");
                    return true;
                }
                String permission = strings[1];

                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if(itemInHand.getType() != Material.IRON_DOOR) {
                    player.sendMessage("철문을 손에 들고 명령어를 입력해 주세요.");
                    return true;
                }
                ItemMeta meta = itemInHand.getItemMeta();
                if (meta != null) {
                    meta.displayName(Component.text(permission + " 문"));
                    meta.addEnchant(Enchantment.LUCK, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                NBTItem nbtItem = new NBTItem(itemInHand);
                nbtItem.getItem().setItemMeta(meta);
                nbtItem.setString("PermissionRegister", "true");
                nbtItem.setString("Permission", permission);
                nbtItem.applyNBT(itemInHand);

                player.sendMessage("철문에 " + permission + " 권한 등록됨.");
                break;

            case "등록":
                if (strings.length != 2) {
                    player.sendMessage("키카드 명령어: /키카드 등록 <이름>");
                    return true;
                }
                String permission2 = strings[1];

                ItemStack itemInHand2 = player.getInventory().getItemInMainHand();
                if(itemInHand2.isEmpty()) {
                    player.sendMessage("손에 물건을 들고 명령어를 입력해 주세요.");
                    return true;
                }

                ItemMeta meta2 = itemInHand2.getItemMeta();
                if (meta2 != null) {
                    meta2.displayName(Component.text(permission2 + " 키카드"));
                    meta2.addEnchant(Enchantment.LUCK, 1, true);
                    meta2.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                NBTItem nbtItem2 = new NBTItem(itemInHand2);
                nbtItem2.getItem().setItemMeta(meta2);
                nbtItem2.setString("PermissionRegister", "true");
                nbtItem2.setString("Permission", permission2);
                nbtItem2.applyNBT(itemInHand2);
                player.sendMessage("손에 들고 있는 아이템이 " + permission2 + " 키카드로 등록됨.");
                break;

//              Todo: 작지서에 내용 추가 요구
//            case "아이템설정":
//                player.sendMessage("키카드 아이템 설정됨.");
//                break;
//            default:
//                player.sendMessage("키카드 명령어: /키카드 [생성|철문|등록|아이템설정]");
//                break;
        }

        return true;
    }
}
