package com.mertout.benchmark.collectors;

import com.mertout.benchmark.api.MetricCollector;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AITrafficCollector implements MetricCollector {

    private final Map<String, Integer> aiHotspots = new ConcurrentHashMap<>();
    private int totalAIProcessing = 0;
    private double aiScore = 100;

    @Override
    public void collect() {
        aiHotspots.clear();
        totalAIProcessing = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                int aiLoadInChunk = 0;

                for (Entity entity : chunk.getEntities()) {
                    // Sadece AI'ya sahip canlıları (Mobları) kontrol et
                    if (entity instanceof LivingEntity && ((CraftEntity) entity).getHandle() instanceof EntityInsentient) {
                        EntityInsentient nmsMob = (EntityInsentient) ((CraftEntity) entity).getHandle();

                        // NMS Navigasyon kontrolü: Mob şu an bir yol hesaplıyor mu veya bir yere gidiyor mu?
                        if (nmsMob.getNavigation() != null && nmsMob.getNavigation().n()) {
                            aiLoadInChunk++;
                        }
                    }
                }

                if (aiLoadInChunk > 0) {
                    totalAIProcessing += aiLoadInChunk;
                    if (aiLoadInChunk > 15) { // Bir chunkta 15+ mob aynı anda yol buluyorsa tehlikelidir
                        String key = world.getName() + " [" + chunk.getX() + "," + chunk.getZ() + "]";
                        aiHotspots.put(key, aiLoadInChunk);
                    }
                }
            }
        }
    }

    @Override
    public double calculateScore() {
        // AI yükü arttıkça puan düşer. 200+ aktif pathfinding sunucuyu yorar.
        this.aiScore = Math.max(0, 100 - (totalAIProcessing * 0.4));
        return aiScore;
    }

    @Override
    public Map<String, Object> getRawMetrics() {
        Map<String, Object> m = new HashMap<>();
        m.put("ActivePathfinding", totalAIProcessing);
        m.put("AIHotspots", aiHotspots);
        return m;
    }

    @Override
    public String getName() { return "AI Traffic Radar"; }
}