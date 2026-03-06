package com.mertout.benchmark.collectors;

import com.mertout.benchmark.api.MetricCollector;
import org.bukkit.Bukkit;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class ThreadGhostCollector implements MetricCollector {

    private final ThreadMXBean threadBean;
    private final long mainThreadId;

    private String ghostThreadName = "None";
    private String suspectedPlugin = "None";
    private long blockedTimeMs = 0;
    private double contentionScore = 100;

    public ThreadGhostCollector() {
        this.threadBean = ManagementFactory.getThreadMXBean();
        // Ana thread ID'sini bul (Genelde 1'dir ama garantilemek iyidir)
        this.mainThreadId = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> t.getName().equals("main"))
                .map(Thread::getId)
                .findFirst().orElse(1L);

        // Thread izleme özelliklerini aktif et (Bazı JVM'lerde kapalı olabilir)
        if (threadBean.isThreadContentionMonitoringSupported()) {
            threadBean.setThreadContentionMonitoringEnabled(true);
        }
    }

    @Override
    public void collect() {
        ThreadInfo mainThreadInfo = threadBean.getThreadInfo(mainThreadId, 10);
        if (mainThreadInfo == null) return;

        // Ana thread bir kilit yüzünden engellenmiş mi?
        long lockOwnerId = mainThreadInfo.getLockOwnerId();
        this.blockedTimeMs = mainThreadInfo.getBlockedTime();

        if (lockOwnerId != -1) {
            ThreadInfo lockOwnerInfo = threadBean.getThreadInfo(lockOwnerId, 20);
            if (lockOwnerInfo != null) {
                this.ghostThreadName = lockOwnerInfo.getThreadName();
                this.suspectedPlugin = identifyPluginFromStackTrace(lockOwnerInfo.getStackTrace());
            }
        } else {
            this.ghostThreadName = "None";
            this.suspectedPlugin = "None";
        }
    }

    private String identifyPluginFromStackTrace(StackTraceElement[] stackTrace) {
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName().toLowerCase();
            // Standart kütüphaneleri filtrele, plugin paketlerini ara
            if (className.contains("com.mertout")) continue; // Kendi eklentimiz değilse
            if (className.contains("net.minecraft") || className.contains("org.bukkit") || className.contains("java.")) continue;

            // İlk bulduğun yabancı paketi şüpheli olarak işaretle
            String[] parts = element.getClassName().split("\\.");
            return parts.length > 1 ? parts[0] + "." + parts[1] : element.getClassName();
        }
        return "Unknown Async Task";
    }

    @Override
    public double calculateScore() {
        // Engellenme süresi arttıkça puan düşer.
        // 500ms üzerinde engellenme sunucu için felakettir.
        this.contentionScore = Math.max(0, 100 - (blockedTimeMs / 10.0));
        return contentionScore;
    }

    @Override
    public Map<String, Object> getRawMetrics() {
        Map<String, Object> m = new HashMap<>();
        m.put("GhostThread", ghostThreadName);
        m.put("SuspectedPlugin", suspectedPlugin);
        m.put("BlockedTimeMs", blockedTimeMs);
        return m;
    }

    @Override
    public String getName() { return "Thread Ghost"; }
}