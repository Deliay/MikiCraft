package com.bakacraft.miki;

import com.bakacraft.miki.recipes.TeleportStone;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {
    private TeleportStone teleportStone;
    @Override
    public void onEnable() {
        teleportStone = new TeleportStone();
        teleportStone.register(this);
        getServer().getPluginManager().registerEvents(teleportStone, this);
        this.getLogger().info(String.format("%s Miki Craft spigot plugin enabled! (%s)", ChatColor.GREEN, this.getName()));
    }

    @Override
    public void onDisable() {
        teleportStone.unregister();
        this.getLogger().info(String.format("%s Miki Craft spigot plugin disabled! (%s)", ChatColor.GREEN, this.getName()));
    }
}
