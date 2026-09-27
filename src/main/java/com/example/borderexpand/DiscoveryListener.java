package com.example.borderexpand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class DiscoveryListener implements Listener {

    private final BorderExpandPlugin plugin;
    private final DiscoveryManager manager;

    public DiscoveryListener(BorderExpandPlugin plugin, DiscoveryManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("track-mining", true)) return;
        tryDiscover(event.getBlock().getType(), event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!plugin.getConfig().getBoolean("track-pickup", true)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        tryDiscover(event.getItem().getItemStack().getType(), player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!plugin.getConfig().getBoolean("track-crafting", true)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack result = event.getInventory().getResult();
        if (result == null) return;
        tryDiscover(result.getType(), player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        if (!plugin.getConfig().getBoolean("track-smelting", true)) return;
        tryDiscover(event.getItemType(), event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (!plugin.getConfig().getBoolean("track-fishing", true)) return;
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item item)) return;
        tryDiscover(item.getItemStack().getType(), event.getPlayer());
    }

    private void tryDiscover(Material material, Player player) {
        boolean isNew = manager.discover(material);
        if (!isNew) return;

        if (plugin.getConfig().getBoolean("broadcast-message", true)) {
            String template = plugin.getConfig().getString("discovery-message",
                    "&a&l[Discovery]&r &e%player% &7found &f%material% &7for the first time!");
            String formatted = template
                    .replace("%player%", player.getName())
                    .replace("%material%", prettyName(material));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', formatted));
        }

        String soundName = plugin.getConfig().getString("discovery-sound", "");
        if (soundName != null && !soundName.isBlank()) {
            try {
                Sound sound = Sound.valueOf(soundName);
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.playSound(online.getLocation(), sound, 1.0f, 1.0f);
                }
            } catch (IllegalArgumentException ignored) {
                // invalid sound name in config; skip silently
            }
        }
    }

    private String prettyName(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
