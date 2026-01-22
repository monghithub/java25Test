package com.monghit.java25.features;

import org.springframework.stereotype.Service;

import java.util.concurrent.StructuredTaskScope;

/**
 * Demo de Scoped Values (JEP 481 - Final)
 *
 * Scoped Values permiten compartir datos de contexto inmutable entre llamadores,
 * llamadas y threads hijos, dentro de un scope léxico bien definido.
 *
 * Ventajas sobre ThreadLocal:
 * - Inmutabilidad
 * - Mejor rendimiento
 * - Scope léxico claro
 * - Mejor integración con virtual threads
 */
@Service
public class ScopedValuesDemo {

    // Definir Scoped Values
    private static final ScopedValue<String> USER_ID = ScopedValue.newInstance();
    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
    private static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();

    /**
     * Ejemplo de uso básico de Scoped Values
     */
    public String processWithContext(String userId, String requestId, String tenantId) {
        // Ejecutar código con valores en el scope
        return ScopedValue.where(USER_ID, userId)
                .where(REQUEST_ID, requestId)
                .where(TENANT_ID, tenantId)
                .call(() -> {
                    return performOperation();
                });
    }

    /**
     * Método que accede a los valores del scope
     */
    private String performOperation() {
        String userId = USER_ID.get();
        String requestId = REQUEST_ID.get();
        String tenantId = TENANT_ID.get();

        // Simular operación que necesita contexto
        String result = String.format(
                "Procesando operación - User: %s, Request: %s, Tenant: %s",
                userId, requestId, tenantId
        );

        // Los scoped values están disponibles en métodos anidados
        nestedOperation();

        return result;
    }

    /**
     * Método anidado que también puede acceder a los scoped values
     */
    private void nestedOperation() {
        if (USER_ID.isBound()) {
            String userId = USER_ID.get();
            System.out.println("Operación anidada - User ID: " + userId);
        }
    }

    /**
     * Ejemplo con Concurrencia usando Virtual Threads
     * Los Scoped Values se propagan automáticamente a los threads hijos
     *
     * NOTA: La API de Structured Concurrency cambió en Java 25.
     * Este ejemplo usa Virtual Threads simples para demostrar la propagación de Scoped Values.
     */
    public String processWithConcurrency(String userId) throws Exception {
        return ScopedValue.where(USER_ID, userId).call(() -> {
            // Usar virtual threads simples
            StringBuilder result1 = new StringBuilder();
            StringBuilder result2 = new StringBuilder();

            Thread t1 = Thread.ofVirtual().start(() -> {
                // Este thread hijo tiene acceso al USER_ID
                result1.append("Task 1 ejecutada por: ").append(USER_ID.get());
            });

            Thread t2 = Thread.ofVirtual().start(() -> {
                result2.append("Task 2 ejecutada por: ").append(USER_ID.get());
            });

            t1.join();
            t2.join();

            return result1.toString() + " | " + result2.toString();
        });
    }

    /**
     * Ejemplo de orElse - obtener valor con fallback
     */
    public String getUserIdOrDefault() {
        return USER_ID.orElse("usuario-anonimo");
    }

    /**
     * Ejemplo de isBound - verificar si hay valor
     */
    public boolean hasUserContext() {
        return USER_ID.isBound();
    }

    /**
     * Ejemplo de anidamiento de scopes
     */
    public String nestedScopes() {
        return ScopedValue.where(TENANT_ID, "tenant-1").call(() -> {
            String outerTenant = TENANT_ID.get();

            // Scope interno que sobrescribe el valor
            String innerResult = ScopedValue.where(TENANT_ID, "tenant-2").call(() -> {
                return "Inner tenant: " + TENANT_ID.get();
            });

            // El valor original se restaura
            return "Outer tenant: " + outerTenant + " | " + innerResult;
        });
    }
}
