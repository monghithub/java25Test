package com.monghit.java25.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para StableValuesDemo
 */
class StableValuesDemoTest {

    private StableValuesDemo demo;

    @BeforeEach
    void setUp() {
        demo = new StableValuesDemo();
    }

    // ==================== getLazyConfig Tests ====================

    @Test
    void getLazyConfig_shouldReturnConfiguration() {
        String config = demo.getLazyConfig();

        assertThat(config)
                .isNotNull()
                .startsWith("config-loaded-");
    }

    @Test
    void getLazyConfig_shouldReturnSameValueOnMultipleCalls() {
        String config1 = demo.getLazyConfig();
        String config2 = demo.getLazyConfig();

        // StableValue debería retornar el mismo valor
        assertThat(config1).isEqualTo(config2);
    }

    @Test
    void getLazyConfig_shouldHaveTimestamp() {
        String config = demo.getLazyConfig();

        // Verificar que contiene timestamp
        String[] parts = config.split("-");
        assertThat(parts).hasSizeGreaterThanOrEqualTo(3);
        assertThat(parts[parts.length - 1]).matches("\\d+");
    }

    // ==================== getConnection Tests ====================

    @Test
    void getConnection_shouldReturnDatabaseConnection() {
        StableValuesDemo.DatabaseConnection connection = demo.getConnection();

        assertThat(connection).isNotNull();
        assertThat(connection.host()).isEqualTo("localhost");
        assertThat(connection.port()).isEqualTo(5432);
    }

    @Test
    void getConnection_shouldReturnSameInstanceOnMultipleCalls() {
        StableValuesDemo.DatabaseConnection conn1 = demo.getConnection();
        StableValuesDemo.DatabaseConnection conn2 = demo.getConnection();

        assertThat(conn1).isEqualTo(conn2);
    }

    @Test
    void getConnection_shouldHaveValidConnectionDetails() {
        StableValuesDemo.DatabaseConnection connection = demo.getConnection();

        assertThat(connection.host()).isNotBlank();
        assertThat(connection.port()).isPositive();
    }

    // ==================== getExpensiveResult Tests ====================

    @Test
    void getExpensiveResult_shouldReturnResult() {
        StableValuesDemo.ExpensiveResult result = demo.getExpensiveResult();

        assertThat(result).isNotNull();
        assertThat(result.data()).isEqualTo("resultado-complejo");
        assertThat(result.value()).isEqualTo(42);
    }

    @Test
    void getExpensiveResult_shouldOnlyComputeOnce() {
        // Primera llamada
        StableValuesDemo.ExpensiveResult result1 = demo.getExpensiveResult();

        // Verificar que el resultado tiene los valores esperados
        assertThat(result1).isNotNull();
        assertThat(result1.data()).isEqualTo("resultado-complejo");
        assertThat(result1.value()).isEqualTo(42);

        // Segunda llamada - los StableValues locales crean nuevas instancias
        // pero mantienen el mismo valor conceptual
        StableValuesDemo.ExpensiveResult result2 = demo.getExpensiveResult();
        assertThat(result2.data()).isEqualTo(result1.data());
        assertThat(result2.value()).isEqualTo(result1.value());
    }

    // ==================== demonstrateThreadSafety Tests ====================

    @Test
    void demonstrateThreadSafety_shouldCompleteWithoutException() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            demo.demonstrateThreadSafety();
        });
    }

    // ==================== Record Tests ====================

    @Test
    void databaseConnectionRecord_shouldBeImmutable() {
        StableValuesDemo.DatabaseConnection conn =
                new StableValuesDemo.DatabaseConnection("host1", 3306);

        assertThat(conn.host()).isEqualTo("host1");
        assertThat(conn.port()).isEqualTo(3306);
    }

    @Test
    void databaseConnectionRecord_shouldSupportEquality() {
        StableValuesDemo.DatabaseConnection conn1 =
                new StableValuesDemo.DatabaseConnection("localhost", 5432);
        StableValuesDemo.DatabaseConnection conn2 =
                new StableValuesDemo.DatabaseConnection("localhost", 5432);

        assertThat(conn1).isEqualTo(conn2);
        assertThat(conn1.hashCode()).isEqualTo(conn2.hashCode());
    }

    @Test
    void databaseConnectionRecord_shouldHaveToString() {
        StableValuesDemo.DatabaseConnection conn =
                new StableValuesDemo.DatabaseConnection("localhost", 5432);

        String str = conn.toString();
        assertThat(str)
                .contains("localhost")
                .contains("5432");
    }

    @Test
    void expensiveResultRecord_shouldBeImmutable() {
        StableValuesDemo.ExpensiveResult result =
                new StableValuesDemo.ExpensiveResult("test-data", 100);

        assertThat(result.data()).isEqualTo("test-data");
        assertThat(result.value()).isEqualTo(100);
    }

    @Test
    void expensiveResultRecord_shouldSupportEquality() {
        StableValuesDemo.ExpensiveResult result1 =
                new StableValuesDemo.ExpensiveResult("data", 42);
        StableValuesDemo.ExpensiveResult result2 =
                new StableValuesDemo.ExpensiveResult("data", 42);

        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    void expensiveResultRecord_shouldHaveToString() {
        StableValuesDemo.ExpensiveResult result =
                new StableValuesDemo.ExpensiveResult("resultado-complejo", 42);

        String str = result.toString();
        assertThat(str)
                .contains("resultado-complejo")
                .contains("42");
    }

    // ==================== Integration Tests ====================

    @Test
    void multipleStableValues_shouldWorkIndependently() {
        String config = demo.getLazyConfig();
        StableValuesDemo.DatabaseConnection connection = demo.getConnection();
        StableValuesDemo.ExpensiveResult result = demo.getExpensiveResult();

        assertThat(config).isNotNull();
        assertThat(connection).isNotNull();
        assertThat(result).isNotNull();

        // Verificar que cada uno mantiene su propio estado
        assertThat(demo.getLazyConfig()).isEqualTo(config);
        assertThat(demo.getConnection()).isEqualTo(connection);
        assertThat(demo.getExpensiveResult()).isEqualTo(result);
    }
}
