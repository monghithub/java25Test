package com.monghit.java25.controller;

import com.monghit.java25.features.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración del controller usando @WebMvcTest.
 * Compatible con Spring Boot 4.0 testing APIs.
 */
@WebMvcTest(Java25FeaturesController.class)
@DisplayName("Java25FeaturesController Tests")
class Java25FeaturesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrimitivePatternMatchingDemo primitivePatternDemo;

    @MockitoBean
    private ScopedValuesDemo scopedValuesDemo;

    @MockitoBean
    private StructuredConcurrencyDemo structuredConcurrencyDemo;

    @MockitoBean
    private StableValuesDemo stableValuesDemo;

    @MockitoBean
    private ModuleImportDemo moduleImportDemo;

    @Nested
    @DisplayName("Root Endpoint Tests")
    class RootEndpointTests {

        @Test
        @DisplayName("GET /api/java25 should return features overview")
        void getFeatures_shouldReturnFeaturesOverview() throws Exception {
            mockMvc.perform(get("/api/java25"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Java 25 New Features Demo API"))
                    .andExpect(jsonPath("$.features").isArray())
                    .andExpect(jsonPath("$.features", hasSize(8)))
                    .andExpect(jsonPath("$.endpoints").isMap())
                    .andExpect(jsonPath("$.endpoints['primitive-patterns']").value("/api/java25/primitive-patterns"))
                    .andExpect(jsonPath("$.endpoints['scoped-values']").value("/api/java25/scoped-values"));
        }

        @Test
        @DisplayName("GET /api/java25/health should return health status")
        void health_shouldReturnHealthStatus() throws Exception {
            mockMvc.perform(get("/api/java25/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.javaVersion").exists())
                    .andExpect(jsonPath("$.application").value("Java 25 Features Demo"));
        }
    }

    @Nested
    @DisplayName("Primitive Pattern Matching Endpoints")
    class PrimitivePatternMatchingTests {

        @Test
        @DisplayName("POST /api/java25/primitive-patterns/process should process primitive value")
        void processPrimitive_shouldProcessValue() throws Exception {
            when(primitivePatternDemo.processPrimitive(any())).thenReturn("Large integer: 100");

            mockMvc.perform(post("/api/java25/primitive-patterns/process")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("100"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Large integer: 100"));
        }

        @Test
        @DisplayName("POST /api/java25/primitive-patterns/check-type should check primitive type")
        void checkPrimitiveType_shouldCheckType() throws Exception {
            when(primitivePatternDemo.checkPrimitiveType(any())).thenReturn("Type: Integer");

            mockMvc.perform(post("/api/java25/primitive-patterns/check-type")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("42"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Type: Integer"));
        }

        @Test
        @DisplayName("POST /api/java25/primitive-patterns/convert should convert to int")
        void convertToInt_shouldConvertValue() throws Exception {
            when(primitivePatternDemo.safeConvertToInt(any())).thenReturn(42);

            mockMvc.perform(post("/api/java25/primitive-patterns/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("\"42\""))
                    .andExpect(status().isOk())
                    .andExpect(content().string("42"));
        }

        @Test
        @DisplayName("POST /api/java25/primitive-patterns/validate should validate number")
        void validateNumber_shouldValidateNumber() throws Exception {
            when(primitivePatternDemo.validateNumber(any())).thenReturn("Valid positive number");

            mockMvc.perform(post("/api/java25/primitive-patterns/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("42"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Valid positive number"));
        }
    }

    @Nested
    @DisplayName("Scoped Values Endpoints")
    class ScopedValuesTests {

        @Test
        @DisplayName("GET /api/java25/scoped-values/context should process with context")
        void testScopedValues_shouldProcessWithContext() throws Exception {
            when(scopedValuesDemo.processWithContext(anyString(), anyString(), anyString()))
                    .thenReturn("Processing with context - User: user123, Request: req456, Tenant: tenant789");

            mockMvc.perform(get("/api/java25/scoped-values/context")
                            .param("userId", "user123")
                            .param("requestId", "req456")
                            .param("tenantId", "tenant789"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("User: user123")))
                    .andExpect(content().string(containsString("Request: req456")))
                    .andExpect(content().string(containsString("Tenant: tenant789")));
        }

        @Test
        @DisplayName("GET /api/java25/scoped-values/concurrency should process with concurrency")
        void testScopedValuesWithConcurrency_shouldProcessConcurrently() throws Exception {
            when(scopedValuesDemo.processWithConcurrency(anyString()))
                    .thenReturn("Concurrent processing completed");

            mockMvc.perform(get("/api/java25/scoped-values/concurrency")
                            .param("userId", "user123"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Concurrent processing completed"));
        }

        @Test
        @DisplayName("GET /api/java25/scoped-values/nested should process nested scopes")
        void testNestedScopes_shouldProcessNestedScopes() throws Exception {
            when(scopedValuesDemo.nestedScopes())
                    .thenReturn("Nested scopes processed");

            mockMvc.perform(get("/api/java25/scoped-values/nested"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Nested scopes processed"));
        }

        @Test
        @DisplayName("GET /api/java25/scoped-values/default should return default values")
        void testDefaultValue_shouldReturnDefaultValues() throws Exception {
            when(scopedValuesDemo.hasUserContext()).thenReturn(false);
            when(scopedValuesDemo.getUserIdOrDefault()).thenReturn("anonymous");

            mockMvc.perform(get("/api/java25/scoped-values/default"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.hasContext").value(false))
                    .andExpect(jsonPath("$.userId").value("anonymous"));
        }
    }

    @Nested
    @DisplayName("Structured Concurrency Endpoints")
    class StructuredConcurrencyTests {

        @Test
        @DisplayName("GET /api/java25/structured-concurrency/user-data should fetch user data")
        void fetchUserData_shouldFetchUserData() throws Exception {
            when(structuredConcurrencyDemo.fetchUserDataWithFailure(anyString()))
                    .thenReturn("User data fetched successfully");

            mockMvc.perform(get("/api/java25/structured-concurrency/user-data")
                            .param("userId", "user123"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User data fetched successfully"));
        }

        @Test
        @DisplayName("GET /api/java25/structured-concurrency/multi-source should fetch from multiple sources")
        void fetchFromMultipleSources_shouldFetchFromMultipleSources() throws Exception {
            when(structuredConcurrencyDemo.fetchFromMultipleSources(anyString()))
                    .thenReturn("Data aggregated from multiple sources");

            mockMvc.perform(get("/api/java25/structured-concurrency/multi-source")
                            .param("query", "search-term"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Data aggregated from multiple sources"));
        }

        @Test
        @DisplayName("GET /api/java25/structured-concurrency/timeout should handle timeout")
        void fetchWithTimeout_shouldHandleTimeout() throws Exception {
            when(structuredConcurrencyDemo.fetchWithTimeout(anyString()))
                    .thenReturn("Data fetched within timeout");

            mockMvc.perform(get("/api/java25/structured-concurrency/timeout")
                            .param("userId", "user123"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Data fetched within timeout"));
        }

        @Test
        @DisplayName("GET /api/java25/structured-concurrency/virtual-threads should process with virtual threads")
        void processWithVirtualThreads_shouldProcessWithVirtualThreads() throws Exception {
            when(structuredConcurrencyDemo.processWithVirtualThreads(anyString()))
                    .thenReturn("Processed with virtual threads");

            mockMvc.perform(get("/api/java25/structured-concurrency/virtual-threads")
                            .param("data", "test-data"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Processed with virtual threads"));
        }

        @Test
        @DisplayName("GET /api/java25/structured-concurrency/aggregate should aggregate data")
        void aggregateData_shouldAggregateData() throws Exception {
            var summary = new StructuredConcurrencyDemo.Summary(42, 1234.56, 29.39, 999);
            when(structuredConcurrencyDemo.aggregateData(anyString())).thenReturn(summary);

            mockMvc.perform(get("/api/java25/structured-concurrency/aggregate")
                            .param("category", "electronics"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.count").value(42))
                    .andExpect(jsonPath("$.sum").value(1234.56))
                    .andExpect(jsonPath("$.average").value(29.39))
                    .andExpect(jsonPath("$.max").value(999));
        }
    }

    @Nested
    @DisplayName("Stable Values Endpoints")
    class StableValuesTests {

        @Test
        @DisplayName("GET /api/java25/stable-values/lazy-config should return lazy config")
        void getLazyConfig_shouldReturnLazyConfig() throws Exception {
            when(stableValuesDemo.getLazyConfig())
                    .thenReturn("Config loaded lazily");

            mockMvc.perform(get("/api/java25/stable-values/lazy-config"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Config loaded lazily"));
        }

        @Test
        @DisplayName("GET /api/java25/stable-values/connection should return database connection")
        void getConnection_shouldReturnDatabaseConnection() throws Exception {
            var connection = new StableValuesDemo.DatabaseConnection(
                    "localhost",
                    5432
            );
            when(stableValuesDemo.getConnection()).thenReturn(connection);

            mockMvc.perform(get("/api/java25/stable-values/connection"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.host").value("localhost"))
                    .andExpect(jsonPath("$.port").value(5432));
        }

        @Test
        @DisplayName("GET /api/java25/stable-values/expensive should return expensive result")
        void getExpensiveResult_shouldReturnExpensiveResult() throws Exception {
            var result = new StableValuesDemo.ExpensiveResult(
                    "resultado-complejo",
                    42
            );
            when(stableValuesDemo.getExpensiveResult()).thenReturn(result);

            mockMvc.perform(get("/api/java25/stable-values/expensive"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data").value("resultado-complejo"))
                    .andExpect(jsonPath("$.value").value(42));
        }
    }

    @Nested
    @DisplayName("Module Import Endpoints")
    class ModuleImportTests {

        @Test
        @DisplayName("GET /api/java25/module-imports/info should return module import info")
        void getModuleImportInfo_shouldReturnModuleImportInfo() throws Exception {
            when(moduleImportDemo.demonstrateModuleImport())
                    .thenReturn("Module import demonstration");

            mockMvc.perform(get("/api/java25/module-imports/info"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Module import demonstration"));
        }

        @Test
        @DisplayName("GET /api/java25/module-imports/example should return module example")
        void getModuleExample_shouldReturnModuleExample() throws Exception {
            when(moduleImportDemo.getModuleExample())
                    .thenReturn("Module import example code");

            mockMvc.perform(get("/api/java25/module-imports/example"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Module import example code"));
        }
    }
}
