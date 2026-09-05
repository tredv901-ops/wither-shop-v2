package dev.withershop.commands;

import dev.withershop.bounty.BountyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class BountyListCommand implements CommandExecutor {

    private final BountyManager bountyManager;

    public BountyListCommand(BountyManager bountyManager) {
        this.bountyManager = bountyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Map<UUID, Integer> all = bountyManager.getAllBounties();

        if (all.isEmpty()) {
            sender.sendMessage(Component.text(
                    "There are currently no active bounties.",
                    NamedTextColor.GRAY
            ));
            return true;
        }

        // Sort highest → lowest
        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(all.entrySet());
        sorted.sort(Map.Entry.<UUID, Integer>comparingByValue().reversed());

        sender.sendMessage(
                Component.text("===== Active Bounties =====", NamedTextColor.DARK_RED)
                        .decorate(TextDecoration.BOLD)
        );

        int rank = 1;

        for (Map.Entry<UUID, Integer> entry : sorted) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getKey());

            String name = target.getName() != null
                    ? target.getName()
                    : "Unknown";

            sender.sendMessage(
                    Component.text(
                            "#" + rank + " " + name + " - $" + entry.getValue(),
                            NamedTextColor.RED
                    )
            );

            rank++;
        }

        return true;
    }
}
```
