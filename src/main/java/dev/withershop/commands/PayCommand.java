package dev.withershop.commands;

import dev.withershop.points.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /pay <player> <amount> - transfers points from the sender to another
 * online player. No OP/permission required - any player can pay any other.
 */
public class PayCommand implements CommandExecutor {

    private final PointsManager pointsManager;

    public PayCommand(PointsManager pointsManager) {
        this.pointsManager = pointsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player payer)) {
            sender.sendMessage("Only players can pay points.");
            return true;
        }

        if (args.length != 2) {
            payer.sendMessage(Component.text("Usage: /pay <player> <amount>").color(NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            payer.sendMessage(Component.text("Player '" + args[0] + "' is not online.").color(NamedTextColor.RED));
            return true;
        }
        if (target.getUniqueId().equals(payer.getUniqueId())) {
            payer.sendMessage(Component.text("You can't pay yourself.").color(NamedTextColor.RED));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            payer.sendMessage(Component.text("Amount must be a whole number.").color(NamedTextColor.RED));
            return true;
        }
        if (amount <= 0) {
            payer.sendMessage(Component.text("Amount must be positive.").color(NamedTextColor.RED));
            return true;
        }

        if (!pointsManager.removePoints(payer.getUniqueId(), amount)) {
            payer.sendMessage(Component.text("You don't have " + amount + " points to send.")
                    .color(NamedTextColor.RED));
            return true;
        }

        pointsManager.addPoints(target.getUniqueId(), amount);

        payer.sendMessage(
                Component.text("✔ ", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                        .append(Component.text("Sent " + amount + " points to " + target.getName() + ".",
                                NamedTextColor.GREEN))
        );
        target.sendMessage(
                Component.text("✔ ", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                        .append(Component.text(payer.getName() + " sent you " + amount + " points!",
                                NamedTextColor.GREEN))
        );
        return true;
    }
}
