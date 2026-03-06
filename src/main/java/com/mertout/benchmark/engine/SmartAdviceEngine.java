package com.mertout.benchmark.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmartAdviceEngine {
    public static List<String> getSuggestions(String category, double score, Map<String, Object> raw) {
        List<String> advice = new ArrayList<>();

        if (category.equals("Automation System") && score < 60) {
            advice.add("§e[!] §fHopper yoğunluğu saptandı. §b'hopper-check: 8'§f ayarını spigot.yml'de yükseltin.");
        }

        if (category.equals("Entity System") && score < 50) {
            advice.add("§e[!] §fMob yığılması var. §b'mob-spawn-range'§f değerini 4'e düşürmeyi deneyin.");
        }

        if (category.equals("Chunk System") && score < 70) {
            advice.add("§e[!] §fChunk yükü fazla. §b'view-distance'§f değerini 6 veya altı yapın.");
        }

        return advice;
    }
}