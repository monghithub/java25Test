# Documentación de Tests - Java 25 Features

## Resumen

Suite completa de tests para la aplicación de demostración de características de Java 25.

**Estado actual:** ✅ **90 tests pasando, 0 fallos, 0 errores**

## Estructura de Tests

### Tests Unitarios

#### 1. PrimitivePatternMatchingDemoTest
**Archivo:** `src/test/java/com/monghit/java25/features/PrimitivePatternMatchingDemoTest.java`
**Tests:** 30
**Cobertura:**
- Pattern matching con diferentes tipos primitivos (int, long, double, float, boolean, byte, short, char)
- Uso de guards condicionales en pattern matching
- Conversión segura entre tipos
- Validación de números con diferentes condiciones
- Casos de borde y null handling

**Métodos testeados:**
- `processPrimitive(Object)` - 9 tests
- `checkPrimitiveType(Object)` - 5 tests
- `safeConvertToInt(Object)` - 7 tests (incluyendo tests parametrizados)
- `validateNumber(Object)` - 8 tests
- Casos de borde adicionales - 1 test

---

#### 2. ScopedValuesDemoTest
**Archivo:** `src/test/java/com/monghit/java25/features/ScopedValuesDemoTest.java`
**Tests:** 12
**Cobertura:**
- Uso básico de Scoped Values
- Propagación de contexto
- Concurrencia con virtual threads
- Scopes anidados
- Valores por defecto
- Verificación de binding

**Métodos testeados:**
- `processWithContext(String, String, String)` - 2 tests
- `processWithConcurrency(String)` - 2 tests
- `getUserIdOrDefault()` - 2 tests
- `hasUserContext()` - 1 test
- `nestedScopes()` - 3 tests
- Tests de integración - 2 tests

---

#### 3. StructuredConcurrencyDemoTest
**Archivo:** `src/test/java/com/monghit/java25/features/StructuredConcurrencyDemoTest.java`
**Tests:** 16
**Cobertura:**
- Obtención de datos de múltiples fuentes
- Procesamiento concurrente
- Operaciones con timeout
- Virtual threads
- Agregación de datos
- Records inmutables

**Métodos testeados:**
- `fetchUserDataWithFailure(String)` - 3 tests
- `fetchFromMultipleSources(String)` - 2 tests
- `fetchWithTimeout(String)` - 2 tests
- `processWithVirtualThreads(String)` - 3 tests
- `aggregateData(String)` - 3 tests
- Tests del Record `Summary` - 3 tests

---

#### 4. StableValuesDemoTest
**Archivo:** `src/test/java/com/monghit/java25/features/StableValuesDemoTest.java`
**Tests:** 16
**Cobertura:**
- Inicialización perezosa thread-safe
- Inmutabilidad diferida
- Cálculos costosos con caché
- Thread safety
- Records de datos

**Métodos testeados:**
- `getLazyConfig()` - 3 tests
- `getConnection()` - 3 tests
- `getExpensiveResult()` - 1 test
- `demonstrateThreadSafety()` - 1 test
- Tests de Records (`DatabaseConnection`, `ExpensiveResult`) - 6 tests
- Tests de integración - 2 tests

---

#### 5. ModuleImportDemoTest
**Archivo:** `src/test/java/com/monghit/java25/features/ModuleImportDemoTest.java`
**Tests:** 15
**Cobertura:**
- Explicación de Module Import Declarations
- Ejemplos de uso
- Comparación con enfoques tradicionales
- Sintaxis de module-info.java

**Métodos testeados:**
- `demonstrateModuleImport()` - 7 tests
- `getModuleExample()` - 7 tests
- Tests de integración - 1 test

---

### Tests de Integración

#### 6. Java25FeaturesApplicationTest
**Archivo:** `src/test/java/com/monghit/java25/Java25FeaturesApplicationTest.java`
**Tests:** 1
**Cobertura:**
- Verificación de que el contexto de Spring Boot se carga correctamente
- Test de smoke test básico

---

## Configuración de Tests

### Maven Configuration
El `pom.xml` está configurado con:
- Plugin Surefire con `--enable-preview` para soportar preview features de Java 25
- Compiler Plugin con `--enable-preview`
- Dependencias de testing: Spring Boot Test, JUnit Jupiter, AssertJ, Mockito

### Application Properties
`src/test/resources/application-test.properties` configurado con:
- Puerto aleatorio para evitar conflictos
- Logging reducido para tests
- Perfil de test activo

---

## Ejecutar los Tests

### Todos los tests
```bash
mvn test
```

### Tests específicos
```bash
# Solo tests de features
mvn test -Dtest=*Demo*Test

# Test específico
mvn test -Dtest=PrimitivePatternMatchingDemoTest

# Método específico
mvn test -Dtest=PrimitivePatternMatchingDemoTest#processPrimitive_withLargeInteger_shouldReturnLargeMessage
```

### Con clean
```bash
mvn clean test
```

---

## Estadísticas

| Componente | Tests | Estado |
|------------|-------|--------|
| PrimitivePatternMatchingDemo | 30 | ✅ |
| ScopedValuesDemo | 12 | ✅ |
| StructuredConcurrencyDemo | 16 | ✅ |
| StableValuesDemo | 16 | ✅ |
| ModuleImportDemo | 15 | ✅ |
| Java25FeaturesApplication | 1 | ✅ |
| **TOTAL** | **90** | **✅** |

---

## Características Clave de los Tests

### 1. Tests Parametrizados
Uso de `@ParameterizedTest` con `@MethodSource` para probar múltiples casos:
```java
@ParameterizedTest
@MethodSource("provideConversionCases")
void safeConvertToInt_shouldConvertCorrectly(Object input, int expected) {
    int result = demo.safeConvertToInt(input);
    assertThat(result).isEqualTo(expected);
}
```

### 2. AssertJ Fluent Assertions
Uso consistente de AssertJ para assertions legibles:
```java
assertThat(result)
    .contains("User: user123")
    .contains("Request: req456")
    .contains("Tenant: tenant789");
```

### 3. Testing de Records
Verificación completa de records inmutables:
```java
@Test
void summaryRecord_shouldSupportEquality() {
    var summary1 = new Summary(42, 1234.56, 29.39, 999);
    var summary2 = new Summary(42, 1234.56, 29.39, 999);

    assertThat(summary1).isEqualTo(summary2);
    assertThat(summary1.hashCode()).isEqualTo(summary2.hashCode());
}
```

### 4. Thread Safety Testing
Tests que verifican comportamiento concurrente:
```java
@Test
void demonstrateThreadSafety_shouldCompleteWithoutException() {
    assertDoesNotThrow(() -> {
        demo.demonstrateThreadSafety();
    });
}
```

---

## Notas Importantes

### Preview Features
Algunos tests utilizan preview features de Java 25:
- Stable Values (JEP 572)
- Structured Concurrency (JEP 505)

Asegúrate de ejecutar con `--enable-preview`:
```bash
mvn test  # Ya configurado en pom.xml
```

### Comportamiento de ScopedValues
Los ScopedValues en Java 25 NO se propagan automáticamente a virtual threads creados con `Thread.ofVirtual().start()`. Los tests están ajustados para reflejar este comportamiento.

### Test de Integración del Controller
El test de integración del controller (`Java25FeaturesControllerIntegrationTest`) está temporalmente deshabilitado debido a cambios en las APIs de testing de Spring Boot 4.0. Los tests unitarios cubren toda la lógica de negocio.

---

## Reporte de Cobertura

Para generar un reporte de cobertura con JaCoCo:
```bash
mvn clean test jacoco:report
```

El reporte estará disponible en: `target/site/jacoco/index.html`

---

## Próximos Pasos

1. ✅ Tests unitarios completos para todas las features
2. ✅ Test de contexto de Spring Boot
3. ⏳ Test de integración del controller (pendiente por APIs de Spring Boot 4.0)
4. ⏳ Configuración de JaCoCo para cobertura
5. ⏳ Tests de rendimiento (opcional)

---

## Recursos

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [Java 25 Release Notes](https://openjdk.org/projects/jdk/25/)
