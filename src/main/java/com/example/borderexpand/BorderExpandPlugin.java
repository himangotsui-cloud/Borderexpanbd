package com.example.borderexpand;

import org.bukkit.plugin.java.JavaPlugin;

public class BorderExpandPlugin extends JavaPlugin {

    private DiscoveryManager discoveryManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.discoveryManager = new DiscoveryManager(this);
        this.discoveryManager.load();

        getServer().getPluginManager().registerEvents(new DiscoveryListener(this, discoveryManager), this);

        BorderExpandCommand executor = new BorderExpandCommand(this, discoveryManager);
        getCommand("borderexpand").setExecutor(executor);
        getCommand("borderexpand").setTabCompleter(executor);

        getLogger().info("BorderExpand enabled. " + discoveryManager.getDiscoveredCount()
                + " material(s) already discovered on this server.");
    }

    @Override
    public void onDisable() {
        if (discoveryManager != null) {
            discoveryManager.save();
        }
    }

    public DiscoveryManager getDiscoveryManager() {
        return discoveryManager;
    }
}
