package com.mertout.benchmark.hooks;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SuperiorHook {

    public static boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("SuperiorSkyblock2");
    }

    public static Island getIslandAt(Location loc) {
        if (!isEnabled()) return null;
        return SuperiorSkyblockAPI.getIslandAt(loc);
    }
}