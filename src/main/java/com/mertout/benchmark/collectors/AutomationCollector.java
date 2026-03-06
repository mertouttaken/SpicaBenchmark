package com.mertout.benchmark.collectors;

import com.mertout.benchmark.api.MetricCollector;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import java.util.HashMap;
import java.util.Map;

public class AutomationCollector implements MetricCollector {
    private int hopperChains = 0;
    private int totalHoppers = 0;

    @Override
    public void collect() {
        int hoppers = 0;
        int chains = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (BlockState tile : chunk.getTileEntities()) {
                    if (tile.getType() == Material.HOPPER) {
                        hoppers++;
                        // Etrafında başka hopper var mı? (Basit zincir kontrolü)
                        if (tile.getBlock().getRelative(0, -1, 0).getType() == Material.HOPPER) {
                            chains++;
                        }
                    }
                }
            }
        }
        this.totalHoppers = hoppers;
        this.hopperChains = chains;
    }

    @Override
    public double calculateScore() {
        // Zincirleme hopperlar normal hopperlardan 3 kat daha fazla puan götürür.
        double penalty = (totalHoppers * 0.1) + (hopperChains * 0.5);
        return Math.max(0, 100 - penalty);
    }

    @Override
    public Map<String, Object> getRawMetrics() {
        Map<String, Object> m = new HashMap<>();
        m.put("TotalHoppers", totalHoppers);
        m.put("ChainsDetected", hopperChains);
        return m;
    }

    @Override
    public String getName() { return "Automation System"; }
}