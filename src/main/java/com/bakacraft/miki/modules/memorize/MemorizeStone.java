package com.bakacraft.miki.modules.memorize;

import com.bakacraft.miki.recipes.IRecipeRegistrar;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class MemorizeStone implements IRecipeRegistrar, Listener {

    public static final String STR_KEY = "memorize_stones";

    public static final String FULL_SUFFIX_NAME = ChatColor.BLUE +  "追忆钻石";

    private ShapedRecipe buildAdvanceRecipe(NamespacedKey key) {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(FULL_SUFFIX_NAME);
        meta.setLore(Arrays.asList(MemorizeLore.CATEGORY, MemorizeLore.USAGE + "7", MemorizeLore.HINT));
        item.setItemMeta(meta);

        return new ShapedRecipe(key, item)
                .shape("ABC","DEF","GGG")
                .setIngredient('A', Material.PUFFERFISH)
                .setIngredient('B', Material.QUARTZ_BLOCK)
                .setIngredient('C', Material.ENDER_EYE)
                .setIngredient('D', Material.RABBIT_HIDE)
                .setIngredient('E', Material.DIAMOND)
                .setIngredient('F', Material.SOUL_CAMPFIRE)
                .setIngredient('G', Material.LEATHER);
    }

    private NamespacedKey key;
    @Override
    public void register(Plugin plugin) {
        key = new NamespacedKey(plugin, STR_KEY);
        Bukkit.addRecipe(buildAdvanceRecipe(key));
    }

    private static boolean isMemorizeStone(ItemStack itemStack) {
        return Optional.ofNullable(itemStack)
                .map(ItemStack::getItemMeta)
                .map(ItemMeta::getLore)
                .map(MemorizeLore::isValidLore)
                .orElse(false);
    }

    @EventHandler
    private void onPlayerCraftItem(CraftItemEvent event) {
        if (isMemorizeStone(event.getCurrentItem())) {
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

            event.getWhoClicked().sendMessage(ChatColor.GREEN + "你合成了一颗追忆钻石，放入背包即可在死亡时保存物品。");
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.getInventory().all(Material.DIAMOND)
                .values().stream()
                .filter(MemorizeStone::isMemorizeStone)
                .anyMatch(item -> {
                    ItemMeta meta = item.getItemMeta();
                    MemorizeLore lore = new MemorizeLore(meta.getLore());

                    if (lore.use()) {
                        event.getEntity().sendMessage(ChatColor.GOLD + "你死亡的瞬间传来追忆钻石碎裂的声音...你发现在你死亡的瞬间回到了出生点，非常饥饿，快吃点东西吧！");
                        meta.setLore(lore);
                        item.setItemMeta(meta);

                        long estimate = lore.getEstimate();

                        if (estimate == 0) {
                            event.getEntity().sendMessage(ChatColor.RED + "追忆钻石在你的背包破碎了...");
                            player.getInventory().remove(item);
                        } else {
                            event.getEntity().sendMessage(ChatColor.RED + "你的追忆钻石剩余可用次数为" + estimate);
                        }

                        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 2);
                        player.setFoodLevel(4);
                        event.setKeepInventory(true);
                        event.setKeepLevel(false);
                        player.setTotalExperience(0);
                        player.setLevel(0);
                        event.getDrops().clear();
                        // try teleport to normal world first
                        Location spawnLocation = Bukkit.getWorlds().stream()
                            .filter(world -> World.Environment.NORMAL.equals(world.getEnvironment()))
                            .findFirst()
                            .orElseGet(player::getWorld)
                            .getSpawnLocation();
                        player.setRemainingAir(0);
                        player.teleport(spawnLocation);
                        return true;
                    } else {
                        event.getEntity().sendMessage(ChatColor.RED + "你的追忆钻石似乎不可用");
                    }
                    return false;
                });
    }

    @Override
    public void unregister() {
        Bukkit.removeRecipe(key);
    }
}
