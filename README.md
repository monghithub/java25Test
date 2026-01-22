# Java 25 Features Demo

Proyecto Spring Boot para probar y demostrar las nuevas funcionalidades de Java 25 comparado con Java 21.

## Requisitos

- **Java 25** (JDK 25)
- **Maven 3.8+**
- **Spring Boot 4.0.1** (con soporte completo para Java 25)
- **SDKMAN** (opcional, para gestión de versiones de Java)

## Instalación de Java 25 con SDKMAN

Este proyecto incluye un archivo `.sdkmanrc` que configura automáticamente la versión de Java 25.

### Setup Rápido (Recomendado)

```bash
# Ejecutar el script de setup automático
./setup.sh
```

El script `setup.sh` hará automáticamente:
- Verificar que SDKMAN esté instalado
- Instalar Java 25 según `.sdkmanrc`
- Configurar auto-env (opcional)
- Compilar el proyecto (opcional)

### Setup Manual

```bash
# Instalar SDKMAN si no lo tienes
curl -s "https://get.sdkman.io" | bash

# Listar versiones disponibles de Java 25
sdk list java | grep 25

# Instalar Java 25 (por ejemplo, OpenJDK 25)
sdk install java 25-open

# Activar automáticamente la versión de Java del proyecto
# Navegar al directorio del proyecto y ejecutar:
sdk env install

# Cambiar a la versión de Java especificada en .sdkmanrc
sdk env

# Verificar la versión de Java
java -version
```

## Instalación y Ejecución

```bash
# Si usas SDKMAN, cambiar a Java 25
sdk env

# Compilar el proyecto
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run

# O ejecutar con preview features habilitado
java --enable-preview -jar target/java25-features-1.0.0-SNAPSHOT.jar
```

La aplicación estará disponible en: `http://localhost:8080`

### Configuración automática de SDKMAN

El proyecto incluye `.sdkmanrc` que especifica `java=25-open`. SDKMAN puede cambiar automáticamente a esta versión cuando entres al directorio del proyecto si tienes habilitado el auto-env:

```bash
# Habilitar auto-env en SDKMAN
sdk config auto-env true

# Ahora cada vez que entres al directorio del proyecto, SDKMAN cambiará automáticamente a Java 25
cd java25Test
# SDKMAN activará automáticamente Java 25
```

## Nuevas Funcionalidades de Java 25

### 1. Primitive Types in Patterns (JEP 455 - Preview)

**¿Qué es?**
Permite usar tipos primitivos en pattern matching, extendiendo `instanceof` y `switch` para trabajar con todos los tipos primitivos.

**Ventajas:**
- Pattern matching más expresivo
- Código más conciso y legible
- Eliminación de conversiones manuales
- Type-safe handling de primitivos

**Ejemplo:**
```java
public String processPrimitive(Object value) {
    return switch (value) {
        case int i when i > 100 -> "Integer grande: " + i;
        case int i -> "Integer pequeño: " + i;
        case double d -> "Double: " + d;
        case boolean b -> "Boolean: " + b;
        default -> "Tipo desconocido";
    };
}
```

**Endpoints:**
- `POST /api/java25/primitive-patterns/process` - Procesar valor primitivo
- `POST /api/java25/primitive-patterns/check-type` - Verificar tipo
- `POST /api/java25/primitive-patterns/validate` - Validar número

### 2. Scoped Values (JEP 481 - Final)

**¿Qué es?**
Nueva primitiva de concurrencia que permite compartir datos de contexto inmutable entre llamadores, callees y threads hijos dentro de un scope léxico bien definido.

**Ventajas sobre ThreadLocal:**
- Inmutabilidad garantizada
- Mejor rendimiento
- Scope léxico claro
- Mejor integración con virtual threads
- No hay riesgo de memory leaks

**Ejemplo:**
```java
private static final ScopedValue<String> USER_ID = ScopedValue.newInstance();

public String processWithContext(String userId) {
    return ScopedValue.where(USER_ID, userId).call(() -> {
        // USER_ID está disponible aquí
        return performOperation();
    });
}
```

**Endpoints:**
- `GET /api/java25/scoped-values/context` - Probar con contexto
- `GET /api/java25/scoped-values/concurrency` - Probar con concurrencia
- `GET /api/java25/scoped-values/nested` - Probar scopes anidados

### 3. Structured Concurrency (JEP 505 - Final)

**¿Qué es?**
API para concurrencia estructurada que permite código paralelo seguro, legible y mantenible agrupando subtareas en ciclos de vida claros.

**Ventajas:**
- Código paralelo más seguro
- Manejo consistente de errores
- Propagación automática de cancelaciones
- Mejor debugging
- Previene thread leaks

**Ejemplo:**
```java
public String fetchUserData(String userId) throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        var userTask = scope.fork(() -> fetchUserProfile(userId));
        var ordersTask = scope.fork(() -> fetchUserOrders(userId));

        scope.join();
        scope.throwIfFailed();

        return userTask.get() + " | " + ordersTask.get();
    }
}
```

**Endpoints:**
- `GET /api/java25/structured-concurrency/user-data` - Fetch datos de usuario
- `GET /api/java25/structured-concurrency/multi-source` - Fetch de múltiples fuentes
- `GET /api/java25/structured-concurrency/aggregate` - Agregación de datos

### 4. Stable Values (JEP 572 - Preview)

**¿Qué es?**
API para inmutabilidad diferida. Un `StableValue` puede ser creado sin valor inicial y establecerse exactamente una vez, después de lo cual es inmutable.

**Ventajas:**
- Inicialización perezosa thread-safe
- Mejor rendimiento que volatile o AtomicReference
- Garantía de inmutabilidad post-inicialización
- Más simple que double-checked locking

**Ejemplo:**
```java
private final StableValue<String> lazyConfig = StableValue.of(null);

public String getConfig() {
    return lazyConfig.get(() -> {
        // Esta función solo se ejecuta la primera vez
        return loadConfiguration();
    });
}
```

**Endpoints:**
- `GET /api/java25/stable-values/lazy-config` - Configuración lazy
- `GET /api/java25/stable-values/connection` - Conexión lazy
- `GET /api/java25/stable-values/expensive` - Cálculo costoso lazy

### 5. Module Import Declarations (JEP 476 - Final)

**¿Qué es?**
Permite importar todos los paquetes exportados por un módulo de forma sucinta usando `import module <module-name>`.

**Ventajas:**
- Código más limpio
- Menos imports repetitivos
- Mejor expresividad
- Documenta dependencias de módulos
- Facilita refactoring

**Ejemplo:**
```java
// Antes
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

// Con Java 25
import module java.base;
```

**Endpoints:**
- `GET /api/java25/module-imports/info` - Información sobre module imports
- `GET /api/java25/module-imports/example` - Ejemplo de uso

### 6. Instance Main Methods (JEP 495 - Final)

**¿Qué es?**
Permite escribir métodos `main` de instancia, simplificando el código para principiantes y casos simples.

**Ventajas:**
- Código más simple para principiantes
- Acceso directo a campos de instancia
- Menos boilerplate
- Más intuitivo

**Ejemplo:**
```java
public class SimplifiedMainDemo {
    void main() {
        System.out.println("¡Hola Mundo!");
    }
}
```

### 7. Compact Object Headers (JEP 519 - Product/Stable)

**¿Qué es?**
Reduce el tamaño de los headers de objetos en la JVM, ahorrando memoria significativa.

**Ventajas:**
- Menor uso de memoria
- Mejor rendimiento de cache
- Más objetos en memoria
- Sin cambios en el código

**Nota:** Esta es una mejora a nivel de JVM, no requiere cambios en el código.

### 8. JDK Flight Recorder Enhancements

**¿Qué es?**
Mejoras en JFR incluyendo method timing, bytecode instrumentation y CPU time profiling.

**Ventajas:**
- Mejor profiling
- Más detalles de rendimiento
- CPU time profiling en Linux
- Method timing mejorado

**Nota:** Usar con herramientas de monitoreo JFR.

## Endpoints de la API

### Información General
```bash
# Obtener lista de features
GET http://localhost:8080/api/java25

# Health check
GET http://localhost:8080/api/java25/health
```

### Primitive Pattern Matching
```bash
# Procesar primitivo
POST http://localhost:8080/api/java25/primitive-patterns/process
Content-Type: application/json
Body: 42

# Validar número
POST http://localhost:8080/api/java25/primitive-patterns/validate
Content-Type: application/json
Body: 150
```

### Scoped Values
```bash
# Probar con contexto
GET http://localhost:8080/api/java25/scoped-values/context?userId=user123&requestId=req456&tenantId=tenant789

# Probar con concurrencia
GET http://localhost:8080/api/java25/scoped-values/concurrency?userId=user123

# Scopes anidados
GET http://localhost:8080/api/java25/scoped-values/nested
```

### Structured Concurrency
```bash
# Fetch datos de usuario
GET http://localhost:8080/api/java25/structured-concurrency/user-data?userId=user123

# Fetch de múltiples fuentes
GET http://localhost:8080/api/java25/structured-concurrency/multi-source?query=search-term

# Agregación de datos
GET http://localhost:8080/api/java25/structured-concurrency/aggregate?category=sales
```

### Stable Values
```bash
# Configuración lazy
GET http://localhost:8080/api/java25/stable-values/lazy-config

# Conexión lazy
GET http://localhost:8080/api/java25/stable-values/connection

# Resultado costoso
GET http://localhost:8080/api/java25/stable-values/expensive
```

### Module Imports
```bash
# Información
GET http://localhost:8080/api/java25/module-imports/info

# Ejemplo
GET http://localhost:8080/api/java25/module-imports/example
```

## Estructura del Proyecto

```
java25Test/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/monghit/java25/
│       │       ├── Java25FeaturesApplication.java
│       │       ├── controller/
│       │       │   └── Java25FeaturesController.java
│       │       └── features/
│       │           ├── PrimitivePatternMatchingDemo.java
│       │           ├── ScopedValuesDemo.java
│       │           ├── StructuredConcurrencyDemo.java
│       │           ├── StableValuesDemo.java
│       │           ├── ModuleImportDemo.java
│       │           ├── InstanceMainDemo.java
│       │           └── SimplifiedMainDemo.java
│       └── resources/
│           └── application.properties
├── pom.xml
└── README.md
```

## Testing

Cada feature tiene su propia clase de demostración con métodos para probar diferentes aspectos:

1. **PrimitivePatternMatchingDemo** - Pattern matching con primitivos
2. **ScopedValuesDemo** - Manejo de contexto con scoped values
3. **StructuredConcurrencyDemo** - Concurrencia estructurada
4. **StableValuesDemo** - Valores estables e inmutabilidad diferida
5. **ModuleImportDemo** - Imports de módulos
6. **InstanceMainDemo** - Métodos main de instancia

## Recursos Adicionales

### Documentación Oficial
- [JDK 25 Release Notes](https://www.oracle.com/java/technologies/javase/25-relnote-issues.html)
- [OpenJDK JDK 25](https://openjdk.org/projects/jdk/25/)

### Artículos y Tutoriales
- [New Features in Java 25 | Baeldung](https://www.baeldung.com/java-25-features)
- [Java 25 New Features With Examples](https://javatechonline.com/java-25-new-features-with-examples/)
- [JDK 25: The new features in Java 25 | InfoWorld](https://www.infoworld.com/article/3846172/jdk-25-the-new-features-in-java-25.html)

### JEPs Relacionados
- **JEP 455** - Primitive Types in Patterns (Preview)
- **JEP 476** - Module Import Declarations
- **JEP 481** - Scoped Values
- **JEP 495** - Simple Source Files and Instance Main Methods
- **JEP 505** - Structured Concurrency
- **JEP 519** - Compact Object Headers (Product)
- **JEP 572** - Stable Values (Preview)

## Notas Importantes

1. **Preview Features**: Algunas características están en preview (Primitive Patterns, Stable Values) y requieren `--enable-preview` para funcionar.

2. **Java 25 LTS**: Java 25 es una versión LTS (Long-Term Support) con soporte de Oracle por al menos 8 años.

3. **Compatibilidad**: Estas características son específicas de Java 25 y no están disponibles en versiones anteriores.

4. **Virtual Threads**: Aunque introducidos en Java 21, se han mejorado significativamente en Java 25 con mejor integración con Scoped Values y Structured Concurrency.

## Contribuciones

Este es un proyecto de demostración educativo. Siéntete libre de explorar, modificar y aprender de los ejemplos.

## Licencia

MIT License
