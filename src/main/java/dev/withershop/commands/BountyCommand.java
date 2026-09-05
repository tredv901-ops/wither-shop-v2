package dev.withershop.commands;

import dev.withershop.bounty.BountyManager;
import dev.withershop.points.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyCommand implements CommandExecutor {

    private final BountyManager bountyManager;
    private final PointsManager pointsManager;

    public BountyCommand(BountyManager bountyManager, PointsManager pointsManager) {
        this.bountyManager = bountyManager;
        this.pointsManager = pointsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can place bounties.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /bountyplace <player> <amount>", NamedTextColor.RED));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("You cannot place a bounty on yourself.", NamedTextColor.RED));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Amount must be a number.", NamedTextColor.RED));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(Component.text("Amount must be positive.", NamedTextColor.RED));
            return true;
        }

        if (!pointsManager.removePoints(player.getUniqueId(), amount)) {
            player.sendMessage(Component.text("You don't have enough points.", NamedTextColor.RED));
            return true;
        }

        bountyManager.addBounty(target.getUniqueId(), amount);
        int totalBounty = bountyManager.getBounty(target.getUniqueId());

        player.sendMessage(
                Component.text("✔ Placed a bounty of ", NamedTextColor.GREEN)
                        .append(Component.text(amount + " points", NamedTextColor.YELLOW))
                        .append(Component.text(" on ", NamedTextColor.GREEN))
                        .append(Component.text(target.getName(), NamedTextColor.RED))
        );

        if (totalBounty >= 100) {
            Bukkit.broadcast(
                    Component.text("☠ HIGH BOUNTY ☠ ", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD)
                            .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" now has a bounty of ", NamedTextColor.GRAY))
                            .append(Component.text(totalBounty + " points!", NamedTextColor.GOLD))
            );
        }

        return true;
    }
}
