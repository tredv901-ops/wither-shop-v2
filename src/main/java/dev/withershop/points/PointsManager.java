package dev.withershop.points;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks each player's Wither-kill points and persists them to points.yml
 * so balances survive a server restart.
 */
public class PointsManager {

    private final JavaPlugin plugin;
    private final File file;
    private final FileConfiguration config;
    private final Map<UUID, Integer> points = new HashMap<>();

    public PointsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "points.yml");

        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create points.yml: " + e.getMessage());
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load() {
        ConfigurationSection section = config.getConfigurationSection("points");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                points.put(uuid, section.getInt(key));
            } catch (IllegalArgumentException ignored) {
                // Skip malformed entries instead of crashing on load.
            }
        }
    }

    public void save() {
        for (Map.Entry<UUID, Integer> entry : points.entrySet()) {
            config.set("points." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save points.yml: " + e.getMessage());
        }
    }

    public int getPoints(UUID uuid) {
        return points.getOrDefault(uuid, 0);
    }

    public void addPoint(UUID uuid) {
        addPoints(uuid, 1);
    }

    /**
     * Adds an arbitrary number of points (e.g. from /pay or /points give) and
     * immediately persists, so transfers survive a crash, not just a clean shutdown.
     */
    public void addPoints(UUID uuid, int amount) {
        points.merge(uuid, amount, Integer::sum);
        save();
    }

    /**
     * @return true if the player had enough points and they were deducted.
     */
    public boolean removePoints(UUID uuid, int amount) {
        int current = getPoints(uuid);
        if (current < amount) {
            return false;
        }
        points.put(uuid, current - amount);
        save();
        return true;
    }
}
