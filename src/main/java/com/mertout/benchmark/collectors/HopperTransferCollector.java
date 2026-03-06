package com.mertout.benchmark.collectors;

import com.mertout.benchmark.api.MetricCollector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HopperTransferCollector implements MetricCollector, Listener {

    // Koordinat bazlı transfer sayacı (Blok bazında gruplama)
    private final Map<Vector, AtomicInteger> transferCounts = new ConcurrentHashMap<>();
    private final Map<Vector, String> worldNames = new ConcurrentHashMap<>();

    private double totalImpactScore = 0;
    private List<String> topBottlenecks = new ArrayList<>();

    @Override
    public void collect() {
        // Her 60 saniyede bir (veya her benchmark yenilendiğinde) verileri analiz et
        List<Map.Entry<Vector, AtomicInteger>> sorted = new ArrayList<>(transferCounts.entrySet());
        sorted.sort((a, b) -> b.getValue().get() - a.getValue().get());

        topBottlenecks.clear();
        totalImpactScore = 0;

        // En yoğun 5 noktayı raporla
        for (int i = 0; i < Math.min(5, sorted.size()); i++) {
            Map.Entry<Vector, AtomicInteger> entry = sorted.get(i);
            int count = entry.getValue().get();
            Vector vec = entry.getKey();
            String world = worldNames.get(vec);

            // Tahmini Yük Hesaplama (Heuristic):
            // 1.16.5'te yoğun bir huni işlemi yaklaşık 0.05ms - 0.1ms arası tick maliyeti yaratabilir.
            double estimatedImpact = (count / 60.0) * 0.08; // 1 saniyeye düşen ms yükü
            totalImpactScore += estimatedImpact;

            topBottlenecks.add(String.format("§e%s, %d, %d, %d §7-> §c%d transfer §8(Impact: ~%.1f%%)",
                    world, vec.getBlockX(), vec.getBlockY(), vec.getBlockZ(), count, (estimatedImpact / 50) * 100));
        }

        // Analiz bittikten sonra sayacı sıfırla (Rolling window)
        transferCounts.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (event.getSource().getLocation() == null) return;

        Location loc = event.getSource().getLocation();
        Vector vec = loc.toVector();

        transferCounts.computeIfAbsent(vec, k -> new AtomicInteger(0)).incrementAndGet();
        worldNames.putIfAbsent(vec, loc.getWorld().getName());
    }

    @Override
    public double calculateScore() {
        // Toplam huni yükü 5ms'yi (1 tick'in %10'u) geçiyorsa puan düşmeye başlar
        return Math.max(0, 100 - (totalImpactScore * 15));
    }

    @Override
    public Map<String, Object> getRawMetrics() {
        Map<String, Object> m = new HashMap<>();
        m.put("HopperHotspots", topBottlenecks);
        m.put("TotalTransferImpactMs", String.format("%.2fms", totalImpactScore));
        return m;
    }

    @Override
    public String getName() { return "Transfer Bottleneck"; }
}