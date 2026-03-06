package com.mertout.benchmark.engine;

import com.mertout.benchmark.SpicaBenchmark;
import org.bukkit.Bukkit;
import java.util.LinkedList;

public class OraclePredictor {

    private final SpicaBenchmark plugin;
    private final LinkedList<Double> msptHistory = new LinkedList<>();
    private final int MAX_HISTORY = 10;

    private double efficiencyCoefficient = 1.0;
    private long baselineScore = 1000;

    public OraclePredictor(SpicaBenchmark plugin) {
        this.plugin = plugin;
    }

    /**
     * Eksik olan metot: Geçmiş MSPT verilerinin ortalamasını alır.
     */
    public double getAverageMspt() {
        if (msptHistory.isEmpty()) return 20.0; // Varsayılan sağlıklı değer

        double sum = 0;
        for (double d : msptHistory) {
            sum += d;
        }
        return sum / msptHistory.size();
    }

    /**
     * Donanım gücü ile mevcut MSPT arasındaki ilişkiyi kurar.
     * Efficiency $E = \frac{MSPT_{avg} \times Players}{Baseline}$
     */
    public void calibrate(long hardwareMs) {
        this.baselineScore = hardwareMs;
        double currentMspt = getAverageMspt();
        int players = Math.max(1, Bukkit.getOnlinePlayers().size());

        // Bu katsayı, her oyuncunun donanıma bindirdiği 'gerçek' yükü temsil eder.
        this.efficiencyCoefficient = (currentMspt * players) / hardwareMs;
    }

    public void updateSnapshot(double currentMspt) {
        if (msptHistory.size() >= MAX_HISTORY) msptHistory.removeFirst();
        msptHistory.add(currentMspt);
    }

    /**
     * Gelecek tahmini yapar.
     * Formül: $\Delta MSPT / \Delta Time$ oranına göre 50ms eşiğine kalan süreyi bulur.
     */
    public PredictionResult predict() {
        if (msptHistory.size() < 5) return new PredictionResult(0, "Veri toplanıyor...");

        double first = msptHistory.getFirst();
        double last = msptHistory.getLast();

        // Trend hesaplama: Son veri ile ilk veri arasındaki fark / örneklem sayısı
        double trend = (last - first) / msptHistory.size();

        if (trend <= 0) return new PredictionResult(100, "Stabil");

        // 50ms (Kritik Lag Eşiği) noktasına ne kadar kaldı?
        double remainingMspt = 50.0 - last;
        double minutesToLag = remainingMspt / trend;

        return new PredictionResult(minutesToLag, trend > 0.5 ? "HIZLI ARTIŞ" : "YAVAŞ ARTIŞ");
    }

    public double getEfficiencyCoefficient() {
        return efficiencyCoefficient;
    }

    public static class PredictionResult {
        public final double minutesToLag;
        public final String status;

        public PredictionResult(double minutesToLag, String status) {
            this.minutesToLag = minutesToLag;
            this.status = status;
        }
    }
}