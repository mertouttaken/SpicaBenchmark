package com.mertout.benchmark.api;

import java.util.Map;

public interface MetricCollector {
    /**
     * Verileri toplar ve dahili değişkenleri günceller.
     * Mümkünse ana thread'i yormayacak şekilde tasarlanmalıdır.
     */
    void collect();

    /**
     * Toplanan ham verileri 0-100 arası bir puana dönüştürür.
     */
    double calculateScore();

    /**
     * Raporlama için ham veri haritasını döndürür.
     */
    Map<String, Object> getRawMetrics();

    /**
     * Kolektörün adını döndürür.
     */
    String getName();
}