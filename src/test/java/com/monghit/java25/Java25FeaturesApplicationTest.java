package com.monghit.java25;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de carga del contexto de Spring Boot
 */
@SpringBootTest
@ActiveProfiles("test")
class Java25FeaturesApplicationTest {

    @Test
    void contextLoads() {
        // Este test verifica que el contexto de Spring Boot se carga correctamente
    }
}
