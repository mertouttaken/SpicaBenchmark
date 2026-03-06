package com.mertout.benchmark.api;

public class PerformanceMetric {
    private final String name;
    private double rawValue;
    private double score; // 0-100
    private final double weight; // Global skora etkisi

    public PerformanceMetric(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }

    // Getters, Setters and Score calculation logic
    public void setRawValue(double value) { this.rawValue = value; }
    public double getWeightedScore() { return score * weight; }
    public void setScore(double score) { this.score = score; }
    public double getWeight() { return weight; }
}