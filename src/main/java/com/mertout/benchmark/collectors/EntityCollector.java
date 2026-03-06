package com.mertout.benchmark.collectors;

import com.mertout.benchmark.api.MetricCollector;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityCollector implements MetricCollector {
    private int totalEntities, livingEntities, items, projectiles, armorStands;
    private double entitiesPerChunk;

    @Override
    public void collect() {
        int t = 0, l = 0, i = 0, p = 0, a = 0;
        int loadedChunksCount = 0;

        for (World world : Bukkit.getWorlds()) {
            // world.getEntityCount() yerine listeyi alıyoruz
            List<Entity> allEntities = world.getEntities();
            t += allEntities.size();

            loadedChunksCount += world.getLoadedChunks().length;

            // Listeyi bir kez dönerek kategorize ediyoruz
            for (Entity e : allEntities) {
                if (e instanceof LivingEntity && !(e instanceof Player)) {
                    l++;
                } else if (e instanceof Item) {
                    i++;
                } else if (e instanceof Projectile) {
                    p++;
                } else if (e instanceof ArmorStand) {
                    a++;
                }
            }
        }

        this.totalEntities = t;
        this.livingEntities = l;
        this.items = i;
        this.projectiles = p;
        this.armorStands = a;
        this.entitiesPerChunk = loadedChunksCount > 0 ? (double) t / loadedChunksCount : 0;
    }

    @Override
    public double calculateScore() {
        // Formül: Chunk başına düşen entity yoğunluğuna göre puanlama
        double score = 100.0 - (entitiesPerChunk * 1.5);

        // Eğer yaşayan mob sayısı (LivingEntity) 1500'ü geçerse ağır ceza uygula
        if (livingEntities > 1500) score -= 20;

        return Math.max(0, Math.min(100, score));
    }

    @Override
    public Map<String, Object> getRawMetrics() {
        Map<String, Object> m = new HashMap<>();
        m.put("Total", totalEntities);
        m.put("Living", livingEntities);
        m.put("Items", items);
        m.put("ArmorStands", armorStands);
        m.put("PerChunk", String.format("%.2f", entitiesPerChunk));
        return m;
    }

    @Override
    public String getName() { return "Entity System"; }
}