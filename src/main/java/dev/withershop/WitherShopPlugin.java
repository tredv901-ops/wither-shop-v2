package dev.withershop;

import dev.withershop.commands.PayCommand;
import dev.withershop.commands.PointsCommand;
import dev.withershop.commands.ShopCommand;
import dev.withershop.commands.ShopRestockCommand;
import dev.withershop.listeners.ShopGuiListener;
import dev.withershop.listeners.WitherKillListener;
import dev.withershop.listeners.PlayerDeathListener;   // ← add this line
import dev.withershop.points.PointsManager;
import dev.withershop.shop.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;
import dev.withershop.commands.PointsBalCommand;
import java.util.UUID;
import dev.withershop.bounty.BountyManager;
import dev.withershop.commands.BountyCommand;
import dev.withershop.commands.BountyListCommand;
import dev.withershop.commands.BountyCompassCommand;
import dev.withershop.listeners.BountyDeathListener;

public class WitherShopPlugin extends JavaPlugin {

    private PointsManager pointsManager;
    private ShopManager shopManager;
    private BountyManager bountyManager;

    @Override
    public void onEnable() {
        pointsManager = new PointsManager(this);
        shopManager = new ShopManager(this); // starts the hourly restock task itself
        bountyManager = new BountyManager(this);
        getServer().getPluginManager().registerEvents(new WitherKillListener(pointsManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(pointsManager), this);
        getServer().getPluginManager().registerEvents(new ShopGuiListener(shopManager, pointsManager), this);
        if (getCommand("bountyplace") != null) {
            getCommand("bountyplace").setExecutor(new BountyCommand(bountyManager, pointsManager));
        }
        if (getCommand("bountylist") != null) {
            getCommand("bountylist").setExecutor(new BountyListCommand(bountyManager));
        }
        if (getCommand("bountycompass") != null) {
            getCommand("bountycompass").setExecutor(new BountyCompassCommand(bountyManager));
        }

        var shopExecutor = new ShopCommand(shopManager);
        var pointsExecutor = new PointsCommand(pointsManager);
        var payExecutor = new PayCommand(pointsManager);
        var pointsBalExecutor = new PointsBalCommand(pointsManager);

if (getCommand("pointsbal") != null) {
    getCommand("pointsbal").setExecutor(pointsBalExecutor);
}
        var shopRestockExecutor = new ShopRestockCommand(shopManager);
        if (getCommand("shop") != null) getCommand("shop").setExecutor(shopExecutor);
        if (getCommand("points") != null) getCommand("points").setExecutor(pointsExecutor);
        if (getCommand("pay") != null) getCommand("pay").setExecutor(payExecutor);
        if (getCommand("shoprestock") != null) getCommand("shoprestock").setExecutor(shopRestockExecutor);

        getLogger().info("WitherShop enabled - kill Withers, earn points, spend them with /shop.");
    }

@Override
    public void onDisable() {
        if (pointsManager != null) {
            pointsManager.save();
        }
        getLogger().info("WitherShop disabled - points saved.");
    }

    // Public helper so other classes can give points
    public void addPoints(UUID uuid, int amount) {
        if (pointsManager != null) {
            pointsManager.addPoints(uuid, amount);
        }
    }
// Added for KingOfTheHill / TheReaper (and any other plugin that needs to read/remove points)
    public int getPoints(UUID uuid) {
        return pointsManager != null ? pointsManager.getPoints(uuid) : 0;
    }

    public boolean removePoints(UUID uuid, int amount) {
        return pointsManager != null && pointsManager.removePoints(uuid, amount);
    }
}
