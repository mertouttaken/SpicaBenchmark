package com.mertout.benchmark.engine;

import com.mertout.benchmark.SpicaBenchmark;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LogManager {

    private final SpicaBenchmark plugin;
    private final File logFolder;

    public LogManager(SpicaBenchmark plugin) {
        this.plugin = plugin;
        this.logFolder = new File(plugin.getDataFolder(), "logs");

        // Klasör yoksa oluştur
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
    }

    /**
     * Raporu asenkron olarak dosyaya kaydeder.
     */
    public void saveReportToFile(List<String> reportLines) {
        CompletableFuture.runAsync(() -> {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File logFile = new File(logFolder, "report_" + timeStamp + ".txt");

            try (FileWriter writer = new FileWriter(logFile)) {
                writer.write("=== SpicaBenchmark Otomatik Raporu ===\n");
                writer.write("Tarih: " + new Date().toString() + "\n\n");

                for (String line : reportLines) {
                    // ChatColor renk kodlarını (.txt olduğu için) temizliyoruz
                    writer.write(ChatColor.stripColor(line) + "\n");
                }

                plugin.getLogger().info("Otomatik performans raporu kaydedildi: " + logFile.getName());
            } catch (IOException e) {
                plugin.getLogger().severe("Rapor kaydedilirken hata oluştu: " + e.getMessage());
            }
        });
    }
}