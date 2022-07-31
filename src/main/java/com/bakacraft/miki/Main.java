package com.bakacraft.miki;

import com.bakacraft.miki.modules.lucky.LuckyPaper;
import com.bakacraft.miki.modules.memorize.MemorizeStone;
import com.bakacraft.miki.modules.teleport.TeleportStone;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {
    private TeleportStone teleportStone;
    private MemorizeStone memorizeStone;
    private LuckyPaper luckyPaper;
    @Override
    public void onEnable() {
        teleportStone = new TeleportStone();
        teleportStone.register(this);


        memorizeStone = new MemorizeStone();
        memorizeStone.register(this);

        luckyPaper = new LuckyPaper();
        luckyPaper.register(this);

        getServer().getPluginManager().registerEvents(teleportStone, this);
        getServer().getPluginManager().registerEvents(memorizeStone, this);
        getServer().getPluginManager().registerEvents(luckyPaper, this);
        this.getLogger().info(String.format("%s Miki Craft spigot plugin enabled! (%s)", ChatColor.GREEN, this.getName()));
    }

    @Override
    public void onDisable() {
        teleportStone.unregister();
        memorizeStone.unregister();
        luckyPaper.unregister();
        this.getLogger().info(String.format("%s Miki Craft spigot plugin disabled! (%s)", ChatColor.GREEN, this.getName()));
    }
}
