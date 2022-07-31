package com.bakacraft.miki.modules.teleport;

import com.bakacraft.miki.recipes.IRecipeRegistrar;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class TeleportStone implements IRecipeRegistrar, Listener {
    public static final String STR_KEY = "teleport_stones";
    public static final String STR_KEY_ADV = "teleport_stones_adv";

    public static final String FULL_SUFFIX_NAME = ChatColor.AQUA +  "传送钻石";

    private ShapedRecipe buildAdvanceRecipe(NamespacedKey key) {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(FULL_SUFFIX_NAME);
        meta.setLore(Arrays.asList(TeleportLore.CATEGORY, TeleportLore.USAGE + "50", TeleportLore.getNotSetText));
        item.setItemMeta(meta);

        return new ShapedRecipe(key, item)
            .shape(" A ","BDB"," B ")
            .setIngredient('A', Material.ENDER_PEARL)
            .setIngredient('B', Material.AMETHYST_SHARD)
            .setIngredient('D', Material.DIAMOND);
    }

    private ShapedRecipe buildNormalRecipe(NamespacedKey key) {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("普通的" + FULL_SUFFIX_NAME);
        meta.setLore(Arrays.asList(TeleportLore.CATEGORY, TeleportLore.USAGE + "25", TeleportLore.getNotSetText));
        item.setItemMeta(meta);

        return new ShapedRecipe(key, item)
            .shape(" A ", "BDB", " B ")
            .setIngredient('A', Material.ENDER_PEARL)
            .setIngredient('B', Material.GLOWSTONE_DUST)
            .setIngredient('D', Material.DIAMOND);
    }

    private NamespacedKey keyNormal;
    private NamespacedKey keyAdvance;
    @Override
    public void register(Plugin plugin) {
        keyNormal = new NamespacedKey(plugin, STR_KEY);
        keyAdvance = new NamespacedKey(plugin, STR_KEY_ADV);

        Bukkit.addRecipe(buildAdvanceRecipe(keyNormal));
        Bukkit.addRecipe(buildNormalRecipe(keyAdvance));
    }

    @Override
    public void unregister() {
        Bukkit.removeRecipe(keyNormal);
        Bukkit.removeRecipe(keyAdvance);
    }

    private static boolean isTeleportStone(ItemStack itemStack) {
        return Optional.ofNullable(itemStack)
                .map(ItemStack::getItemMeta)
                .map(ItemMeta::getLore)
                .map(TeleportLore::isValidLore)
                .orElse(false);
    }

    private static void processBind(Block block, ItemStack teleportStone, TeleportLore lore) {
        ItemMeta meta = teleportStone.getItemMeta();

        meta.setLore(lore.setLocation(block.getLocation()));

        teleportStone.setItemMeta(meta);
    }

    private static void processTeleport(Player player, ItemStack teleportStone, TeleportLore lore) {
        ItemMeta itemMeta = teleportStone.getItemMeta();
        if (lore.use()) {
            itemMeta.setLore(lore);
            teleportStone.setItemMeta(itemMeta);
            player.teleport(lore.getLocation());
            player.sendMessage(ChatColor.GOLD + "已传送到对应位置");
        } else {
            player.sendMessage(ChatColor.RED + "使用次数已耗尽！");
        }
    }

    @EventHandler
    private void onPlayerCraftItem(CraftItemEvent event) {
        if (isTeleportStone(event.getCurrentItem())) {
            boolean isOriginalDiamond = Arrays.stream(event.getInventory().getMatrix())
                    .filter(item -> Objects.equals(item.getType(), Material.DIAMOND))
                    .filter(ItemStack::hasItemMeta)
                    .map(ItemStack::getItemMeta).filter(Objects::nonNull)
                    .map(ItemMeta::getLore).filter(Objects::nonNull)
                    .allMatch(Collection::isEmpty);

            if (!isOriginalDiamond) {
                event.getWhoClicked().sendMessage(ChatColor.RED + "这颗钻石已经经过铭刻了，无法再进行合成");
                event.setCancelled(true);
                return;
            }

            event.getWhoClicked().sendMessage(ChatColor.GREEN + "你合成了一颗传送钻石，对方块按右键设置地点，按左键进行传送");
        }
    }

    @EventHandler
    private void onPlayerUseItem(PlayerInteractEvent event) {
        if (Objects.equals(event.hasItem(), Boolean.FALSE)) {
            return;
        }

        ItemStack item = event.getItem();
        if (!isTeleportStone(item)) {
            return;
        }

        TeleportLore lore = new TeleportLore(item.getItemMeta().getLore());

        Player player = event.getPlayer();

        final boolean isBound = lore.isBound();
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK -> {
                if (isBound) {
                    player.sendMessage(ChatColor.RED + "这个传送石已经绑定了！");
                    return;
                }
                processBind(event.getClickedBlock(), item, lore);
            }
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (!isBound) {
                    player.sendMessage(ChatColor.RED + "这个传送石尚未绑定，右击一个方块来设置传送坐标！");
                    return;
                }
                processTeleport(player, item, lore);
            }
        }
    }
}
