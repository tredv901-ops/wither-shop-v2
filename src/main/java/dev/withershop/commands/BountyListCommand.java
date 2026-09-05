package dev.withershop.commands;

import dev.withershop.bounty.BountyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class BountyListCommand implements CommandExecutor {

    private final BountyManager bountyManager;

    public BountyListCommand(BountyManager bountyManager) {
        this.bountyManager = bountyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can open the bounty list.");
            return true;
        }

        Map<UUID, Integer> all = bountyManager.getAllBounties();
        if (all.isEmpty()) {
            player.sendMessage(Component.text("There are currently no active bounties.", NamedTextColor.GRAY));
            return true;
        }

        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(all.entrySet());
        sorted.sort(Map.Entry.<UUID, Integer>comparingByValue().reversed());

        Inventory gui = Bukkit.createInventory(null, 54, Component.text("Active Bounties", NamedTextColor.DARK_RED));

        int slot = 0;
        for (Map.Entry<UUID, Integer> entry : sorted) {
            if (slot >= 54) break;

            OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getKey());
            String name = target.getName() != null ? target.getName() : "Unknown";

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.displayName(Component.text(name, NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
            meta.lore(List.of(
                    Component.text("Bounty: ", NamedTextColor.GRAY)
                            .append(Component.text(entry.getValue() + " points", NamedTextColor.GOLD))
            ));
            head.setItemMeta(meta);

            gui.setItem(slot, head);
            slot++;
        }

        player.openInventory(gui);
        return true;
    }
}
