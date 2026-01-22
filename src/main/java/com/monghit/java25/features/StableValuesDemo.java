package com.monghit.java25.features;

import org.springframework.stereotype.Service;
import java.lang.StableValue;

/**
 * Demo de Stable Values (JEP 572 - Preview)
 *
 * Stable Values proporcionan una API para inmutabilidad diferida.
 * Un StableValue puede ser creado sin un valor inicial y luego
 * establecerse exactamente una vez, después de lo cual es inmutable.
 *
 * Ventajas:
 * - Inicialización perezosa thread-safe
 * - Mejor rendimiento que volatile o AtomicReference
 * - Garantía de inmutabilidad después de establecer
 */
@Service
public class StableValuesDemo {

    // StableValue sin valor inicial
    private final StableValue<String> lazyConfig = StableValue.of();
    private final StableValue<DatabaseConnection> dbConnection = StableValue.of();

    /**
     * Ejemplo básico de StableValue
     */
    public String getLazyConfig() {
        // Obtener o inicializar
        return lazyConfig.orElseSet(() -> {
            // Esta función solo se ejecuta la primera vez
            System.out.println("Inicializando configuración...");
            return loadConfiguration();
        });
    }

    /**
     * Ejemplo con objeto complejo
     */
    public DatabaseConnection getConnection() {
        return dbConnection.orElseSet(() -> {
            System.out.println("Estableciendo conexión a BD...");
            return new DatabaseConnection("localhost", 5432);
        });
    }

    /**
     * Ejemplo de inicialización condicional
     */
    public String getOrSetValue(StableValue<String> stableValue, String newValue) {
        return stableValue.orElseSet(() -> {
            // Solo se ejecuta si no está establecido
            System.out.println("Estableciendo nuevo valor: " + newValue);
            return newValue;
        });
    }

    /**
     * Ejemplo con cálculo costoso
     */
    public ExpensiveResult getExpensiveResult() {
        StableValue<ExpensiveResult> result = StableValue.of();

        return result.orElseSet(() -> {
            System.out.println("Realizando cálculo costoso...");
            // Simular operación costosa
            sleep(1000);
            return new ExpensiveResult("resultado-complejo", 42);
        });
    }

    /**
     * Ejemplo de uso en contexto multi-thread
     * Múltiples threads pueden intentar establecer, pero solo uno lo hará
     */
    public void demonstrateThreadSafety() throws InterruptedException {
        StableValue<String> sharedValue = StableValue.of();

        Thread t1 = Thread.ofVirtual().start(() -> {
            String value = sharedValue.orElseSet(() -> {
                System.out.println("Thread 1 inicializando...");
                sleep(100);
                return "valor-thread-1";
            });
            System.out.println("Thread 1 obtuvo: " + value);
        });

        Thread t2 = Thread.ofVirtual().start(() -> {
            String value = sharedValue.orElseSet(() -> {
                System.out.println("Thread 2 inicializando...");
                sleep(100);
                return "valor-thread-2";
            });
            System.out.println("Thread 2 obtuvo: " + value);
        });

        t1.join();
        t2.join();

        // Ambos threads obtienen el mismo valor (el del primero que inicializó)
    }

    /**
     * Comparación con alternativas tradicionales
     */
    public void compareWithTraditionalApproaches() {
        // 1. Tradicional con volatile (no lazy, usa memoria)
        class VolatileExample {
            private volatile String config = loadConfiguration();
        }

        // 2. Double-checked locking (complejo, propenso a errores)
        class DoubleCheckedExample {
            private volatile String config;

            public String getConfig() {
                if (config == null) {
                    synchronized (this) {
                        if (config == null) {
                            config = loadConfiguration();
                        }
                    }
                }
                return config;
            }
        }

        // 3. StableValue (simple, seguro, eficiente)
        class StableValueExample {
            private final StableValue<String> config = StableValue.of();

            public String getConfig() {
                return config.orElseSet(() -> loadConfiguration());
            }
        }
    }

    // Métodos auxiliares

    private String loadConfiguration() {
        sleep(100);
        return "config-loaded-" + System.currentTimeMillis();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Clases auxiliares
    public record DatabaseConnection(String host, int port) {}

    public record ExpensiveResult(String data, int value) {}
}
