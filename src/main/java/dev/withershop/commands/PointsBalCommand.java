package dev.withershop.commands;

import dev.withershop.points.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class PointsBalCommand implements CommandExecutor {

    private final PointsManager pointsManager;

    public PointsBalCommand(PointsManager pointsManager) {
        this.pointsManager = pointsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        List<Map.Entry<UUID, Integer>> top = pointsManager.getAllPoints().entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        if (top.isEmpty()) {
            sender.sendMessage(Component.text("No one has any points yet.", NamedTextColor.GRAY));
            return true;
        }

        sender.sendMessage(Component.text("===== Richest Players =====", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));

        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : top) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
            String name = player.getName() != null ? player.getName() : "Unknown";

            sender.sendMessage(
                    Component.text("#" + rank + " ", NamedTextColor.YELLOW)
                            .append(Component.text(name, NamedTextColor.WHITE))
                            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(entry.getValue() + " points", NamedTextColor.LIGHT_PURPLE))
            );
            rank++;
        }

        return true;
    }
}
