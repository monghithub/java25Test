package com.monghit.java25.controller;

import com.monghit.java25.features.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/java25")
public class Java25FeaturesController {

    private final PrimitivePatternMatchingDemo primitivePatternDemo;
    private final ScopedValuesDemo scopedValuesDemo;
    private final StructuredConcurrencyDemo structuredConcurrencyDemo;
    private final StableValuesDemo stableValuesDemo;
    private final ModuleImportDemo moduleImportDemo;

    // Constructor manual en lugar de @RequiredArgsConstructor para compatibilidad con Java 25
    public Java25FeaturesController(
            PrimitivePatternMatchingDemo primitivePatternDemo,
            ScopedValuesDemo scopedValuesDemo,
            StructuredConcurrencyDemo structuredConcurrencyDemo,
            StableValuesDemo stableValuesDemo,
            ModuleImportDemo moduleImportDemo) {
        this.primitivePatternDemo = primitivePatternDemo;
        this.scopedValuesDemo = scopedValuesDemo;
        this.structuredConcurrencyDemo = structuredConcurrencyDemo;
        this.stableValuesDemo = stableValuesDemo;
        this.moduleImportDemo = moduleImportDemo;
    }

    /**
     * Endpoint principal con información general
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFeatures() {
        Map<String, Object> features = new HashMap<>();
        features.put("message", "Java 25 New Features Demo API");
        features.put("features", new String[]{
                "Primitive Types in Patterns (Preview)",
                "Module Import Declarations",
                "Scoped Values",
                "Structured Concurrency",
                "Stable Values (Preview)",
                "Instance Main Methods",
                "Compact Object Headers",
                "JDK Flight Recorder Enhancements"
        });
        features.put("endpoints", Map.of(
                "primitive-patterns", "/api/java25/primitive-patterns",
                "scoped-values", "/api/java25/scoped-values",
                "structured-concurrency", "/api/java25/structured-concurrency",
                "stable-values", "/api/java25/stable-values",
                "module-imports", "/api/java25/module-imports"
        ));
        return ResponseEntity.ok(features);
    }

    /**
     * Primitive Pattern Matching endpoints
     */
    @PostMapping("/primitive-patterns/process")
    public ResponseEntity<String> processPrimitive(@RequestBody Object value) {
        String result = primitivePatternDemo.processPrimitive(value);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/primitive-patterns/check-type")
    public ResponseEntity<String> checkPrimitiveType(@RequestBody Object value) {
        String result = primitivePatternDemo.checkPrimitiveType(value);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/primitive-patterns/convert")
    public ResponseEntity<Integer> convertToInt(@RequestBody Object value) {
        int result = primitivePatternDemo.safeConvertToInt(value);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/primitive-patterns/validate")
    public ResponseEntity<String> validateNumber(@RequestBody Object value) {
        String result = primitivePatternDemo.validateNumber(value);
        return ResponseEntity.ok(result);
    }

    /**
     * Scoped Values endpoints
     */
    @GetMapping("/scoped-values/context")
    public ResponseEntity<String> testScopedValues(
            @RequestParam String userId,
            @RequestParam String requestId,
            @RequestParam String tenantId) {
        String result = scopedValuesDemo.processWithContext(userId, requestId, tenantId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/scoped-values/concurrency")
    public ResponseEntity<String> testScopedValuesWithConcurrency(
            @RequestParam String userId) throws Exception {
        String result = scopedValuesDemo.processWithConcurrency(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/scoped-values/nested")
    public ResponseEntity<String> testNestedScopes() {
        String result = scopedValuesDemo.nestedScopes();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/scoped-values/default")
    public ResponseEntity<Map<String, Object>> testDefaultValue() {
        Map<String, Object> response = new HashMap<>();
        response.put("hasContext", scopedValuesDemo.hasUserContext());
        response.put("userId", scopedValuesDemo.getUserIdOrDefault());
        return ResponseEntity.ok(response);
    }

    /**
     * Structured Concurrency endpoints
     */
    @GetMapping("/structured-concurrency/user-data")
    public ResponseEntity<String> fetchUserData(@RequestParam String userId) throws Exception {
        String result = structuredConcurrencyDemo.fetchUserDataWithFailure(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/structured-concurrency/multi-source")
    public ResponseEntity<String> fetchFromMultipleSources(@RequestParam String query) throws Exception {
        String result = structuredConcurrencyDemo.fetchFromMultipleSources(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/structured-concurrency/timeout")
    public ResponseEntity<String> fetchWithTimeout(@RequestParam String userId) throws Exception {
        String result = structuredConcurrencyDemo.fetchWithTimeout(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/structured-concurrency/virtual-threads")
    public ResponseEntity<String> processWithVirtualThreads(@RequestParam String data) throws Exception {
        String result = structuredConcurrencyDemo.processWithVirtualThreads(data);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/structured-concurrency/aggregate")
    public ResponseEntity<StructuredConcurrencyDemo.Summary> aggregateData(
            @RequestParam String category) throws Exception {
        StructuredConcurrencyDemo.Summary result = structuredConcurrencyDemo.aggregateData(category);
        return ResponseEntity.ok(result);
    }

    /**
     * Stable Values endpoints
     */
    @GetMapping("/stable-values/lazy-config")
    public ResponseEntity<String> getLazyConfig() {
        String result = stableValuesDemo.getLazyConfig();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stable-values/connection")
    public ResponseEntity<StableValuesDemo.DatabaseConnection> getConnection() {
        StableValuesDemo.DatabaseConnection result = stableValuesDemo.getConnection();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stable-values/expensive")
    public ResponseEntity<StableValuesDemo.ExpensiveResult> getExpensiveResult() {
        StableValuesDemo.ExpensiveResult result = stableValuesDemo.getExpensiveResult();
        return ResponseEntity.ok(result);
    }

    /**
     * Module Import declarations
     */
    @GetMapping("/module-imports/info")
    public ResponseEntity<String> getModuleImportInfo() {
        String result = moduleImportDemo.demonstrateModuleImport();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/module-imports/example")
    public ResponseEntity<String> getModuleExample() {
        String result = moduleImportDemo.getModuleExample();
        return ResponseEntity.ok(result);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("javaVersion", System.getProperty("java.version"));
        health.put("application", "Java 25 Features Demo");
        return ResponseEntity.ok(health);
    }
}
