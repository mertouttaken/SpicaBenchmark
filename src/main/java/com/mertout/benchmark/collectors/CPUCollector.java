package com.mertout.benchmark.collectors;

import com.mertout.benchmark.api.MetricCollector;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

public class CPUCollector implements MetricCollector {
    private final OperatingSystemMXBean osBean;
    private final ThreadMXBean threadBean;
    private double processCpuLoad;
    private double systemCpuLoad;
    private long mainThreadCpuTime;

    public CPUCollector() {
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();
    }

    @Override
    public void collect() {
        this.processCpuLoad = osBean.getProcessCpuLoad() * 100; // Sunucunun kullandığı
        this.systemCpuLoad = osBean.getSystemCpuLoad() * 100;   // Makinenin toplam yükü

        // Main Thread'in CPU kullanım süresini (nanosaniye) alıyoruz
        // Bu değer, ana thread'in işlemciyi ne kadar "meşgul" ettiğini gösterir.
        this.mainThreadCpuTime = threadBean.getThreadCpuTime(1); // 1 genelde Main Thread ID'dir
    }

    @Override
    public double calculateScore() {
        // İşlemci yükü %80'in üzerindeyse puan hızla düşer
        double score = 100 - (processCpuLoad * 0.8);
        return Math.max(0, Math.min(100, score));
    }

    @Override
    public Map<String, Object> getRawMetrics() {
        Map<String, Object> m = new HashMap<>();
        m.put("ProcessUsage", String.format("%.2f%%", processCpuLoad));
        m.put("SystemTotal", String.format("%.2f%%", systemCpuLoad));
        m.put("Cores", Runtime.getRuntime().availableProcessors());
        return m;
    }

    @Override
    public String getName() { return "CPU System"; }
}