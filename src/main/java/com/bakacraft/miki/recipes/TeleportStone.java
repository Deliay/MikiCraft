package com.bakacraft.miki.recipes;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class TeleportStone implements IRecipeRegistrar, Listener {
    public static final class TeleportLore extends AbstractList<String> {
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
            return !Objects.equals(binding, NOT_SET);
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
            if (estimate <= 0){
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
    private static final String STR_KEY = "teleport_stones";
    private static final String STR_KEY_ADV = "teleport_stones_adv";
    private static final String NOT_SET = "(未设置) 对地面按右键即可设置";
    private static final String CATEGORY = ChatColor.GOLD + "类别: " + ChatColor.WHITE + "传送石";
    private static final String USAGE = ChatColor.LIGHT_PURPLE + "剩余次数: " + ChatColor.WHITE;

    private static final String FULL_SUFFIX_NAME = ChatColor.AQUA +  "传送钻石";

    private ShapedRecipe buildAdvanceRecipe(NamespacedKey key) {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(FULL_SUFFIX_NAME);
        meta.setLore(Arrays.asList(CATEGORY, USAGE + "50", NOT_SET));
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
        meta.setLore(Arrays.asList(CATEGORY, USAGE + "25", NOT_SET));
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
        } else {
            player.sendMessage("使用次数已耗尽！");
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
                player.sendMessage(ChatColor.GOLD + "已传送到对应位置");
            }
        }
    }
}
