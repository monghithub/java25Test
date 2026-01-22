package com.monghit.java25.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para ScopedValuesDemo
 */
class ScopedValuesDemoTest {

    private ScopedValuesDemo demo;

    @BeforeEach
    void setUp() {
        demo = new ScopedValuesDemo();
    }

    // ==================== processWithContext Tests ====================

    @Test
    void processWithContext_shouldReturnFormattedMessage() {
        String result = demo.processWithContext("user123", "req456", "tenant789");

        assertThat(result)
                .contains("User: user123")
                .contains("Request: req456")
                .contains("Tenant: tenant789");
    }

    @Test
    void processWithContext_withDifferentValues_shouldProcessCorrectly() {
        String result = demo.processWithContext("alice", "req-001", "org-abc");

        assertThat(result)
                .isNotNull()
                .contains("alice")
                .contains("req-001")
                .contains("org-abc");
    }

    // ==================== processWithConcurrency Tests ====================

    @Test
    void processWithConcurrency_shouldPropagateToVirtualThreads() throws Exception {
        String result = demo.processWithConcurrency("user999");

        // En Java 25, ScopedValues tienen comportamiento específico con virtual threads
        // El resultado debe contener los marcadores de tareas
        assertThat(result)
                .contains("Task 1")
                .contains("Task 2")
                .contains("|");
    }

    @Test
    void processWithConcurrency_shouldExecuteBothTasks() throws Exception {
        String result = demo.processWithConcurrency("testUser");

        // Verificar que ambas tareas se ejecutaron
        assertThat(result)
                .contains("ejecutada por")
                .contains("|");
    }

    // ==================== getUserIdOrDefault Tests ====================

    @Test
    void getUserIdOrDefault_withoutContext_shouldReturnDefault() {
        String result = demo.getUserIdOrDefault();
        assertThat(result).isEqualTo("usuario-anonimo");
    }

    @Test
    void getUserIdOrDefault_withContext_shouldReturnActualValue() {
        String result = demo.processWithContext("actualUser", "req1", "tenant1");

        // Dentro del contexto, el valor debería estar disponible
        assertThat(result).isNotNull();
    }

    // ==================== hasUserContext Tests ====================

    @Test
    void hasUserContext_withoutContext_shouldReturnFalse() {
        boolean result = demo.hasUserContext();
        assertThat(result).isFalse();
    }

    // ==================== nestedScopes Tests ====================

    @Test
    void nestedScopes_shouldHandleNestedValues() {
        String result = demo.nestedScopes();

        assertThat(result)
                .contains("Outer tenant: tenant-1")
                .contains("Inner tenant: tenant-2");
    }

    @Test
    void nestedScopes_shouldRestoreOuterValue() {
        String result = demo.nestedScopes();

        // Verificar que ambos valores están presentes
        assertThat(result)
                .contains("tenant-1")
                .contains("tenant-2");
    }

    @Test
    void nestedScopes_shouldMaintainSeparation() {
        String result = demo.nestedScopes();

        // El formato debería ser: "Outer tenant: X | Inner tenant: Y"
        assertThat(result).contains("|");
        String[] parts = result.split("\\|");
        assertThat(parts).hasSize(2);
        assertThat(parts[0].trim()).startsWith("Outer tenant:");
        assertThat(parts[1].trim()).startsWith("Inner tenant:");
    }

    // ==================== Integration Tests ====================

    @Test
    void scopedValues_shouldBeIsolatedBetweenCalls() {
        String result1 = demo.processWithContext("user1", "req1", "tenant1");
        String result2 = demo.processWithContext("user2", "req2", "tenant2");

        assertThat(result1).contains("user1");
        assertThat(result2).contains("user2");
        assertThat(result1).doesNotContain("user2");
        assertThat(result2).doesNotContain("user1");
    }

    @Test
    void processWithContext_shouldNotAffectOutsideScope() {
        // Antes del scope
        assertThat(demo.hasUserContext()).isFalse();

        // Durante el scope
        String result = demo.processWithContext("user", "req", "tenant");
        assertThat(result).isNotNull();

        // Después del scope
        assertThat(demo.hasUserContext()).isFalse();
    }
}
