package com.bakacraft.miki.modules.lucky;

import com.bakacraft.miki.recipes.IRecipeRegistrar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Stream;

public class LuckyPaper implements IRecipeRegistrar, Listener {
    private static final String KEY = "lucky_paper";

    private static final String LUCKY = ChatColor.GOLD + "运势：" + ChatColor.WHITE + ChatColor.MAGIC + "????";
    private static final List<String> LORES = Arrays.asList(
            LUCKY,
            "使用此物品，点击左键/右键查看今日运势",
            "(大吉、吉、中吉、小吉、末吉、凶、大凶)"
    );

    private Recipe buildLuckyRecipe(NamespacedKey key) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "今日运势");
        meta.setLore(LORES);
        item.setItemMeta(meta);

        return new ShapelessRecipe(key, item)
            .addIngredient(Material.PAPER)
            .addIngredient(Material.BAMBOO)
            .addIngredient(Material.FEATHER);
    }

    private NamespacedKey key;
    private LuckyPlayerResultRepository repository;
    @Override
    public void register(Plugin plugin) {
        repository = new LuckyPlayerResultRepository(plugin);
        key = new NamespacedKey(plugin, KEY);
        plugin.getServer().addRecipe(buildLuckyRecipe(key));
    }

    @Override
    public void unregister() {
        Bukkit.removeRecipe(key);
    }

    public static boolean isLuckyPaper(ItemStack item) {
        return Optional.of(item)
                .map(ItemStack::getItemMeta)
                .map(ItemMeta::getLore)
                .map(Collection::stream)
                .flatMap(Stream::findFirst)
                .filter(LUCKY::equals)
                .isPresent();
    }

    public static final Random RAND = new Random();

    public LuckyResult roll() {
        int value = RAND.nextInt(1, 1000);
        if (value <= 10) {
            return LuckyResult.Miki;
        } else if (value <= 40) {
            return LuckyResult.Excellent;
        } else if (value <= 110) {
            return LuckyResult.Good;
        } else if (value <= 310) {
            return LuckyResult.Fair;
        } else if (value <= 560) {
            return LuckyResult.Little;
        } else if (value <= 910) {
            return LuckyResult.Just;
        } else if (value <= 970) {
            return LuckyResult.Bad;
        } else {
            return LuckyResult.ExtremelyBad;
        }
    }

    @EventHandler
    private void onPlayerUseItem(PlayerInteractEvent event) {
        if (Objects.equals(event.hasItem(), Boolean.FALSE)) {
            return;
        }
        if (!isLuckyPaper(event.getItem())) {
            return;
        }

        // 打开运势签
        // 先看看今天有没有打开过，打开过则提示今天已经开过了
        Player player = event.getPlayer();
        Optional<LuckyResult> todayOptional = repository.getToday(player);
        if (todayOptional.isPresent()) {
            LuckyResult result = todayOptional.get();
            player.sendMessage(ChatColor.RED + "今天你已经roll过啦！你的运势是" + result.getColor() + result.getText());
            return;
        }

        // 没有则roll运势
        LuckyResult result = roll();
        player.sendMessage(ChatColor.GOLD + "你今天roll出了：" + result.getColor() + result.getText());

        repository.setPlayerRollResult(player, result);
        event.getPlayer().getInventory().remove(event.getItem());
    }
}
