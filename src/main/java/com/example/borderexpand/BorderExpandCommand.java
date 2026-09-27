package com.example.borderexpand;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BorderExpandCommand implements CommandExecutor, TabCompleter {

    private final BorderExpandPlugin plugin;
    private final DiscoveryManager manager;

    public BorderExpandCommand(BorderExpandPlugin plugin, DiscoveryManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info" -> {
                sender.sendMessage(ChatColor.GREEN + "BorderExpand: " + ChatColor.WHITE
                        + manager.getDiscoveredCount() + ChatColor.GRAY + " material(s) discovered so far.");
                for (World world : plugin.getServer().getWorlds()) {
                    sender.sendMessage(ChatColor.GRAY + " - " + world.getName() + ": border size "
                            + world.getWorldBorder().getSize());
                }
                return true;
            }
            case "list" -> {
                Set<Material> all = manager.getDiscovered();
                if (all.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "Nothing discovered yet.");
                    return true;
                }
                String joined = all.stream().map(Material::name).collect(Collectors.joining(", "));
                sender.sendMessage(ChatColor.GREEN + "Discovered (" + all.size() + "): " + ChatColor.WHITE + joined);
                return true;
            }
            case "reset" -> {
                if (!sender.hasPermission("borderexpand.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                manager.resetAll();
                sender.sendMessage(ChatColor.GREEN + "Discovery list cleared. Border sizes were left as-is.");
                return true;
            }
            case "add" -> {
                if (!sender.hasPermission("borderexpand.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /borderexpand add <MATERIAL>");
                    return true;
                }
                try {
                    Material material = Material.valueOf(args[1].toUpperCase());
                    boolean added = manager.addSilently(material);
                    manager.save();
                    sender.sendMessage(added
                            ? ChatColor.GREEN + "Marked " + material + " as already discovered (border not expanded)."
                            : ChatColor.YELLOW + material + " was already marked as discovered.");
                } catch (IllegalArgumentException ex) {
                    sender.sendMessage(ChatColor.RED + "Unknown material: " + args[1]);
                }
                return true;
            }
            default -> {
                sendUsage(sender);
                return true;
            }
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- BorderExpand ---");
        sender.sendMessage(ChatColor.YELLOW + "/borderexpand info " + ChatColor.GRAY + "- show discovery count & border sizes");
        sender.sendMessage(ChatColor.YELLOW + "/borderexpand list " + ChatColor.GRAY + "- list everything discovered");
        sender.sendMessage(ChatColor.YELLOW + "/borderexpand reset " + ChatColor.GRAY + "- wipe the discovery list (admin)");
        sender.sendMessage(ChatColor.YELLOW + "/borderexpand add <material> " + ChatColor.GRAY + "- mark as discovered without expanding (admin)");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("info", "list", "reset", "add"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            List<String> names = new ArrayList<>();
            for (Material m : Material.values()) {
                if (m.isItem()) names.add(m.name());
            }
            return filter(names, args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String prefix) {
        String upper = prefix.toUpperCase();
        return options.stream()
                .filter(o -> o.toUpperCase().startsWith(upper))
                .limit(50)
                .collect(Collectors.toList());
    }
}
