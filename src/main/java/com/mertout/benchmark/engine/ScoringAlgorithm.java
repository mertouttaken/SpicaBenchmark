package com.mertout.benchmark.engine;

import com.mertout.benchmark.api.PerformanceMetric;
import java.util.Map;

public class ScoringAlgorithm {
    public static double calculateGlobalScore(Map<String, PerformanceMetric> metrics) {
        double totalWeightedScore = 0;
        double totalWeight = 0;

        for (PerformanceMetric metric : metrics.values()) {
            totalWeightedScore += metric.getWeightedScore();
            totalWeight += metric.getWeight();
        }

        return totalWeight == 0 ? 0 : totalWeightedScore / totalWeight;
    }
}