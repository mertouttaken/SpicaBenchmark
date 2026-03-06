package com.mertout.benchmark.engine;

import com.mertout.benchmark.SpicaBenchmark;
import org.bukkit.ChatColor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class HardwareProfiler {

    private final SpicaBenchmark plugin;

    public HardwareProfiler(SpicaBenchmark plugin) {
        this.plugin = plugin;
    }

    // 1. Standart Single-Thread Testi (Prime Calculation)
    public CompletableFuture<Long> runSingleThreadTest() {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            int count = 0;
            for (int i = 2; i < 5_000_000; i++) {
                if (isPrime(i)) count++;
            }
            return System.currentTimeMillis() - start;
        });
    }

    // 2. Heavy Math Stress Test (Matrix Multiplication)
    // Complexity: $O(n^3)$
    public CompletableFuture<Long> runMatrixStressTest() {
        return CompletableFuture.supplyAsync(() -> {
            int size = 400;
            double[][] a = new double[size][size];
            double[][] b = new double[size][size];
            double[][] c = new double[size][size];

            long start = System.currentTimeMillis();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    for (int k = 0; k < size; k++) {
                        c[i][j] += a[i][k] * b[k][j];
                    }
                }
            }
            return System.currentTimeMillis() - start;
        });
    }

    // 3. Multi-Thread Stress Test
    // Tüm çekirdekleri aynı anda %100 yük altına sokar.
    public CompletableFuture<Long> runMultiThreadTest() {
        int cores = Runtime.getRuntime().availableProcessors();
        CompletableFuture<Long>[] futures = new CompletableFuture[cores];
        long start = System.currentTimeMillis();

        for (int i = 0; i < cores; i++) {
            futures[i] = runSingleThreadTest();
        }

        return CompletableFuture.allOf(futures).thenApply(v -> System.currentTimeMillis() - start);
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    public String getGrade(long singleMs, long matrixMs) {
        // Minecraft için en önemli değer singleMs'dir.
        if (singleMs < 800 && matrixMs < 500) return ChatColor.GREEN + "S+ (Ultra Performance)";
        if (singleMs < 1200) return ChatColor.AQUA + "A (High-End)";
        if (singleMs < 1800) return ChatColor.YELLOW + "B (Stable)";
        return ChatColor.RED + "F (Lags likely)";
    }
}