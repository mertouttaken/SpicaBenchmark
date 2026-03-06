package com.mertout.benchmark.engine;

import com.mertout.benchmark.SpicaBenchmark;
import com.mertout.benchmark.api.MetricCollector;
import com.mertout.benchmark.collectors.TickCollector;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BenchmarkEngine {
    private final SpicaBenchmark plugin;
    private final LogManager logManager;
    private final OraclePredictor oracle; // 🔮 Kahin eklendi

    private boolean active = false;
    private BukkitTask currentTask;
    private BukkitTask loggingTask;

    private double lastGlobalScore = 0;
    private final Map<String, Double> categoryScores = new HashMap<>();

    public BenchmarkEngine(SpicaBenchmark plugin) {
        this.plugin = plugin;
        this.logManager = new LogManager(plugin);
        this.oracle = new OraclePredictor(plugin);
    }

    public void startBenchmark() {
        if (active) return;
        this.active = true;

        // 1. Ana Analiz Döngüsü (Her saniye - 20 tick)
        this.currentTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            updateMetrics();
        }, 20L, 20L);

        // 2. Otomatik Log Döngüsü (Her 30 Dakika = 36000 tick)
        this.loggingTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!active) return;

            ReportGenerator generator = new ReportGenerator(plugin);
            List<String> report = generator.generate();
            logManager.saveReportToFile(report);

        }, 1200L, 36000L);
    }

    public void stopBenchmark() {
        if (!active) return;
        this.active = false;

        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }

        if (loggingTask != null) {
            loggingTask.cancel();
            loggingTask = null;
        }
    }

    private void updateMetrics() {
        double weightedSum = 0;
        double weightTotal = 0;

        for (MetricCollector collector : plugin.getCollectors()) {
            collector.collect();
            double score = collector.calculateScore();
            double weight = getWeightFor(collector.getName());

            categoryScores.put(collector.getName(), score);
            weightedSum += (score * weight);
            weightTotal += weight;

            // 🔮 Kahin için MSPT verisini besle (TickCollector'dan çekilir)
            if (collector instanceof TickCollector) {
                oracle.updateSnapshot(((TickCollector) collector).getMspt());
            }
        }
        lastGlobalScore = weightedSum / weightTotal;
    }

    // --- GETTERS ---

    public boolean isActive() {
        return active;
    }

    public OraclePredictor getOracle() {
        return oracle;
    }

    public double getLastGlobalScore() {
        return lastGlobalScore;
    }

    public Map<String, Double> getCategoryScores() {
        return categoryScores;
    }

    private double getWeightFor(String name) {
        switch (name) {
            case "Tick System": return 0.25;
            case "Entity System": return 0.15;
            case "Chunk System": return 0.15;
            case "CPU System": return 0.15;
            case "Network System": return 0.10;
            case "Thread Ghost": return 0.10; // Thread Ghost ağırlığı eklendi
            case "Automation System": return 0.05;
            default: return 0.05;
        }
    }
}