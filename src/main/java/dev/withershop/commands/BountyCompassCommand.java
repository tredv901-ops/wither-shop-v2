package dev.withershop.commands;

import dev.withershop.bounty.BountyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BountyCompassCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final BountyManager bountyManager;

    public BountyCompassCommand(JavaPlugin plugin, BountyManager bountyManager) {
        this.plugin = plugin;
        this.bountyManager = bountyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }

        Player bestTarget = null;
        int highest = 0;

        for (Map.Entry<UUID, Integer> entry : bountyManager.getAllBounties().entrySet()) {

            if (entry.getValue() < 100) {
                continue;
            }

            Player online = Bukkit.getPlayer(entry.getKey());

            if (online != null
                    && online.isOnline()
                    && !online.isDead()
                    && entry.getValue() > highest) {

                highest = entry.getValue();
                bestTarget = online;
            }
        }

        if (bestTarget == null) {
            player.sendMessage(
                    Component.text(
                            "There is currently no online player with a 100+ bounty.",
                            NamedTextColor.RED
                    )
            );
            return true;
        }

        ItemStack compass = new ItemStack(Material.COMPASS);

        ItemMeta meta = compass.getItemMeta();

        meta.displayName(
                Component.text(
                        "Bounty Tracker",
                        NamedTextColor.DARK_RED
                ).decorate(TextDecoration.BOLD)
        );

        meta.lore(List.of(
                Component.text("Tracking: ", NamedTextColor.GRAY)
                        .append(Component.text(
                                bestTarget.getName(),
                                NamedTextColor.YELLOW
                        )),
                Component.text("Bounty: ", NamedTextColor.GRAY)
                        .append(Component.text(
                                highest + " points",
                                NamedTextColor.GOLD
                        ))
        ));

        compass.setItemMeta(meta);

        player.getInventory().addItem(compass);

        // Set the initial compass target.
        player.setCompassTarget(bestTarget.getLocation());

        player.sendMessage(
                Component.text(
                        "✔ You received a Bounty Tracker compass pointing at ",
                        NamedTextColor.GREEN
                )
                .append(Component.text(
                        bestTarget.getName(),
                        NamedTextColor.YELLOW
                ))
                .append(Component.text(
                        " (" + highest + " points)",
                        NamedTextColor.GOLD
                ))
        );

        // Keep tracking this exact player.
        startCompassTracker(player, bestTarget);

        return true;
    }

    private void startCompassTracker(Player hunter, Player target) {

        UUID targetUUID = target.getUniqueId();

        BukkitTask task = new BukkitRunnable() {

            @Override
            public void run() {

                // Hunter left the server.
                if (!hunter.isOnline()) {
                    cancel();
                    return;
                }

                Player currentTarget = Bukkit.getPlayer(targetUUID);

                // Target left the server.
                if (currentTarget == null || !currentTarget.isOnline()) {
                    cancel();
                    return;
                }

                // Target died.
                if (currentTarget.isDead()) {
                    cancel();
                    return;
                }

                // Target's bounty dropped below 100.
                if (bountyManager.getBounty(targetUUID) < 100) {
                    cancel();
                    return;
                }

                // Continuously update the compass location.
                hunter.setCompassTarget(currentTarget.getLocation());
            }

        }.runTaskTimer(plugin, 0L, 20L);
    }
}
