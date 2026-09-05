package dev.withershop.listeners;

import dev.withershop.bounty.BountyManager;
import dev.withershop.points.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class BountyDeathListener implements Listener {

    private final BountyManager bountyManager;
    private final PointsManager pointsManager;

    public BountyDeathListener(BountyManager bountyManager, PointsManager pointsManager) {
        this.bountyManager = bountyManager;
        this.pointsManager = pointsManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) {
            return;
        }

        int bounty = bountyManager.claimBounty(victim.getUniqueId());
        if (bounty <= 0) {
            return; // No bounty on this player
        }

        // Give the bounty points to the killer
        pointsManager.addPoints(killer.getUniqueId(), bounty);

        // Announce it
        Bukkit.broadcast(
                Component.text("☠ ", NamedTextColor.DARK_RED)
                        .append(Component.text(killer.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" claimed the bounty on ", NamedTextColor.GRAY))
                        .append(Component.text(victim.getName(), NamedTextColor.RED))
                        .append(Component.text(" and received ", NamedTextColor.GRAY))
                        .append(Component.text(bounty + " points!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
        );

        killer.sendMessage(
                Component.text("✔ You received ", NamedTextColor.GREEN)
                        .append(Component.text(bounty + " points", NamedTextColor.GOLD))
                        .append(Component.text(" from the bounty!", NamedTextColor.GREEN))
        );
    }
}
