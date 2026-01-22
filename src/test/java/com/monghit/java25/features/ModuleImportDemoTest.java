package com.monghit.java25.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para ModuleImportDemo
 */
class ModuleImportDemoTest {

    private ModuleImportDemo demo;

    @BeforeEach
    void setUp() {
        demo = new ModuleImportDemo();
    }

    // ==================== demonstrateModuleImport Tests ====================

    @Test
    void demonstrateModuleImport_shouldReturnDescription() {
        String result = demo.demonstrateModuleImport();

        assertThat(result)
                .isNotNull()
                .isNotBlank();
    }

    @Test
    void demonstrateModuleImport_shouldContainKeyInformation() {
        String result = demo.demonstrateModuleImport();

        assertThat(result)
                .contains("Module Import Declarations")
                .contains("simplifica");
    }

    @Test
    void demonstrateModuleImport_shouldListAdvantages() {
        String result = demo.demonstrateModuleImport();

        assertThat(result)
                .contains("Ventajas:")
                .contains("Código más limpio")
                .contains("Menos imports repetitivos")
                .contains("Mejor expresividad");
    }

    @Test
    void demonstrateModuleImport_shouldListLimitations() {
        String result = demo.demonstrateModuleImport();

        assertThat(result)
                .contains("Limitaciones:")
                .contains("Solo funciona con módulos");
    }

    @Test
    void demonstrateModuleImport_shouldListBenefits() {
        String result = demo.demonstrateModuleImport();

        assertThat(result)
                .contains("Importar todos los paquetes de un módulo")
                .contains("Reducir código boilerplate")
                .contains("Facilitar el uso de librerías modulares");
    }

    // ==================== getModuleExample Tests ====================

    @Test
    void getModuleExample_shouldReturnModuleInfoExample() {
        String result = demo.getModuleExample();

        assertThat(result)
                .isNotNull()
                .isNotBlank();
    }

    @Test
    void getModuleExample_shouldContainModuleDeclaration() {
        String result = demo.getModuleExample();

        assertThat(result)
                .contains("module-info.java")
                .contains("module com.monghit.features");
    }

    @Test
    void getModuleExample_shouldShowExports() {
        String result = demo.getModuleExample();

        assertThat(result)
                .contains("exports com.monghit.features.api")
                .contains("exports com.monghit.features.service")
                .contains("exports com.monghit.features.model");
    }

    @Test
    void getModuleExample_shouldShowRequires() {
        String result = demo.getModuleExample();

        assertThat(result)
                .contains("requires java.base")
                .contains("requires spring.boot");
    }

    @Test
    void getModuleExample_shouldShowImportUsage() {
        String result = demo.getModuleExample();

        assertThat(result)
                .contains("import module com.monghit.features");
    }

    @Test
    void getModuleExample_shouldShowAccessiblePackages() {
        String result = demo.getModuleExample();

        assertThat(result)
                .contains("com.monghit.features.api.*")
                .contains("com.monghit.features.service.*")
                .contains("com.monghit.features.model.*");
    }

    // ==================== Integration Tests ====================

    @Test
    void bothMethods_shouldProvideComplementaryInformation() {
        String description = demo.demonstrateModuleImport();
        String example = demo.getModuleExample();

        assertThat(description).isNotNull();
        assertThat(example).isNotNull();

        // El ejemplo debería complementar la descripción
        assertThat(description).contains("Module Import");
        assertThat(example).contains("module com.monghit.features");
    }

    @Test
    void demonstrateModuleImport_shouldBeWellFormatted() {
        String result = demo.demonstrateModuleImport();

        // Verificar que tiene estructura de secciones
        assertThat(result).contains("1.");
        assertThat(result).contains("2.");
        assertThat(result).contains("3.");
        assertThat(result).contains("4.");
    }

    @Test
    void getModuleExample_shouldBeValidModuleDeclaration() {
        String result = demo.getModuleExample();

        // Verificar estructura básica de module-info.java
        assertThat(result)
                .contains("module ")
                .contains("exports ")
                .contains("requires ")
                .contains("{")
                .contains("}");
    }

    @Test
    void demonstrateModuleImport_shouldExplainComparison() {
        String result = demo.demonstrateModuleImport();

        // Debería explicar diferentes aspectos de module import
        assertThat(result).contains("Module Import");
    }
}
