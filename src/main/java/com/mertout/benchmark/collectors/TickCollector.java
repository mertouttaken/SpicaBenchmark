package com.mertout.benchmark.collectors;

import com.mertout.benchmark.api.MetricCollector;
import java.util.HashMap;
import java.util.Map;

public class TickCollector implements MetricCollector {

    private long lastTickTime = System.nanoTime();
    private double lastMspt = 0;
    private int spikes = 0;
    private int longTicks = 0;

    /**
     * Bu metot SpicaBenchmark ana sınıfındaki BukkitRunnable içinden
     * her tick başında çağrılmalıdır.
     */
    public void tick() {
        long now = System.nanoTime();
        long diff = (now - lastTickTime) / 1_000_000; // milisaniyeye çevir

        // MSPT Hesaplama (Üstel Hareketli Ortalama - EMA)
        // 0.05 katsayısı verinin %5 yeni, %95 eski olmasını sağlar (yumuşatma).
        lastMspt = (lastMspt * 0.95) + (diff * 0.05);

        if (diff > 50) {
            spikes++;
            longTicks++;
        }

        lastTickTime = now;
    }

    @Override
    public void collect() {
    }

    @Override
    public double calculateScore() {
        // İdeal MSPT 15ms ve altıdır (100 puan). 50ms ve üstü 0 puandır.
        double score = (50.0 - lastMspt) * (100.0 / 35.0);
        return Math.max(0, Math.min(100, score));
    }

    @Override
    public Map<String, Object> getRawMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("MSPT", String.format("%.2fms", lastMspt));
        metrics.put("TPS", String.format("%.1f", Math.min(20.0, 1000.0 / lastMspt)));
        metrics.put("Spikes", spikes);
        return metrics;
    }

    @Override
    public String getName() {
        return "Tick System";
    }

    // --- Getters ---
    public double getMspt() { return lastMspt; }
    public int getSpikes() { return spikes; }
}