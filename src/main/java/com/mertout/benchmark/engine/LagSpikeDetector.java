package com.mertout.benchmark.engine;

import com.mertout.benchmark.SpicaBenchmark;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class LagSpikeDetector extends Thread {

    private final SpicaBenchmark plugin;
    private final AtomicLong lastTickTime = new AtomicLong(System.currentTimeMillis());
    private final long thresholdMs = 100; // 100ms üzerindeki gecikmeleri yakala
    private boolean running = true;

    public LagSpikeDetector(SpicaBenchmark plugin) {
        super("Spica-Watchdog");
        this.plugin = plugin;
    }

    // Ana thread her tick başında bu metodu çağırır
    public void tickSign() {
        lastTickTime.set(System.currentTimeMillis());
    }

    @Override
    public void run() {
        while (running) {
            long now = System.currentTimeMillis();
            long lastTick = lastTickTime.get();
            long delta = now - lastTick;

            if (delta > thresholdMs) {
                // LAG SPIKE TESPİT EDİLDİ!
                handleSpike(delta);

                // Aynı spike için üst üste rapor vermemek adına thread'i bir süre beklet
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
                lastTickTime.set(System.currentTimeMillis());
            }

            try {
                Thread.sleep(20); // 20ms'de bir kontrol et (Hassas takip)
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    private void handleSpike(long duration) {
        // Ana thread'in o an ne yaptığını yakala (Stack Trace)
        Thread mainThread = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> t.getName().equals("main"))
                .findFirst()
                .orElse(null);

        if (mainThread != null) {
            StackTraceElement[] stackTrace = mainThread.getStackTrace();

            // Konsola detaylı log bas
            plugin.getLogger().warning("!!! LAG SPIKE TESPİT EDİLDİ !!!");
            plugin.getLogger().warning("Süre: " + duration + "ms");
            plugin.getLogger().warning("Lagın Kaynağı (Son Çağrılar):");

            // İlk 5 satırı yazdır (Genelde lagın olduğu yer burasıdır)
            for (int i = 0; i < Math.min(stackTrace.length, 8); i++) {
                plugin.getLogger().warning("  at " + stackTrace[i].toString());
            }

            // Op olanlara haber ver
            Bukkit.getOnlinePlayers().stream().filter(p -> p.isOp()).forEach(p -> {
                p.sendMessage(ChatColor.RED + "[Spica] " + ChatColor.YELLOW + "Ani lag tespit edildi: " +
                        ChatColor.WHITE + duration + "ms. Detaylar konsolda!");
            });
        }
    }

    public void stopDetector() {
        this.running = false;
        this.interrupt();
    }
}