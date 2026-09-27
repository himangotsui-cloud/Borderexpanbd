package com.example.borderexpand;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Tracks every Material that has ever been obtained on this server and
 * grows the world border each time a brand-new one shows up.
 */
public class DiscoveryManager {

    private final BorderExpandPlugin plugin;
    private final File dataFile;
    private final Set<Material> discovered = new HashSet<>();
    private boolean dirty = false;

    public DiscoveryManager(BorderExpandPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "discovered.yml");
    }

    public void load() {
        discovered.clear();
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        List<String> names = yaml.getStringList("discovered");
        for (String name : names) {
            try {
                Material m = Material.valueOf(name);
                discovered.add(m);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Skipping unknown material in discovered.yml: " + name);
            }
        }
    }

    public void save() {
        if (!dirty) {
            return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        List<String> names = discovered.stream().map(Enum::name).sorted().toList();
        yaml.set("discovered", names);
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yaml.save(dataFile);
            dirty = false;
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save discovered.yml: " + e.getMessage());
        }
    }

    /**
     * Registers a material as obtained.
     *
     * @return true if this material had never been discovered before (i.e. the
     *         border should expand and a message should be shown), false if it
     *         was already known.
     */
    public boolean discover(Material material) {
        if (material == null || material.isAir() || material.isLegacy()) {
            return false;
        }
        if (!discovered.add(material)) {
            return false;
        }
        dirty = true;
        save();
        expandBorders();
        return true;
    }

    public boolean isDiscovered(Material material) {
        return discovered.contains(material);
    }

    public int getDiscoveredCount() {
        return discovered.size();
    }

    public Set<Material> getDiscovered() {
        return Collections.unmodifiableSet(new TreeSet<>(discovered));
    }

    /**
     * Wipes all recorded discoveries. Does not touch current border sizes.
     */
    public void resetAll() {
        discovered.clear();
        dirty = true;
        save();
    }

    /**
     * Manually mark a material as discovered without expanding the border
     * (useful for admins seeding an existing world).
     */
    public boolean addSilently(Material material) {
        if (material == null) return false;
        boolean added = discovered.add(material);
        if (added) dirty = true;
        return added;
    }

    private void expandBorders() {
        double amount = plugin.getConfig().getDouble("border-expand-amount", 10.0);
        long durationSeconds = plugin.getConfig().getLong("expand-duration-seconds", 5);
        List<String> affected = plugin.getConfig().getStringList("affected-worlds");

        for (World world : Bukkit.getWorlds()) {
            if (!affected.isEmpty() && !affected.contains(world.getName())) {
                continue;
            }
            WorldBorder border = world.getWorldBorder();
            double newSize = border.getSize() + amount;
            border.setSize(newSize, durationSeconds);
        }
    }
}
