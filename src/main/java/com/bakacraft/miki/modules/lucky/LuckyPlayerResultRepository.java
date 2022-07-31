package com.bakacraft.miki.modules.lucky;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LuckyPlayerResultRepository {
    private static final String SECTION_LUCKY = "lucky";
    private final Plugin plugin;
    private final ConfigurationSection rootSection;
    private final Map<String, Map<String, LuckyResult>> cache = new HashMap<>();

    LuckyPlayerResultRepository(Plugin plugin) {
        this.plugin = plugin;
        if (!this.plugin.getConfig().contains(SECTION_LUCKY)) {
            this.plugin.getConfig().createSection(SECTION_LUCKY);
        }
        rootSection = this.plugin.getConfig().getConfigurationSection(SECTION_LUCKY);
    }

    private String now() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ISO_DATE);
    }

    private ConfigurationSection getTodaySection() {
        String now = now();
        return Optional.of(now)
                .map(rootSection::getConfigurationSection)
                .orElseGet(() -> rootSection.createSection(now));
    }

    public Optional<LuckyResult> getToday(Player player) {
        return Optional.of(player)
                .map(Player::getUniqueId)
                .map(UUID::toString)
                .map(getTodaySection()::getString)
                .map(LuckyResult::valueOf);
    }

    public void setPlayerRollResult(Player player, LuckyResult result) {
        String uuid = player.getUniqueId().toString();
        ConfigurationSection todaySection = getTodaySection();

        if (!todaySection.contains(uuid)) {
            todaySection.set(uuid, result.name());
            this.plugin.saveConfig();
        }
    }
}
