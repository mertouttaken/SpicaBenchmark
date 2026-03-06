package com.mertout.benchmark;

import com.mertout.benchmark.api.MetricCollector;
import com.mertout.benchmark.collectors.*;
import com.mertout.benchmark.collectors.island.IslandSurgeonCollector;
import com.mertout.benchmark.commands.BenchmarkCommand;
import com.mertout.benchmark.engine.BenchmarkEngine;
import com.mertout.benchmark.engine.LagSpikeDetector;
import com.mertout.benchmark.engine.LogManager;
import com.mertout.benchmark.engine.ReportGenerator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

public class SpicaBenchmark extends JavaPlugin {
    private static SpicaBenchmark instance;
    private final List<MetricCollector> collectors = new ArrayList<>();
    private BenchmarkEngine engine;
    private LagSpikeDetector watchdog;

    @Override
    public void onEnable() {
        instance = this;

        // --- 1. ÖZEL KOLEKTÖRLERİN BAŞLATILMASI ---

        // TickCollector (Kalp Atışı Ayarı)
        TickCollector tickCollector = new TickCollector();

        // NetworkCollector (Netty Injection)
        NetworkCollector netCollector = new NetworkCollector();
        Bukkit.getPluginManager().registerEvents(netCollector, this);

        // HopperTransferCollector (Event Dinleyici)
        HopperTransferCollector hopperTransfer = new HopperTransferCollector();
        Bukkit.getPluginManager().registerEvents(hopperTransfer, this);

        // IslandSurgeonCollector (SuperiorSkyblock Entegrasyonu)
        IslandSurgeonCollector surgeon = new IslandSurgeonCollector();
        Bukkit.getPluginManager().registerEvents(surgeon, this);

        ChunkThrashCollector thrashDetector = new ChunkThrashCollector();
        Bukkit.getPluginManager().registerEvents(thrashDetector, this);
        collectors.add(thrashDetector);

        collectors.add(new AITrafficCollector());

        // --- 2. KOLEKTÖR LİSTESİNE KAYIT ---
        collectors.add(tickCollector);
        collectors.add(new CPUCollector());
        collectors.add(new EntityCollector());
        collectors.add(new ChunkCollector());
        collectors.add(netCollector);
        collectors.add(new AutomationCollector());
        collectors.add(new PluginProfiler());
        collectors.add(hopperTransfer);
        collectors.add(new ThreadGhostCollector());
        collectors.add(surgeon);

        // --- 3. MOTOR VE SİSTEMLERİN BAŞLATILMASI ---
        this.engine = new BenchmarkEngine(this);
        this.watchdog = new LagSpikeDetector(this);
        this.watchdog.start();

        // --- 4. SCHEDULER GÖREVLERİ (KRİTİK!) ---

        // A. Tick Monitoring (MSPT ölçümü için her tick çalışmalı)
        Bukkit.getScheduler().runTaskTimer(this, tickCollector::tick, 1L, 1L);

        // B. Watchdog Tick Sign (Ana thread donma kontrolü)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (watchdog != null) watchdog.tickSign();
        }, 1L, 1L);

        // --- 5. KOMUT VE LOG ---
        getCommand("benchmark").setExecutor(new BenchmarkCommand(this));

        showStartupMessage();
    }

    private void showStartupMessage() {
        getLogger().info("§8========================================");
        getLogger().info("§6 SpicaBenchmark §fv1.0.0 §aAktif!");
        getLogger().info("§7 - Modüller: §b" + collectors.size() + " adet");
        getLogger().info("§7 - Watchdog: §2ÇALIŞIYOR");
        getLogger().info("§7 - Ada Cerrahı: §2BAĞLANDI (SS2)");
        getLogger().info("§8========================================");
    }

    @Override
    public void onDisable() {
        if (engine != null && engine.isActive()) {
            getLogger().info("Kapanış öncesi son rapor alınıyor...");
            ReportGenerator generator = new ReportGenerator(this);
            new LogManager(this).saveReportToFile(generator.generate());
            engine.stopBenchmark();
        }

        if (watchdog != null) {
            watchdog.stopDetector();
        }
    }

    public static SpicaBenchmark getInstance() { return instance; }
    public List<MetricCollector> getCollectors() { return collectors; }
    public BenchmarkEngine getEngine() { return engine; }
}