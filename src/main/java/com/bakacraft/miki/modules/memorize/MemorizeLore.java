package com.bakacraft.miki.modules.memorize;

import org.bukkit.*;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

public final class MemorizeLore extends AbstractList<String> {
    public static final String HINT = "放置在背包内即可保存一次背包物品";
    public static final String CATEGORY = ChatColor.GOLD + "类别: " + ChatColor.WHITE + "追忆石";
    public static final String USAGE = ChatColor.LIGHT_PURPLE + "剩余次数: " + ChatColor.WHITE;
    public static boolean isValidLore(List<String> lore) {
        return Objects.equals(lore.size(), 3) && Objects.equals(lore.get(0), CATEGORY);
    }

    public static void verifyLore(List<String> lore) {
        if (!isValidLore(lore)) {
            throw new IllegalArgumentException("Invalid lore!");
        }
    }

    MemorizeLore(String binding, String usage) {
        this.binding = binding;
        this.usage = usage;
    }

    MemorizeLore(List<String> lore) {
        verifyLore(lore);
        this.usage = lore.get(1);
        this.binding = lore.get(2);
    }

    private final String binding;
    private String usage;

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
