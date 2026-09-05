package dev.withershop.bounty;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BountyManager {

    private final JavaPlugin plugin;
    private final File file;
    private final FileConfiguration config;
    private final Map<UUID, Integer> bounties = new HashMap<>();

    public BountyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bounties.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create bounties.yml");
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load() {
        if (config.getConfigurationSection("bounties") == null) return;
        for (String key : config.getConfigurationSection("bounties").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                bounties.put(uuid, config.getInt("bounties." + key));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void save() {
        config.set("bounties", null);
        for (Map.Entry<UUID, Integer> entry : bounties.entrySet()) {
            config.set("bounties." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save bounties.yml");
        }
    }

    public int getBounty(UUID uuid) {
        return bounties.getOrDefault(uuid, 0);
    }

    public void addBounty(UUID uuid, int amount) {
        bounties.merge(uuid, amount, Integer::sum);
        save();
    }

    public int claimBounty(UUID uuid) {
        int amount = bounties.getOrDefault(uuid, 0);
        if (amount > 0) {
            bounties.remove(uuid);
            save();
        }
        return amount;
    }

    public Map<UUID, Integer> getAllBounties() {
        return new HashMap<>(bounties);
    }

    public boolean hasBounty(UUID uuid) {
        return bounties.containsKey(uuid) && bounties.get(uuid) > 0;
    }
}
