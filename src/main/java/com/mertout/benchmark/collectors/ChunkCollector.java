package com.mertout.benchmark.collectors;

import com.mertout.benchmark.api.MetricCollector;
import org.bukkit.Bukkit;
import org.bukkit.World;
import java.util.HashMap;
import java.util.Map;

public class ChunkCollector implements MetricCollector {
    private int loadedChunks;
    private double chunksPerPlayer;

    @Override
    public void collect() {
        this.loadedChunks = 0;
        int players = Bukkit.getOnlinePlayers().size();

        for (World world : Bukkit.getWorlds()) {
            this.loadedChunks += world.getLoadedChunks().length;
        }

        this.chunksPerPlayer = players > 0 ? (double) loadedChunks / players : loadedChunks;
    }

    @Override
    public double calculateScore() {
        // İdeal: Oyuncu başı 250-300 chunk üstü risklidir (1.16.5 için).
        double score = 100 - (chunksPerPlayer / 10);
        return Math.max(0, Math.min(100, score));
    }

    @Override
    public Map<String, Object> getRawMetrics() {
        Map<String, Object> m = new HashMap<>();
        m.put("Loaded", loadedChunks);
        m.put("PerPlayer", String.format("%.1f", chunksPerPlayer));
        return m;
    }

    @Override
    public String getName() { return "Chunk System"; }
}