package com.mertout.benchmark.engine;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.List;

public class IslandHealthReport {

    /**
     * Ada için detaylı bir "Sağlık ve Performans" raporu oluşturur.
     * * @param island    SuperiorSkyblock2 Ada nesnesi
     * @param transfers IslandSurgeonCollector'dan gelen toplam transfer sayısı
     * @param entities  Kolektörden gelen anlık mob yükü
     * @param hoppers   Adadaki toplam statik huni sayısı
     * @return          Chat'e basılacak satırlar listesi
     */
    public static List<String> generate(Island island, int transfers, int entities, int hoppers) {
        List<String> report = new ArrayList<>();
        String owner = island.getOwner().getName();

        // --- 1. PUANLAMA ALGORİTMASI ---
        // Puanlama Mantığı: Transfer yoğunluğu (aktivite) statik sayıdan daha çok puan düşürür.
        double score = 100.0;
        score -= (transfers * 0.05); // Aktif huni trafiği cezası
        score -= (hoppers * 0.02);   // Statik huni sayısı cezası
        score -= (entities * 0.08);  // Mob yükü cezası
        score = Math.max(0, Math.min(100, score));

        // --- 2. BAŞLIK VE GENEL DURUM ---
        report.add(ChatColor.DARK_GRAY + "========================================");
        report.add(ChatColor.AQUA + "" + ChatColor.BOLD + "🩺 ADA CERRAHI ANALİZ RAPORU");
        report.add(ChatColor.GRAY + "Ada Sahibi: " + ChatColor.WHITE + owner);
        report.add(ChatColor.GRAY + "Sağlık Skoru: " + getScoreColor(score) + (int)score + "/100");
        report.add("");

        // --- 3. TEKNİK VERİLER (VİTAL BULGULAR) ---
        report.add(ChatColor.YELLOW + "Vital Bulgular:");
        report.add(ChatColor.GRAY + " - Aktif Transferler: " + ChatColor.WHITE + transfers + " (Son 1 dk)");
        report.add(ChatColor.GRAY + " - Toplam Huniler: " + ChatColor.WHITE + hoppers);
        report.add(ChatColor.GRAY + " - Mob Yoğunluğu: " + ChatColor.WHITE + entities);
        report.add("");

        // --- 4. CERRAHIN TEŞHİSİ VE TAVSİYESİ ---
        report.add(ChatColor.GOLD + "" + ChatColor.ITALIC + "Cerrahın Teşhisi:");

        boolean clean = true;
        if (transfers > 1000) {
            report.add(ChatColor.RED + " ● [!] " + ChatColor.GRAY + "Kritik transfer yoğunluğu! Huni saatlerini kontrol edin.");
            clean = false;
        }

        if (hoppers > 400) {
            report.add(ChatColor.RED + " ● [!] " + ChatColor.GRAY + "Huni sınırı zorlanıyor. Eşya taşıma için su kanalı kullanın.");
            clean = false;
        }

        if (entities > 250) {
            report.add(ChatColor.RED + " ● [!] " + ChatColor.GRAY + "Mob yoğunluğu yüksek. Sunucu AI hesaplaması yavaşlıyor.");
            clean = false;
        }

        if (clean && score > 80) {
            report.add(ChatColor.GREEN + " ● [✓] " + ChatColor.GRAY + "Ada mükemmel durumda. Herhangi bir tıkanıklık saptanmadı.");
        }

        report.add(ChatColor.DARK_GRAY + "========================================");
        return report;
    }

    private static ChatColor getScoreColor(double score) {
        if (score >= 85) return ChatColor.GREEN;
        if (score >= 65) return ChatColor.YELLOW;
        if (score >= 40) return ChatColor.GOLD;
        return ChatColor.RED;
    }
}