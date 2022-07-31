package com.bakacraft.miki.modules.lucky;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum LuckyResult {
    /**
     * 弥
     */
    Miki (ChatColor.DARK_PURPLE, "弥弥弥弥", 1),
    /**
     * 大吉
     */
    Excellent (ChatColor.GOLD, "大吉", 3),
    /**
     * 吉
     */
    Good (ChatColor.AQUA, "吉", 7),
    /**
     * 中吉
     */
    Fair (ChatColor.BLUE, "中吉", 20),
    /**
     * 小吉
     */
    Little (ChatColor.DARK_AQUA, "小吉", 25),
    /**
     * 末吉
     */
    Just (ChatColor.DARK_BLUE, "末吉", 35),
    /**
     * 凶
     */
    Bad (ChatColor.RED, "凶", 6),
    /**
     * 大凶
     */
    ExtremelyBad (ChatColor.DARK_RED, "大凶", 3),
    ;

    private final ChatColor color;
    private final String text;
    private final int ratio;

    LuckyResult(ChatColor color, String text, int ratio) {
        this.color = color;
        this.text = text;
        this.ratio = ratio;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getText() {
        return text;
    }

    public int getRatio() {
        return ratio;
    }

    public static int sum() {
        return Arrays.stream(LuckyResult.values())
                .mapToInt(LuckyResult::getRatio)
                .sum();
    }
    private static final int SUM = sum();
    public static final Random RANDOM = new Random();
    public static LuckyResult roll() {
        LuckyResult[] values = LuckyResult.values();
        int point = RANDOM.nextInt(1, SUM);
        Iterator<LuckyResult> randIterator = Arrays.stream(values)
                .sorted((a, b) -> RANDOM.nextInt(values.length))
                .iterator();

        int current = 0;
        while (randIterator.hasNext()) {
            LuckyResult next = randIterator.next();
            current += next.getRatio();
            if (current > point) {
                return next;
            }
        }
        throw new IllegalArgumentException();
    }
}
