package com.mertout.benchmark.commands;

import com.mertout.benchmark.SpicaBenchmark;
import com.mertout.benchmark.collectors.island.IslandSurgeonCollector;
import com.mertout.benchmark.engine.HardwareProfiler;
import com.mertout.benchmark.engine.ReportGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkCommand implements CommandExecutor {
    private final SpicaBenchmark plugin;

    public BenchmarkCommand(SpicaBenchmark plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("spica.admin")) {
            sender.sendMessage(ChatColor.RED + "Yetkiniz yok!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                plugin.getEngine().startBenchmark();
                sender.sendMessage(ChatColor.GREEN + "Benchmark analizi başlatıldı...");
                break;
            case "stop":
                plugin.getEngine().stopBenchmark();
                sender.sendMessage(ChatColor.YELLOW + "Benchmark durduruldu.");
                break;
            case "report":
                if (!plugin.getEngine().isActive()) {
                    sender.sendMessage(ChatColor.RED + "Hata: Önce benchmarkı başlatmalısınız!");
                    sender.sendMessage(ChatColor.GRAY + "Kullanım: /benchmark start");
                    return true;
                }

                ReportGenerator generator = new ReportGenerator(plugin);
                generator.generate().forEach(sender::sendMessage);
                break;
            case "testcpu":
                sender.sendMessage(ChatColor.GOLD + "--- Spica Hardware Stress Test Başladı ---");
                sender.sendMessage(ChatColor.GRAY + "İşlemci limitleri zorlanıyor, lütfen bekleyin...");

                HardwareProfiler profiler = new HardwareProfiler(plugin);

                profiler.runSingleThreadTest().thenCombine(profiler.runMatrixStressTest(), (singleMs, matrixMs) -> {
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.YELLOW + ">> Single-Thread Score: " + ChatColor.WHITE + singleMs + "ms");
                    sender.sendMessage(ChatColor.YELLOW + ">> Matrix Math Score: " + ChatColor.WHITE + matrixMs + "ms");

                    profiler.runMultiThreadTest().thenAccept(multiMs -> {
                        sender.sendMessage(ChatColor.YELLOW + ">> Multi-Thread (All Cores): " + ChatColor.WHITE + multiMs + "ms");
                        sender.sendMessage("");
                        sender.sendMessage(ChatColor.GOLD + "SUNUCU KALİTE NOTU: " + profiler.getGrade(singleMs, matrixMs));
                        sender.sendMessage(ChatColor.GRAY + "---------------------------------------");
                    });
                    return null;
                });
                break;
            case "island":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Kullanım: /benchmark island <oyuncu>");
                    return true;
                }

                String targetName = args[1];
                // OfflinePlayer kullanımı 1.16.5 için en sağlıklısıdır
                org.bukkit.OfflinePlayer targetPlayer = org.bukkit.Bukkit.getOfflinePlayer(targetName);

                // 1. DOĞRU API KULLANIMI: Oyuncudan adayı buluyoruz
                com.bgsoftware.superiorskyblock.api.island.Island island =
                        com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI.getIslandByUUID(targetPlayer.getUniqueId());

                if (island == null) {
                    sender.sendMessage(ChatColor.RED + "Hata: '" + targetName + "' oyuncusunun bir adası bulunamadı!");
                    return true;
                }

                // 2. IslandSurgeonCollector'ı çekiyoruz
                // ÖNEMLİ: Paket yolunun (com.mertout.benchmark.collectors.island) doğru olduğundan emin ol
                IslandSurgeonCollector surgeon = (IslandSurgeonCollector)
                        plugin.getCollectors().stream()
                                .filter(c -> c instanceof IslandSurgeonCollector)
                                .findFirst()
                                .orElse(null);

                if (surgeon != null) {
                    // Canlı verileri çekiyoruz (Metot isimlerini Collector sınıfınla eşitledik)
                    int liveTransfers = surgeon.getTransfers(island.getUniqueId());

                    int liveSpawnActivity = surgeon.getSpawnActivity(island.getUniqueId());

                    // 3. Statik Huni Taraması (Chunk bazlı)
                    int totalHoppers = 0;
                    for (org.bukkit.Chunk chunk : island.getAllChunks()) {
                        if (chunk.isLoaded()) {
                            for (org.bukkit.block.BlockState state : chunk.getTileEntities()) {
                                if (state instanceof org.bukkit.block.Hopper) totalHoppers++;
                            }
                        }
                    }

                    // 4. Raporu Oluştur ve Gönder
                    List<String> report = com.mertout.benchmark.engine.IslandHealthReport.generate(
                            island,
                            liveTransfers,
                            liveSpawnActivity, // Collector'dan gelen veri
                            totalHoppers       // Chunklardan saydığımız veri
                    );

                    report.forEach(sender::sendMessage);
                } else {
                    sender.sendMessage(ChatColor.RED + "Hata: IslandSurgeonCollector sistemde kayıtlı değil!");
                }
                break;
            case "heatmap":
                if(sender instanceof Player) showHeatmap((Player) sender);
            default:
                sendHelp(sender);
        }
        return true;
    }
    public void showHeatmap(Player player) {
        player.sendMessage(ChatColor.AQUA + "--- Sunucu Lag Haritası (Hotspots) ---");
        player.getWorld().getLoadedChunks();

        // Basit bir örnek: En çok entity olan 3 chunk'ı bul
        List<Chunk> chunks = new ArrayList<>(java.util.Arrays.asList(player.getWorld().getLoadedChunks()));
        chunks.sort((c1, c2) -> Integer.compare(c2.getEntities().length, c1.getEntities().length));

        for (int i = 0; i < Math.min(chunks.size(), 3); i++) {
            org.bukkit.Chunk c = chunks.get(i);
            player.sendMessage(ChatColor.YELLOW + "Pik Nokta " + (i+1) + ": " +
                    ChatColor.WHITE + "X:" + (c.getX() << 4) + " Z:" + (c.getZ() << 4) +
                    ChatColor.RED + " (" + c.getEntities().length + " Entities)");
        }
    }
    private void sendHelp(CommandSender s) {
        s.sendMessage(ChatColor.GOLD + "/benchmark <start|stop|report|heatmap>");
    }
}