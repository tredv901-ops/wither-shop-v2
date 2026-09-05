package dev.withershop.listeners;

import dev.withershop.points.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final PointsManager pointsManager;

    public PlayerDeathListener(PointsManager pointsManager) {
        this.pointsManager = pointsManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Only care about player vs player kills
        if (killer == null || killer.equals(victim)) {
            return;
        }

        int victimPoints = pointsManager.getPoints(victim.getUniqueId());
        if (victimPoints <= 0) {
            return; // Nothing to steal
        }

        // 50% rounded UP for the killer (11 → 6, 10 → 5)
        int stolen = (victimPoints + 1) / 2;

        // Take the points from the victim
        pointsManager.removePoints(victim.getUniqueId(), stolen);

        // Give them to the killer
        pointsManager.addPoints(killer.getUniqueId(), stolen);

        int killerTotal = pointsManager.getPoints(killer.getUniqueId());
        int victimTotal = pointsManager.getPoints(victim.getUniqueId());

        // Messages
        killer.sendMessage(
                Component.text("⚔ ", NamedTextColor.RED)
                        .append(Component.text("You stole ", NamedTextColor.GRAY))
                        .append(Component.text(stolen + " points", NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
                        .append(Component.text(" from ", NamedTextColor.GRAY))
                        .append(Component.text(victim.getName(), NamedTextColor.YELLOW))
                        .append(Component.text("! (total: " + killerTotal + ")", NamedTextColor.DARK_GRAY))
        );

        victim.sendMessage(
                Component.text("☠ ", NamedTextColor.DARK_RED)
                        .append(Component.text(killer.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" stole ", NamedTextColor.GRAY))
                        .append(Component.text(stolen + " points", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                        .append(Component.text(" from you! (remaining: " + victimTotal + ")", NamedTextColor.DARK_GRAY))
        );
    }
}
