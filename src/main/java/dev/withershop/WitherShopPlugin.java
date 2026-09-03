package dev.withershop;

import dev.withershop.commands.PayCommand;
import dev.withershop.commands.PointsCommand;
import dev.withershop.commands.ShopCommand;
import dev.withershop.commands.ShopRestockCommand;
import dev.withershop.listeners.ShopGuiListener;
import dev.withershop.listeners.WitherKillListener;
import dev.withershop.points.PointsManager;
import dev.withershop.shop.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class WitherShopPlugin extends JavaPlugin {

    private PointsManager pointsManager;
    private ShopManager shopManager;

    @Override
    public void onEnable() {
        pointsManager = new PointsManager(this);
        shopManager = new ShopManager(this); // starts the hourly restock task itself

        getServer().getPluginManager().registerEvents(new WitherKillListener(pointsManager), this);
        getServer().getPluginManager().registerEvents(new ShopGuiListener(shopManager, pointsManager), this);

        var shopExecutor = new ShopCommand(shopManager);
        var pointsExecutor = new PointsCommand(pointsManager);
        var payExecutor = new PayCommand(pointsManager);
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
}
