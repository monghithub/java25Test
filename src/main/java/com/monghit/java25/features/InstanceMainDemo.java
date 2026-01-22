package com.monghit.java25.features;

/**
 * Demo de Instance Main Methods (JEP 495 - Final)
 *
 * Java 25 permite escribir métodos main de instancia, simplificando
 * el código para principiantes y casos simples.
 *
 * Evoluciones del método main:
 * 1. Traditional: public static void main(String[] args)
 * 2. Simplified: void main()  (sin modificadores)
 * 3. Instance: void main() como método de instancia
 */
public class InstanceMainDemo {

    private String message = "¡Hola desde Instance Main!";
    private int counter = 0;

    /**
     * Instance main method - Java 25
     * No necesita ser static, puede acceder a campos de instancia
     */
    void main() {
        System.out.println("=== Instance Main Demo ===");
        System.out.println(message);
        incrementCounter();
        System.out.println("Counter: " + counter);
        demonstrateInstanceAccess();
    }

    private void incrementCounter() {
        counter++;
    }

    private void demonstrateInstanceAccess() {
        // Puede llamar a métodos de instancia directamente
        System.out.println("Acceso a métodos de instancia sin problema");
    }

    /**
     * También puede recibir argumentos
     */
    void mainWithArgs(String[] args) {
        System.out.println("Argumentos recibidos: " + String.join(", ", args));
    }
}
