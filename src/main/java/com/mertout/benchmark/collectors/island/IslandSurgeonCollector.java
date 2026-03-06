package com.mertout.benchmark.collectors.island;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.mertout.benchmark.api.MetricCollector;
import com.mertout.benchmark.hooks.SuperiorHook;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IslandSurgeonCollector implements MetricCollector, Listener {

    // "Anlık Yük" takibi için son 1 dakikadaki verileri tutuyoruz
    private final Map<UUID, AtomicInteger> islandTransfers = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicInteger> islandSpawnActivity = new ConcurrentHashMap<>();

    @Override
    public void collect() {
        // Benchmark her saniye çalıştığı için burayı temizleme amacıyla kullanmıyoruz.
        // Ama istersen her 1 saatte bir haritaları temizleyen bir kontrol ekleyebilirsin.
    }

    // --- TEMİZLİK METODU (Önemli!) ---
    public void resetActivity() {
        islandTransfers.clear();
        islandSpawnActivity.clear();
    }

    // --- CANLI TAKİP ---

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTransfer(InventoryMoveItemEvent event) {
        // Huni (Hopper) trafiğini yakala
        Location loc = event.getSource().getLocation();
        if (loc == null) return;

        Island island = SuperiorHook.getIslandAt(loc);
        if (island != null) {
            islandTransfers.computeIfAbsent(island.getUniqueId(), k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobSpawn(EntitySpawnEvent event) {
        // Adadaki mob doğma aktivitesini yakala (Mob farm tespiti için)
        Island island = SuperiorHook.getIslandAt(event.getLocation());
        if (island != null) {
            islandSpawnActivity.computeIfAbsent(island.getUniqueId(), k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    // --- VERİ ÇEKME ---

    public int getTransfers(UUID islandId) {
        return islandTransfers.getOrDefault(islandId, new AtomicInteger(0)).get();
    }

    public int getSpawnActivity(UUID islandId) {
        return islandSpawnActivity.getOrDefault(islandId, new AtomicInteger(0)).get();
    }

    @Override
    public double calculateScore() {
        return 100; // Global skoru etkilemesin
    }

    @Override
    public Map<String, Object> getRawMetrics() {
        return new HashMap<>();
    }

    @Override
    public String getName() {
        return "Island Surgeon";
    }
}