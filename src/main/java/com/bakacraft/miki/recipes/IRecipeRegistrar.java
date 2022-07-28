package com.bakacraft.miki.recipes;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public interface IRecipeRegistrar {
    void register(Plugin plugin);

    void unregister();
}
