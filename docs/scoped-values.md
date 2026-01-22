# Scoped Values (JEP 481 - Final)

## Índice
- [¿Qué son los Scoped Values?](#qué-son-los-scoped-values)
- [Motivación y Problemas que Resuelven](#motivación-y-problemas-que-resuelven)
- [Conceptos Clave](#conceptos-clave)
- [API y Sintaxis](#api-y-sintaxis)
- [Ejemplos Detallados](#ejemplos-detallados)
- [Casos de Uso Prácticos](#casos-de-uso-prácticos)
- [Scoped Values vs ThreadLocal](#scoped-values-vs-threadlocal)
- [Integración con Virtual Threads](#integración-con-virtual-threads)
- [Mejores Prácticas](#mejores-prácticas)
- [Rendimiento](#rendimiento)
- [Referencias](#referencias)

## ¿Qué son los Scoped Values?

**Scoped Values** es una nueva API de concurrencia en Java 25 (JEP 481 - Final) que permite compartir datos **inmutables** entre métodos, threads y tareas de forma segura y eficiente, dentro de un **scope léxico** bien definido.

### Características Principales

- ✅ **Inmutabilidad**: Los valores no pueden cambiar una vez establecidos
- ✅ **Thread-safe**: Seguro para uso concurrente sin sincronización adicional
- ✅ **Scope léxico**: Los valores están disponibles solo dentro de un bloque definido
- ✅ **Alto rendimiento**: Más eficiente que `ThreadLocal`
- ✅ **Compatible con Virtual Threads**: Diseñado específicamente para la concurrencia moderna

## Motivación y Problemas que Resuelven

### El Problema con ThreadLocal

Antes de Scoped Values, `ThreadLocal` era la solución estándar para compartir contexto entre threads:

```java
// ThreadLocal - Problemático
public class UserContext {
    private static ThreadLocal<String> userId = new ThreadLocal<>();

    public static void setUserId(String id) {
        userId.set(id);
    }

    public static String getUserId() {
        return userId.get();
    }

    // ¡PROBLEMA! Fácil olvidar limpiar
    public static void clear() {
        userId.remove();  // Debe llamarse manualmente
    }
}

// Uso propenso a errores
UserContext.setUserId("user123");
try {
    processRequest();  // Puede fallar
} finally {
    UserContext.clear();  // Fácil de olvidar -> memory leak
}
```

### Problemas de ThreadLocal

1. **Memory Leaks**: Olvidar llamar `remove()` causa fugas de memoria
2. **Mutabilidad**: Los valores pueden cambiar, causando bugs sutiles
3. **No apto para Virtual Threads**: Cada virtual thread necesita su propia copia
4. **Scope no claro**: No hay un scope léxico definido
5. **Rendimiento**: Alto costo en memoria con millones de virtual threads

### La Solución: Scoped Values

```java
// Scoped Values - Seguro y elegante
public class UserContext {
    public static final ScopedValue<String> USER_ID = ScopedValue.newInstance();
}

// Uso seguro con scope léxico
ScopedValue.where(UserContext.USER_ID, "user123")
    .run(() -> {
        processRequest();  // USER_ID disponible aquí
        // Al salir del scope, USER_ID automáticamente no está disponible
    });
// USER_ID ya no está disponible aquí
```

## Conceptos Clave

### 1. ScopedValue

Un `ScopedValue<T>` es un contenedor inmutable para un valor que está disponible dentro de un scope delimitado:

```java
// Declarar un ScopedValue
public static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
public static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();
```

### 2. Binding (Vinculación)

"Bindar" un valor significa establecerlo para un scope específico:

```java
// Bindar y ejecutar código
ScopedValue.where(REQUEST_ID, "req-123")
    .run(() -> {
        // REQUEST_ID está "bound" aquí
        String id = REQUEST_ID.get();  // "req-123"
    });
// REQUEST_ID ya no está "bound" aquí
```

### 3. Scope Léxico

El valor está disponible solo dentro del bloque de código donde fue establecido:

```java
ScopedValue.where(REQUEST_ID, "req-123")
    .run(() -> {
        method1();  // Tiene acceso a REQUEST_ID
    });
// Fuera del scope, REQUEST_ID no está disponible

void method1() {
    method2();  // También tiene acceso
}

void method2() {
    String id = REQUEST_ID.get();  // Funciona!
}
```

### 4. Inmutabilidad

Una vez establecido, el valor no puede cambiar:

```java
ScopedValue.where(REQUEST_ID, "req-123")
    .run(() -> {
        String id1 = REQUEST_ID.get();  // "req-123"

        // No hay forma de cambiar el valor
        // REQUEST_ID.set("new-value");  // No existe!

        String id2 = REQUEST_ID.get();  // Sigue siendo "req-123"
    });
```

## API y Sintaxis

### Crear un ScopedValue

```java
// ScopedValue de tipo específico
public static final ScopedValue<String> USERNAME = ScopedValue.newInstance();
public static final ScopedValue<Integer> REQUEST_COUNT = ScopedValue.newInstance();
public static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();
```

### Establecer y Usar Valores

#### Método 1: `run()` - Para código sin retorno

```java
ScopedValue.where(USERNAME, "alice")
    .run(() -> {
        // Código que no retorna valor
        System.out.println("User: " + USERNAME.get());
    });
```

#### Método 2: `call()` - Para código con retorno

```java
String result = ScopedValue.where(USERNAME, "alice")
    .call(() -> {
        // Código que retorna valor
        return "Processed by: " + USERNAME.get();
    });
```

#### Método 3: Múltiples valores

```java
String result = ScopedValue
    .where(USERNAME, "alice")
    .where(REQUEST_ID, "req-123")
    .where(TENANT_ID, "tenant-1")
    .call(() -> {
        // Todos los valores disponibles aquí
        return processRequest();
    });
```

### Obtener Valores

```java
// get() - Lanza NoSuchElementException si no está bound
String username = USERNAME.get();

// isBound() - Verificar si está disponible
if (USERNAME.isBound()) {
    String username = USERNAME.get();
}

// orElse() - Valor por defecto
String username = USERNAME.orElse("anonymous");
```

## Ejemplos Detallados

### Ejemplo 1: Contexto de Request HTTP

```java
public class RequestContext {
    public static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
    public static final ScopedValue<String> USER_ID = ScopedValue.newInstance();
    public static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();
    public static final ScopedValue<Instant> REQUEST_START = ScopedValue.newInstance();
}

// En el controlador HTTP
@PostMapping("/api/process")
public Response processRequest(@RequestBody Data data) {
    String requestId = UUID.randomUUID().toString();
    String userId = extractUserId();
    String tenantId = extractTenantId();

    return ScopedValue
        .where(REQUEST_ID, requestId)
        .where(USER_ID, userId)
        .where(TENANT_ID, tenantId)
        .where(REQUEST_START, Instant.now())
        .call(() -> {
            // Todo el procesamiento tiene acceso al contexto
            return service.process(data);
        });
}

// En cualquier capa profunda del código
public class AuditLogger {
    public void log(String message) {
        String requestId = REQUEST_ID.orElse("unknown");
        String userId = USER_ID.orElse("system");
        Duration duration = Duration.between(
            REQUEST_START.get(),
            Instant.now()
        );

        System.out.printf("[%s][%s][%dms] %s%n",
            requestId, userId, duration.toMillis(), message);
    }
}
```

### Ejemplo 2: Scopes Anidados

```java
public String demonstrateNesting() {
    return ScopedValue.where(TENANT_ID, "tenant-1")
        .call(() -> {
            String outer = TENANT_ID.get();  // "tenant-1"

            // Scope interno sobrescribe el valor
            String inner = ScopedValue.where(TENANT_ID, "tenant-2")
                .call(() -> {
                    return "Inner: " + TENANT_ID.get();  // "tenant-2"
                });

            // El valor original se restaura
            String restored = TENANT_ID.get();  // "tenant-1"

            return String.format("Outer: %s, %s, Restored: %s",
                outer, inner, restored);
        });
}

// Output: "Outer: tenant-1, Inner: tenant-2, Restored: tenant-1"
```

### Ejemplo 3: Propagación a Virtual Threads

```java
public String processWithVirtualThreads(String userId) throws Exception {
    return ScopedValue.where(USER_ID, userId)
        .call(() -> {
            // Crear virtual threads
            Thread t1 = Thread.ofVirtual().start(() -> {
                // Este thread hijo hereda el USER_ID automáticamente
                String id = USER_ID.get();  // Funciona!
                System.out.println("Thread 1 ve: " + id);
            });

            Thread t2 = Thread.ofVirtual().start(() -> {
                String id = USER_ID.get();  // También funciona!
                System.out.println("Thread 2 ve: " + id);
            });

            t1.join();
            t2.join();

            return "Procesado por: " + USER_ID.get();
        });
}
```

## Casos de Uso Prácticos

### 1. Logging Contextual

```java
public class ContextualLogger {
    private static final ScopedValue<Map<String, String>> LOG_CONTEXT =
        ScopedValue.newInstance();

    public static void withContext(Map<String, String> context, Runnable action) {
        ScopedValue.where(LOG_CONTEXT, context).run(action);
    }

    public static void log(String level, String message) {
        Map<String, String> context = LOG_CONTEXT.orElse(Map.of());
        String contextStr = context.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining(", "));

        System.out.printf("[%s] [%s] %s%n", level, contextStr, message);
    }
}

// Uso
ContextualLogger.withContext(
    Map.of("requestId", "req-123", "userId", "user-456"),
    () -> {
        ContextualLogger.log("INFO", "Processing started");
        processData();
        ContextualLogger.log("INFO", "Processing completed");
    }
);

// Output:
// [INFO] [requestId=req-123, userId=user-456] Processing started
// [INFO] [requestId=req-123, userId=user-456] Processing completed
```

### 2. Multi-Tenancy

```java
public class TenantContext {
    public static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();
    public static final ScopedValue<TenantConfig> TENANT_CONFIG = ScopedValue.newInstance();

    public static void runInTenantContext(String tenantId, Runnable action) {
        TenantConfig config = loadTenantConfig(tenantId);

        ScopedValue
            .where(TENANT_ID, tenantId)
            .where(TENANT_CONFIG, config)
            .run(action);
    }
}

// Repositorio que usa el contexto
public class TenantAwareRepository {
    public List<Data> findAll() {
        String tenantId = TenantContext.TENANT_ID.get();
        // Automáticamente filtra por tenant
        return database.query("SELECT * FROM data WHERE tenant_id = ?", tenantId);
    }
}

// Uso
TenantContext.runInTenantContext("tenant-123", () -> {
    List<Data> data = repository.findAll();  // Solo datos del tenant-123
});
```

### 3. Transacciones y Contexto de Base de Datos

```java
public class TransactionContext {
    public static final ScopedValue<Connection> CONNECTION = ScopedValue.newInstance();
    public static final ScopedValue<Boolean> READ_ONLY = ScopedValue.newInstance();

    public static <T> T inTransaction(boolean readOnly,
                                      Callable<T> action) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            conn.setReadOnly(readOnly);

            return ScopedValue
                .where(CONNECTION, conn)
                .where(READ_ONLY, readOnly)
                .call(() -> {
                    try {
                        T result = action.call();
                        conn.commit();
                        return result;
                    } catch (Exception e) {
                        conn.rollback();
                        throw e;
                    }
                });
        }
    }
}

// Uso
User user = TransactionContext.inTransaction(false, () -> {
    // Todas las operaciones usan la misma conexión y transacción
    userRepository.save(user);
    auditRepository.log("User created");
    return user;
});
```

## Scoped Values vs ThreadLocal

### Comparación Detallada

| Aspecto | ThreadLocal | Scoped Values |
|---------|-------------|---------------|
| **Mutabilidad** | Mutable (set/remove) | Inmutable |
| **Scope** | Thread completo | Léxico (bloque de código) |
| **Limpieza** | Manual (remove()) | Automática |
| **Memory Leaks** | Fácil de causar | Imposible |
| **Virtual Threads** | Alto costo memoria | Optimizado |
| **Rendimiento** | Más lento | Más rápido |
| **Thread Safety** | Requiere cuidado | Garantizado |
| **Herencia a hijos** | Optional (InheritableThreadLocal) | Automático |

### Ejemplo Comparativo

#### ThreadLocal (Antiguo)

```java
public class ThreadLocalExample {
    private static ThreadLocal<String> userId = new ThreadLocal<>();

    public void process(String id) {
        userId.set(id);
        try {
            doWork();  // Si falla, userId no se limpia
        } finally {
            userId.remove();  // Fácil de olvidar
        }
    }

    private void doWork() {
        String id = userId.get();
        // Puede ser null si olvidaste set()
        if (id != null) {
            // ...
        }
    }
}
```

#### Scoped Values (Nuevo)

```java
public class ScopedValueExample {
    private static final ScopedValue<String> USER_ID = ScopedValue.newInstance();

    public void process(String id) {
        ScopedValue.where(USER_ID, id)
            .run(() -> {
                doWork();  // Limpieza automática, incluso si falla
            });
        // USER_ID no disponible aquí - sin memory leaks
    }

    private void doWork() {
        String id = USER_ID.get();  // Siempre disponible en el scope
        // ...
    }
}
```

## Integración con Virtual Threads

Scoped Values está diseñado específicamente para trabajar eficientemente con Virtual Threads:

```java
public class VirtualThreadsExample {
    private static final ScopedValue<String> CONTEXT = ScopedValue.newInstance();

    public void processWithMillionsOfThreads() {
        ScopedValue.where(CONTEXT, "shared-context")
            .run(() -> {
                // Crear millones de virtual threads
                try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    for (int i = 0; i < 1_000_000; i++) {
                        executor.submit(() -> {
                            // Cada virtual thread tiene acceso al CONTEXT
                            // Sin overhead de memoria significativo
                            String ctx = CONTEXT.get();
                            processTask(ctx);
                        });
                    }
                }
            });
    }
}
```

### ¿Por qué Scoped Values es mejor para Virtual Threads?

1. **Memoria**: ThreadLocal copia valores para cada thread (millones de copias)
2. **Rendimiento**: Scoped Values usa una estructura de datos más eficiente
3. **Herencia**: Propagación automática a threads hijos sin overhead

## Mejores Prácticas

### 1. Declarar como `static final`

```java
// ✅ Correcto
public static final ScopedValue<String> USER_ID = ScopedValue.newInstance();

// ❌ Incorrecto
public ScopedValue<String> userId = ScopedValue.newInstance();
```

### 2. Usar Nombres Descriptivos

```java
// ✅ Correcto
public static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
public static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();

// ❌ Incorrecto
public static final ScopedValue<String> VALUE = ScopedValue.newInstance();
public static final ScopedValue<Object> DATA = ScopedValue.newInstance();
```

### 3. Verificar Disponibilidad

```java
// ✅ Correcto - con verificación
if (USER_ID.isBound()) {
    String id = USER_ID.get();
} else {
    String id = "anonymous";
}

// O usar orElse
String id = USER_ID.orElse("anonymous");

// ❌ Arriesgado - puede lanzar NoSuchElementException
String id = USER_ID.get();
```

### 4. Mantener Scopes Pequeños

```java
// ✅ Correcto - scope limitado
public void processRequest(Request request) {
    ScopedValue.where(REQUEST_ID, request.getId())
        .run(() -> {
            handleRequest(request);
        });
}

// ❌ Incorrecto - scope demasiado amplio
public void processRequest(Request request) {
    ScopedValue.where(REQUEST_ID, request.getId())
        .run(() -> {
            // Mucho código aquí
            // Difícil de razonar sobre el scope
        });
}
```

## Rendimiento

### Benchmarks Aproximados

```
ThreadLocal.get():       ~10 ns
ScopedValue.get():       ~5 ns  (2x más rápido)

ThreadLocal memory:      40 bytes por thread
ScopedValue memory:      Compartido entre threads (mucho menor)

Con 1M virtual threads:
ThreadLocal:             ~40 MB
ScopedValues:            ~1 MB (40x menos memoria)
```

## Referencias

### JEP y Documentación Oficial
- [JEP 481: Scoped Values (Third Preview)](https://openjdk.org/jeps/481)
- [JEP 446: Scoped Values (Preview)](https://openjdk.org/jeps/446)
- [Java 25 Scoped Values API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ScopedValue.html)

### Artículos y Tutoriales
- [Scoped Values in Java - Baeldung](https://www.baeldung.com/java-scoped-values)
- [Modern Java Concurrency - Scoped Values](https://inside.java/tag/scoped-values)

### Ejemplos en el Proyecto
- [`ScopedValuesDemo.java`](../src/main/java/com/monghit/java25/features/ScopedValuesDemo.java)

---

[⬅️ Volver al README](../README.md)
