package com.monghit.java25.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para StructuredConcurrencyDemo
 */
class StructuredConcurrencyDemoTest {

    private StructuredConcurrencyDemo demo;

    @BeforeEach
    void setUp() {
        demo = new StructuredConcurrencyDemo();
    }

    // ==================== fetchUserDataWithFailure Tests ====================

    @Test
    void fetchUserDataWithFailure_shouldFetchAllData() throws Exception {
        String result = demo.fetchUserDataWithFailure("user123");

        assertThat(result)
                .contains("User:")
                .contains("Orders:")
                .contains("Preferences:")
                .contains("user123");
    }

    @Test
    void fetchUserDataWithFailure_shouldContainAllComponents() throws Exception {
        String result = demo.fetchUserDataWithFailure("testUser");

        assertThat(result)
                .contains("Profile-testUser")
                .contains("Orders-testUser")
                .contains("Preferences-testUser");
    }

    @Test
    void fetchUserDataWithFailure_shouldHandleDifferentUserIds() throws Exception {
        String result1 = demo.fetchUserDataWithFailure("alice");
        String result2 = demo.fetchUserDataWithFailure("bob");

        assertThat(result1).contains("alice");
        assertThat(result2).contains("bob");
        assertThat(result1).doesNotContain("bob");
    }

    // ==================== fetchFromMultipleSources Tests ====================

    @Test
    void fetchFromMultipleSources_shouldReturnCacheResult() throws Exception {
        String result = demo.fetchFromMultipleSources("testQuery");

        assertThat(result)
                .contains("Cache-Result")
                .contains("testQuery");
    }

    @Test
    void fetchFromMultipleSources_shouldHandleDifferentQueries() throws Exception {
        String result1 = demo.fetchFromMultipleSources("query1");
        String result2 = demo.fetchFromMultipleSources("query2");

        assertThat(result1).contains("query1");
        assertThat(result2).contains("query2");
    }

    // ==================== fetchWithTimeout Tests ====================

    @Test
    void fetchWithTimeout_shouldReturnBothOperations() throws Exception {
        String result = demo.fetchWithTimeout("user456");

        assertThat(result)
                .contains("SlowOp1-user456")
                .contains("SlowOp2-user456")
                .contains("|");
    }

    @Test
    void fetchWithTimeout_shouldFormatCorrectly() throws Exception {
        String result = demo.fetchWithTimeout("test");

        String[] parts = result.split("\\|");
        assertThat(parts).hasSize(2);
        assertThat(parts[0].trim()).startsWith("SlowOp1");
        assertThat(parts[1].trim()).startsWith("SlowOp2");
    }

    // ==================== processWithVirtualThreads Tests ====================

    @Test
    void processWithVirtualThreads_shouldProcessAllData() throws Exception {
        String result = demo.processWithVirtualThreads("testData");

        assertThat(result)
                .contains("Resultados:")
                .contains("Processed1-testData")
                .contains("Processed2-testData")
                .contains("Processed3-testData");
    }

    @Test
    void processWithVirtualThreads_shouldFormatAsArray() throws Exception {
        String result = demo.processWithVirtualThreads("data");

        assertThat(result)
                .startsWith("Resultados: [")
                .contains("]");
    }

    @Test
    void processWithVirtualThreads_shouldHandleDifferentInputs() throws Exception {
        String result1 = demo.processWithVirtualThreads("input1");
        String result2 = demo.processWithVirtualThreads("input2");

        assertThat(result1).contains("input1");
        assertThat(result2).contains("input2");
    }

    // ==================== aggregateData Tests ====================

    @Test
    void aggregateData_shouldReturnValidSummary() throws Exception {
        StructuredConcurrencyDemo.Summary summary = demo.aggregateData("testCategory");

        assertThat(summary).isNotNull();
        assertThat(summary.count()).isEqualTo(42);
        assertThat(summary.sum()).isEqualTo(1234.56);
        assertThat(summary.average()).isEqualTo(29.39);
        assertThat(summary.max()).isEqualTo(999);
    }

    @Test
    void aggregateData_shouldReturnConsistentResults() throws Exception {
        StructuredConcurrencyDemo.Summary summary1 = demo.aggregateData("cat1");
        StructuredConcurrencyDemo.Summary summary2 = demo.aggregateData("cat2");

        // Los valores deberían ser los mismos (simulación)
        assertThat(summary1.count()).isEqualTo(summary2.count());
        assertThat(summary1.sum()).isEqualTo(summary2.sum());
    }

    @Test
    void aggregateData_summaryRecord_shouldHaveCorrectValues() throws Exception {
        StructuredConcurrencyDemo.Summary summary = demo.aggregateData("test");

        assertThat(summary.count()).isPositive();
        assertThat(summary.sum()).isPositive();
        assertThat(summary.average()).isPositive();
        assertThat(summary.max()).isPositive();
    }

    // ==================== Record Tests ====================

    @Test
    void summaryRecord_shouldBeImmutable() {
        StructuredConcurrencyDemo.Summary summary =
                new StructuredConcurrencyDemo.Summary(10, 100.0, 10.0, 50);

        assertThat(summary.count()).isEqualTo(10);
        assertThat(summary.sum()).isEqualTo(100.0);
        assertThat(summary.average()).isEqualTo(10.0);
        assertThat(summary.max()).isEqualTo(50);
    }

    @Test
    void summaryRecord_shouldSupportEquality() {
        StructuredConcurrencyDemo.Summary summary1 =
                new StructuredConcurrencyDemo.Summary(42, 1234.56, 29.39, 999);
        StructuredConcurrencyDemo.Summary summary2 =
                new StructuredConcurrencyDemo.Summary(42, 1234.56, 29.39, 999);

        assertThat(summary1).isEqualTo(summary2);
        assertThat(summary1.hashCode()).isEqualTo(summary2.hashCode());
    }

    @Test
    void summaryRecord_shouldHaveWorkingToString() {
        StructuredConcurrencyDemo.Summary summary =
                new StructuredConcurrencyDemo.Summary(42, 1234.56, 29.39, 999);

        String str = summary.toString();
        assertThat(str)
                .contains("42")
                .contains("1234.56")
                .contains("29.39")
                .contains("999");
    }
}
