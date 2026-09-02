package dev.withershop.listeners;

import dev.withershop.points.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Awards exactly 1 point to whichever player lands the killing blow on a Wither.
 * Withers are far rarer than Wardens - you have to farm Wither Skeleton skulls,
 * build the summon structure, then survive the boss fight - so this is meant to
 * be a much slower trickle of points than a Warden-based economy would be.
 */
public class WitherKillListener implements Listener {

    private final PointsManager pointsManager;

    public WitherKillListener(PointsManager pointsManager) {
        this.pointsManager = pointsManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() != EntityType.WITHER) {
            return;
        }

        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return; // Wither didn't die to a direct player hit.
        }

        pointsManager.addPoint(killer.getUniqueId());
        int total = pointsManager.getPoints(killer.getUniqueId());

        killer.sendMessage(
                Component.text("☠ ", NamedTextColor.DARK_GRAY)
                        .append(Component.text("WITHER SLAIN", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                        .append(Component.text("  +1 point", NamedTextColor.GREEN))
                        .append(Component.text("  (total: " + total + ")", NamedTextColor.GRAY))
                        .append(Component.text("  -  spend it with ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("/shop", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
        );
    }
}
