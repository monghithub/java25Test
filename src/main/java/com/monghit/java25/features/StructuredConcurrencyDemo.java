package com.monghit.java25.features;

import org.springframework.stereotype.Service;

/**
 * Demo de Structured Concurrency (JEP 505 - Preview en Java 25)
 *
 * NOTA IMPORTANTE: La API de StructuredTaskScope cambió significativamente en Java 25.
 * ShutdownOnFailure y ShutdownOnSuccess ya NO son clases estáticas internas.
 * StructuredTaskScope es ahora una sealed interface con una única implementación.
 *
 * Esta demo muestra ejemplos conceptuales. Para código funcional completo,
 * se requiere usar la nueva API de Java 25 que está en evolución.
 *
 * Referencia: https://rockthejvm.com/articles/structured-concurrency-jdk-25
 */
@Service
public class StructuredConcurrencyDemo {

    /**
     * NOTA: En Java 25, la API de StructuredTaskScope cambió.
     * Los ejemplos aquí son conceptuales hasta que la API se estabilice.
     *
     * La nueva API en Java 25:
     * - StructuredTaskScope es una sealed interface
     * - join() ahora lanza FailedException (unchecked) en lugar de ExecutionException
     * - Las políticas de concurrencia (ShutdownOnFailure, ShutdownOnSuccess) cambiaron
     */

    public String fetchUserDataWithFailure(String userId) throws Exception {
        // Ejemplo conceptual - la implementación requiere la nueva API de Java 25
        // En Java 25, se usa StructuredTaskScope con una estrategia diferente

        // Simulación simple para demostración
        String user = fetchUserProfile(userId);
        String orders = fetchUserOrders(userId);
        String preferences = fetchUserPreferences(userId);

        return String.format("User: %s, Orders: %s, Preferences: %s",
                user, orders, preferences);
    }

    public String fetchFromMultipleSources(String query) throws Exception {
        // En Java 25, la forma de hacer "shutdown on success" cambió
        // Este es un ejemplo simplificado

        return fetchFromCache(query);
    }

    public String fetchWithTimeout(String userId) throws Exception {
        // Ejemplo simplificado
        String result1 = slowOperation1(userId);
        String result2 = slowOperation2(userId);

        return result1 + " | " + result2;
    }

    public String processWithVirtualThreads(String data) throws Exception {
        // Ejemplo con virtual threads simple
        Thread t1 = Thread.ofVirtual().start(() -> processData1(data));
        Thread t2 = Thread.ofVirtual().start(() -> processData2(data));
        Thread t3 = Thread.ofVirtual().start(() -> processData3(data));

        t1.join();
        t2.join();
        t3.join();

        return String.format("Resultados: [%s, %s, %s]",
                processData1(data), processData2(data), processData3(data));
    }

    public Summary aggregateData(String category) throws Exception {
        // Ejemplo simplificado sin StructuredTaskScope
        int count = getCount(category);
        double sum = getSum(category);
        double avg = getAverage(category);
        int max = getMax(category);

        return new Summary(count, sum, avg, max);
    }

    // Métodos auxiliares de simulación

    private String fetchUserProfile(String userId) {
        sleep(100);
        return "Profile-" + userId;
    }

    private String fetchUserOrders(String userId) {
        sleep(150);
        return "Orders-" + userId;
    }

    private String fetchUserPreferences(String userId) {
        sleep(80);
        return "Preferences-" + userId;
    }

    private String fetchFromDatabase(String query) {
        sleep(200);
        return "DB-Result: " + query;
    }

    private String fetchFromCache(String query) {
        sleep(50);
        return "Cache-Result: " + query;
    }

    private String fetchFromAPI(String query) {
        sleep(300);
        return "API-Result: " + query;
    }

    private String slowOperation1(String userId) {
        sleep(1000);
        return "SlowOp1-" + userId;
    }

    private String slowOperation2(String userId) {
        sleep(1500);
        return "SlowOp2-" + userId;
    }

    private String processData1(String data) {
        return "Processed1-" + data;
    }

    private String processData2(String data) {
        return "Processed2-" + data;
    }

    private String processData3(String data) {
        return "Processed3-" + data;
    }

    private int getCount(String category) {
        sleep(100);
        return 42;
    }

    private double getSum(String category) {
        sleep(120);
        return 1234.56;
    }

    private double getAverage(String category) {
        sleep(90);
        return 29.39;
    }

    private int getMax(String category) {
        sleep(110);
        return 999;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Record para el ejemplo de agregación
    public record Summary(int count, double sum, double average, int max) {}
}
