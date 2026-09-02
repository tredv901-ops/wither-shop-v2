package dev.withershop.shop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns the full pool of possible shop items and the 9 currently on offer.
 * Every hour, 9 different items are randomly chosen from the pool - players
 * never get to pick what's for sale, only whether to buy it.
 *
 * The current stock and the time of the last restock are persisted to disk
 * so that restarting the server does NOT reroll the shop early - it only
 * changes once a real hour has actually passed.
 */
public class ShopManager {

    private static final long RESTOCK_INTERVAL_TICKS = 20L * 60L * 60L; // 1 real hour
    private static final long RESTOCK_INTERVAL_MILLIS = (RESTOCK_INTERVAL_TICKS / 20L) * 1000L;
    private static final int SHOP_SIZE = 9;

    private final JavaPlugin plugin;
    private final Map<Material, ShopItem> itemPool = new LinkedHashMap<>();
    private final ShopItem[] currentStock = new ShopItem[SHOP_SIZE];

    private final File dataFile;
    private FileConfiguration config;
    private long lastRestockMillis;
    private BukkitTask restockTask;

    public ShopManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "shop.yml");
        buildPool();
        loadOrInitializeState();
        scheduleRestockTask();
    }

    /**
     * The full catalog the shop can draw from. Withers are far rarer and harder to
     * kill than Wardens (you have to farm Wither Skeleton skulls, build the summon
     * structure, then survive the boss fight), so points come in much slower -
     * prices here are tuned to be affordable on a handful of Wither kills rather
     * than dozens. Elytra is deliberately left out of the pool entirely - it never
     * appears for sale, at any price.
     */
    private void buildPool() {
        addItem(Material.IRON_INGOT, 1, 4);
        addItem(Material.GOLD_INGOT, 1, 4);
        addItem(Material.EXPERIENCE_BOTTLE, 1, 4);
        addItem(Material.EMERALD, 1, 3);
        addItem(Material.ENDER_PEARL, 1, 2);
        addItem(Material.BLAZE_ROD, 1, 2);
        addItem(Material.DIAMOND, 2, 1);
        addItem(Material.IRON_BLOCK, 3, 1);
        addItem(Material.GOLD_BLOCK, 3, 1);
        addItem(Material.GHAST_TEAR, 3, 1);
        addItem(Material.SPECTRAL_ARROW, 3, 8);
        addItem(Material.FIREWORK_ROCKET, 3, 8);
        addItem(Material.DIAMOND_SWORD, 4, 1);
        addItem(Material.DIAMOND_PICKAXE, 4, 1);
        addItem(Material.DIAMOND_CHESTPLATE, 5, 1);
        addItem(Material.NETHERITE_SCRAP, 5, 1);
        addItem(Material.SADDLE, 5, 1);
        addItem(Material.NAME_TAG, 5, 1);
        addItem(Material.EMERALD_BLOCK, 6, 1);
        addItem(Material.TRIDENT, 6, 1);
        addItem(Material.RESPAWN_ANCHOR, 6, 1);
        addItem(Material.LODESTONE, 6, 1);
        addItem(Material.DIAMOND_BLOCK, 8, 1);
        addItem(Material.SHULKER_BOX, 8, 1);
        addItem(Material.NETHERITE_INGOT, 9, 1);
        addItem(Material.TOTEM_OF_UNDYING, 10, 1);
        addItem(Material.ENCHANTED_GOLDEN_APPLE, 12, 1);
        addItem(Material.NETHER_STAR, 15, 1);
        addItem(Material.BEACON, 18, 1);
        addItem(Material.DRAGON_EGG, 20, 1);
        addItem(Material.NETHERITE_BLOCK, 25, 1);
        // Elytra intentionally excluded - it never shows up in the shop.
    }

    private void addItem(Material material, int price, int amount) {
        itemPool.put(material, new ShopItem(material, price, amount));
    }

    /**
     * Loads the saved stock + timestamp if present and still valid. If the file is
     * missing, corrupt, or refers to materials no longer in the pool, falls back to
     * generating a fresh random stock.
     */
    private void loadOrInitializeState() {
        if (dataFile.exists()) {
            config = YamlConfiguration.loadConfiguration(dataFile);
            long savedRestock = config.getLong("lastRestock", 0);
            List<String> savedNames = config.getStringList("stock");

            if (savedRestock > 0 && savedNames.size() == SHOP_SIZE) {
                ShopItem[] restored = new ShopItem[SHOP_SIZE];
                boolean valid = true;
                for (int i = 0; i < SHOP_SIZE; i++) {
                    try {
                        Material material = Material.valueOf(savedNames.get(i));
                        ShopItem item = itemPool.get(material);
                        if (item == null) {
                            valid = false;
                            break;
                        }
                        restored[i] = item;
                    } catch (IllegalArgumentException e) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    System.arraycopy(restored, 0, currentStock, 0, SHOP_SIZE);
                    lastRestockMillis = savedRestock;
                    return; // Successfully restored - do NOT roll a new stock.
                }
            }
        } else {
            plugin.getDataFolder().mkdirs();
            config = new YamlConfiguration();
        }

        // No valid saved state - roll a fresh stock now.
        rollNewStock();
        saveState();
    }

    /**
     * Schedules the recurring hourly restock, picking up wherever the saved timer
     * left off instead of always waiting a full hour from server start.
     */
    private void scheduleRestockTask() {
        long elapsed = System.currentTimeMillis() - lastRestockMillis;
        long initialDelayTicks;

        if (elapsed >= RESTOCK_INTERVAL_MILLIS) {
            // The server was offline (or just started) past the restock point - restock now.
            restock(false);
            initialDelayTicks = RESTOCK_INTERVAL_TICKS;
        } else {
            long remainingMillis = RESTOCK_INTERVAL_MILLIS - elapsed;
            initialDelayTicks = Math.max(1L, (remainingMillis * 20L) / 1000L);
        }

        restockTask = Bukkit.getScheduler().runTaskTimer(
                plugin, () -> restock(true), initialDelayTicks, RESTOCK_INTERVAL_TICKS);
    }

    private void rollNewStock() {
        List<Material> materials = new ArrayList<>(itemPool.keySet());
        Collections.shuffle(materials);
        for (int i = 0; i < SHOP_SIZE; i++) {
            currentStock[i] = itemPool.get(materials.get(i));
        }
        lastRestockMillis = System.currentTimeMillis();
    }

    private void restock(boolean announce) {
        rollNewStock();
        saveState();
        if (announce) {
            Bukkit.broadcast(
                    Component.text("☠ ", NamedTextColor.DARK_GRAY)
                            .append(Component.text("The Wither Shop", NamedTextColor.DARK_PURPLE)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" just restocked with 9 new items! Check ", NamedTextColor.GRAY))
                            .append(Component.text("/shop", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
            );
        }
    }

    /**
     * Immediately rerolls the stock (e.g. from /shoprestock) and resets the hourly
     * timer so the next automatic restock is a full hour from this moment.
     */
    public void forceRestock() {
        restock(true);
        if (restockTask != null) {
            restockTask.cancel();
        }
        restockTask = Bukkit.getScheduler().runTaskTimer(
                plugin, () -> restock(true), RESTOCK_INTERVAL_TICKS, RESTOCK_INTERVAL_TICKS);
    }

    private void saveState() {
        config.set("lastRestock", lastRestockMillis);
        List<String> names = new ArrayList<>();
        for (ShopItem item : currentStock) {
            names.add(item.getMaterial().name());
        }
        config.set("stock", names);
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save shop.yml: " + e.getMessage());
        }
    }

    public ShopItem[] getCurrentStock() {
        return currentStock;
    }
}
