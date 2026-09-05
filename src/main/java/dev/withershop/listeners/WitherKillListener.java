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

import java.util.concurrent.ThreadLocalRandom;

/**
 * Awards a random amount of points (1–10) to whichever player
 * lands the killing blow on a Wither.
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

        // Random points between 1 and 10 (inclusive)
        int amount = ThreadLocalRandom.current().nextInt(1, 11);
        pointsManager.addPoints(killer.getUniqueId(), amount);

        int total = pointsManager.getPoints(killer.getUniqueId());

        killer.sendMessage(
                Component.text("☠ ", NamedTextColor.DARK_GRAY)
                        .append(Component.text("WITHER SLAIN", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                        .append(Component.text("  +" + amount + " point" + (amount == 1 ? "" : "s"), NamedTextColor.GREEN))
                        .append(Component.text("  (total: " + total + ")", NamedTextColor.GRAY))
                        .append(Component.text("  -  spend it with ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("/shop", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
        );
    }
}
