package com.mertout.benchmark.collectors;

import com.mertout.benchmark.api.MetricCollector;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.util.*;

public class PluginProfiler implements MetricCollector {
    private final Map<String, Long> pluginTickTimes = new HashMap<>();

    @Override
    public void collect() {
        // Not: Bu işlem Paper'ın kendi Timings verilerinden de çekilebilir
        // ancak biz bağımsız bir 'Impact Score' hesaplıyoruz.
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (!plugin.isEnabled()) continue;
            // Basit bir simülasyon: Gerçekte bu, Event geçiş sürelerinden hesaplanır.
            long estimatedTime = calculatePluginLoad(plugin);
            pluginTickTimes.put(plugin.getName(), estimatedTime);
        }
    }

    private long calculatePluginLoad(Plugin p) {
        // Pluginin register ettiği listener sayısına göre bir yük tahmini
        return Bukkit.getPluginManager().getPermissionSubscriptions(p.getName()).size() * 100L;
    }

    @Override
    public double calculateScore() {
        // Eğer bir plugin tick süresinin %10'undan fazlasını tek başına yiyorsa puan düşer.
        return 100; // Örnek skor
    }

    @Override
    public Map<String, Object> getRawMetrics() {
        return new HashMap<>(pluginTickTimes);
    }

    @Override
    public String getName() { return "Plugin Impact"; }
}