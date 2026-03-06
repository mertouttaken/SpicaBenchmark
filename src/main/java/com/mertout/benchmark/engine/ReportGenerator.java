package com.mertout.benchmark.engine;

import com.mertout.benchmark.SpicaBenchmark;
import com.mertout.benchmark.api.MetricCollector;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportGenerator {

    private final SpicaBenchmark plugin;

    public ReportGenerator(SpicaBenchmark plugin) {
        this.plugin = plugin;
    }

    public List<String> generate() {
        List<String> report = new ArrayList<>();
        double globalScore = plugin.getEngine().getLastGlobalScore();

        report.add(ChatColor.DARK_GRAY + "========================================");
        report.add(ChatColor.GOLD + "       " + ChatColor.BOLD + "SPICA BENCHMARK REPORT");
        report.add(ChatColor.GRAY + "       Global Server Score: " + getScoreColor(globalScore) + (int)globalScore + "/100");
        report.add(getProgressBar(globalScore));
        report.add("");

        // 1. KATEGORİ BAZLI PUANLAR
        report.add(ChatColor.YELLOW + "System Breakdown:");
        for (MetricCollector collector : plugin.getCollectors()) {
            double score = collector.calculateScore();
            report.add(ChatColor.GRAY + " - " + collector.getName() + ": " + getScoreColor(score) + (int)score + "/100");
        }

        // 2. THE ORACLE (Gelecek Tahmini) - YENİ EKLEME 🔮
        OraclePredictor oracle = plugin.getEngine().getOracle();
        if (oracle != null) {
            OraclePredictor.PredictionResult prediction = oracle.predict();
            report.add("");
            report.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "🔮 THE ORACLE (Performance Forecast):");

            if (prediction.minutesToLag > 60 || prediction.minutesToLag <= 0) {
                report.add(ChatColor.GRAY + " - Status: " + ChatColor.GREEN + "Optimal (Next 60m looks clean)");
            } else {
                report.add(ChatColor.GRAY + " - Status: " + ChatColor.RED + "LAG RISK in " + (int)prediction.minutesToLag + " mins!");
                report.add(ChatColor.GRAY + " - Trend: " + ChatColor.YELLOW + prediction.status);
            }
            report.add(ChatColor.GRAY + " - Hardware Efficiency: " + ChatColor.AQUA + String.format("%.2f", oracle.getEfficiencyCoefficient()));
        }

        // 3. KRİTİK DARBOĞAZLAR (Transfer & Thread Ghost)
        boolean hasCriticalInfo = false;
        for (MetricCollector c : plugin.getCollectors()) {
            // Transfer Bottleneck Analizi
            if (c.getName().equals("Transfer Bottleneck")) {
                Map<String, Object> metrics = c.getRawMetrics();
                List<String> hotspots = (List<String>) metrics.get("HopperHotspots");

                if (hotspots != null && !hotspots.isEmpty()) {
                    if (!hasCriticalInfo) report.add("");
                    report.add(ChatColor.RED + "" + ChatColor.BOLD + "⚠️ LAG SOURCES (Hopper Hotspots):");
                    hasCriticalInfo = true;
                    for (String hotspot : hotspots) {
                        report.add("  " + hotspot);
                    }
                }
            }
            // Thread Ghost Analizi
            else if (c.getName().equals("Thread Ghost")) {
                Map<String, Object> metrics = c.getRawMetrics();
                long blocked = (long) metrics.get("BlockedTimeMs");
                if (blocked > 50) {
                    if (!hasCriticalInfo) report.add("");
                    report.add(ChatColor.RED + " ● [!] " + ChatColor.GRAY + "Thread Ghost Detected!");
                    report.add(ChatColor.YELLOW + "   - Suspected: " + ChatColor.WHITE + metrics.get("SuspectedPlugin"));
                    report.add(ChatColor.YELLOW + "   - Delay: " + ChatColor.WHITE + blocked + "ms lock time");
                    hasCriticalInfo = true;
                }
            }
            else if (c.getName().equals("Chunk Thrash Detector")) {
                List<String> thrashPoints = (List<String>) c.getRawMetrics().get("ThrashPoints");
                if (thrashPoints != null && !thrashPoints.isEmpty()) {
                    report.add(ChatColor.RED + "" + ChatColor.BOLD + "🚨 CHUNK THRASHING (Disko Modu):");
                    for (String point : thrashPoints) {
                        report.add("  " + point);
                    }
                    report.add(ChatColor.GRAY + "  Tavsiye: Görüş mesafesini (View Distance) sabitleyin veya");
                    report.add(ChatColor.GRAY + "  sürekli chunk yükleyen eklentileri kontrol edin.");
                }
            }
            else if (c.getName().equals("AI Traffic Radar")) {
                Map<String, Object> metrics = c.getRawMetrics();
                Map<String, Integer> hotspots = (Map<String, Integer>) metrics.get("AIHotspots");

                if (hotspots != null && !hotspots.isEmpty()) {
                    report.add("");
                    report.add(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "🧠 AI TRAFFIC JAM (Pathfinding Load):");
                    hotspots.forEach((loc, count) -> {
                        report.add(ChatColor.GRAY + " - " + ChatColor.WHITE + loc + ChatColor.RED + " -> " + count + " mob yol hesaplıyor!");
                    });
                    report.add(ChatColor.GRAY + "  Tavsiye: Bu chunklardaki mobları dar alanlardan çıkarın.");
                }
            }
        }

        // 4. AKILLI ANALİZ (Öneriler)
        report.add("");
        report.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "Top Performance Issues:");

        List<String> issues = detectIssues();
        if (issues.isEmpty()) {
            report.add(ChatColor.GREEN + " * No major issues detected. Great job!");
        } else {
            for (String issue : issues) {
                report.add(ChatColor.RED + " * " + ChatColor.GRAY + issue);
            }
        }

        report.add(ChatColor.DARK_GRAY + "========================================");
        return report;
    }

    private List<String> detectIssues() {
        List<String> issues = new ArrayList<>();

        plugin.getCollectors().forEach(c -> {
            double score = c.calculateScore();
            if (score < 70) {
                switch (c.getName()) {
                    case "Entity System":
                        issues.add("High entity density! Check for mob farms.");
                        break;
                    case "Tick System":
                        issues.add("Low MSPT detected! Potential main thread bottleneck.");
                        break;
                    case "CPU System":
                        issues.add("CPU usage is peaking! Consider upgrading your hardware.");
                        break;
                    case "Chunk System":
                        issues.add("Excessive loaded chunks! Lower your View Distance.");
                        break;
                    case "Automation System":
                        issues.add("Heavy automation detected (Hoppers/Redstone clocks).");
                        break;
                    case "Plugin Impact":
                        issues.add("One or more plugins are consuming too much tick time.");
                        break;
                    case "Transfer Bottleneck":
                        issues.add("Critical item transfer load in specific chunks!");
                        break;
                    case "Thread Ghost":
                        issues.add("Async threads are causing lock contention (Thread Ghost).");
                        break;
                }
            }
        });

        return issues;
    }

    private String getProgressBar(double score) {
        int filled = (int) (score / 10);
        StringBuilder bar = new StringBuilder(ChatColor.GRAY + "       [");
        bar.append(getScoreColor(score));
        for (int i = 0; i < 10; i++) {
            if (i < filled) bar.append("■");
            else bar.append("□");
        }
        bar.append(ChatColor.GRAY + "]");
        return bar.toString();
    }

    private ChatColor getScoreColor(double score) {
        if (score >= 85) return ChatColor.GREEN;
        if (score >= 65) return ChatColor.YELLOW;
        if (score >= 40) return ChatColor.GOLD;
        return ChatColor.RED;
    }
}