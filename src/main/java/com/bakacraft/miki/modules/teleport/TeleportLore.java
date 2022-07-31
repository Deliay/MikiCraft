package com.bakacraft.miki.modules.teleport;

import org.bukkit.*;
import org.bukkit.block.Block;

import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public final class TeleportLore extends AbstractList<String> {
    public static final String getNotSetText = "(未设置) 对地面按右键即可设置";
    public static final String CATEGORY = ChatColor.GOLD + "类别: " + ChatColor.WHITE + "传送石";
    public static final String USAGE = ChatColor.LIGHT_PURPLE + "剩余次数: " + ChatColor.WHITE;
    public static boolean isValidLore(List<String> lore) {
        return Objects.equals(lore.size(), 3) && Objects.equals(lore.get(0), CATEGORY);
    }

    public static void verifyLore(List<String> lore) {
        if (!isValidLore(lore)) {
            throw new IllegalArgumentException("Invalid lore!");
        }
    }

    TeleportLore(String binding, String usage) {
        this.binding = binding;
        this.usage = usage;
    }

    TeleportLore(List<String> lore) {
        verifyLore(lore);
        this.usage = lore.get(1);
        this.binding = lore.get(2);
    }

    private String binding;
    private String usage;

    public boolean isBound() {
        return !Objects.equals(binding, getNotSetText);
    }

    public TeleportLore setLocation(Location location) {
        String raw = String.format("%s,%s,%s,%s",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
        this.binding = Base64.getEncoder()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public Location getLocation() {
        String raw = new String(Base64.getDecoder().decode(this.binding), StandardCharsets.UTF_8);
        String[] locationStr = raw.split(",");

        World world = Bukkit.getWorld(locationStr[0]);
        int x = Integer.parseInt(locationStr[1]);
        int y = Integer.parseInt(locationStr[2]);
        int z = Integer.parseInt(locationStr[3]);
        Block blockAt = world.getBlockAt(x, y, z);

        // not safe!
        if (Material.AIR.equals(blockAt.getType())) {
            int safeY = world.getHighestBlockYAt(x, z);
            return new Location(world, x, safeY + 1L, z);
        }
        return new Location(world, x, y + 1L, z);
    }

    private String getRawUsage() {
        return this.usage.substring(USAGE.length());
    }

    public long getEstimate() {
        return Long.parseLong(getRawUsage().split("/")[0]);
    }

    public boolean use() {
        long estimate = getEstimate();
        if (estimate <= 0) {
            return false;
        }
        this.usage = USAGE + (estimate - 1);
        return true;
    }

    @Override
    public String get(int index) {
        switch (index) {
            case 0 -> {
                return CATEGORY;
            }
            case 1 -> {
                return usage;
            }
            case 2 -> {
                return binding;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        return 3;
    }
}
