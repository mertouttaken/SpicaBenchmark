package com.mertout.benchmark.collectors;

import com.mertout.benchmark.api.MetricCollector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkThrashCollector implements MetricCollector, Listener {

    private final Map<String, AtomicInteger> thrashMap = new ConcurrentHashMap<>();
    private final List<String> detectedThrashPoints = new ArrayList<>();
    private double globalThrashScore = 100;

    @Override
    public void collect() {
        detectedThrashPoints.clear();
        double penalty = 0;

        // En çok yüklenen/boşaltılan ilk 3 noktayı bul
        List<Map.Entry<String, AtomicInteger>> sorted = new ArrayList<>(thrashMap.entrySet());
        sorted.sort((a, b) -> b.getValue().get() - a.getValue().get());

        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            Map.Entry<String, AtomicInteger> entry = sorted.get(i);
            int count = entry.getValue().get();

            // 1 dakikada 20'den fazla yükle-boşalt ciddidir
            if (count > 20) {
                detectedThrashPoints.add(String.format("§e%s §7-> §c%d döngü §8(Kritik!)", entry.getKey(), count));
                penalty += (count * 0.5);
            }
        }

        this.globalThrashScore = Math.max(0, 100 - penalty);
        // Her dakika verileri sıfırla ki trendi güncel tutalım
        thrashMap.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) return; // Yeni oluşan dünyalar thrash sayılmaz
        track(event.getChunk().getX(), event.getChunk().getZ(), event.getWorld().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        track(event.getChunk().getX(), event.getChunk().getZ(), event.getWorld().getName());
    }

    private void track(int x, int z, String world) {
        String key = world + " [" + x + "," + z + "]";
        thrashMap.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }

    @Override
    public double calculateScore() { return globalThrashScore; }

    @Override
    public Map<String, Object> getRawMetrics() {
        Map<String, Object> m = new HashMap<>();
        m.put("ThrashPoints", detectedThrashPoints);
        return m;
    }

    @Override
    public String getName() { return "Chunk Thrash Detector"; }
}