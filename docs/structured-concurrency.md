# Structured Concurrency (JEP 505 - Preview)

## Índice
- [¿Qué es Structured Concurrency?](#qué-es-structured-concurrency)
- [Problemas que Resuelve](#problemas-que-resuelve)
- [Conceptos Fundamentales](#conceptos-fundamentales)
- [API Básica](#api-básica)
- [Patrones Comunes](#patrones-comunes)
- [Ejemplos Prácticos](#ejemplos-prácticos)
- [Ventajas](#ventajas)
- [Limitaciones y Consideraciones](#limitaciones-y-consideraciones)
- [Referencias](#referencias)

## ¿Qué es Structured Concurrency?

**Structured Concurrency** es un paradigma de programación concurrente que trata las tareas paralelas como una única unidad de trabajo con ciclo de vida bien definido. En Java 25 (JEP 505 - Preview), esto se implementa mediante la API `StructuredTaskScope`.

### Principio Core

> "Las subtareas no viven más que su tarea padre"

Todos los threads hijos se completan o cancelan antes de que el padre continúe.

## Problemas que Resuelve

### Problema 1: Thread Leaks

**Antes (Código tradicional):**
```java
ExecutorService executor = Executors.newFixedThreadPool(10);

Future<String> future1 = executor.submit(() -> fetchUser());
Future<String> future2 = executor.submit(() -> fetchOrders());

// Si hay excepción aquí, los threads pueden quedar colgados
String user = future1.get();
String orders = future2.get();

executor.shutdown();  // Fácil de olvidar
```

**Problemas:**
- Los threads pueden seguir ejecutándose después de un error
- Fácil olvidar hacer shutdown
- Memory leaks
- Recursos no liberados

### Problema 2: Error Handling Complejo

```java
Future<String> f1 = executor.submit(task1);
Future<String> f2 = executor.submit(task2);

try {
    String r1 = f1.get();
    String r2 = f2.get();
} catch (Exception e) {
    // ¿Cómo cancelo las otras tareas?
    f1.cancel(true);
    f2.cancel(true);
    // ¿Están realmente canceladas?
}
```

### Solución: Structured Concurrency

```java
try (var scope = StructuredTaskScope.open()) {
    var task1 = scope.fork(() -> fetchUser());
    var task2 = scope.fork(() -> fetchOrders());

    scope.join();  // Espera a que TODAS completen o fallen

    String user = task1.get();
    String orders = task2.get();
}
// Al salir del try-with-resources, GARANTIZADO que todos los threads han terminado
```

## Conceptos Fundamentales

### 1. StructuredTaskScope

Un "scope" que agrupa tareas relacionadas:

```java
// Crear un scope
try (var scope = StructuredTaskScope.open()) {
    // Forkar tareas
    Subtask<String> task1 = scope.fork(() -> operation1());
    Subtask<String> task2 = scope.fork(() -> operation2());

    // Esperar completación
    scope.join();

    // Procesar resultados
    String result1 = task1.get();
    String result2 = task2.get();
}
```

### 2. Subtask

Representa una tarea individual dentro del scope:

```java
Subtask<String> task = scope.fork(() -> {
    return "resultado";
});

// Después de join()
task.state();  // State: UNAVAILABLE, SUCCESS, FAILED
task.get();    // Resultado (si SUCCESS)
task.exception();  // Excepción (si FAILED)
```

### 3. Join Policies (Políticas de Join)

Define cómo el scope espera a las tareas:

```java
// Espera a TODAS las tareas
scope.join();

// Espera hasta deadline
scope.joinUntil(Instant.now().plusSeconds(5));
```

### 4. Shutdown Policies

**NOTA IMPORTANTE**: En Java 25, la API cambió. Las clases `ShutdownOnFailure` y `ShutdownOnSuccess` ya no existen como clases internas. La API está en evolución.

## API Básica

### Crear y Usar un Scope

```java
// API básica
try (var scope = StructuredTaskScope.open()) {
    // Fork tareas
    Subtask<String> task = scope.fork(() -> compute());

    // Esperar
    scope.join();

    // Obtener resultado
    if (task.state() == Subtask.State.SUCCESS) {
        String result = task.get();
    }
}
```

### Fork (Iniciar Tarea)

```java
Subtask<ReturnType> task = scope.fork(Callable<ReturnType> callable);
Subtask<Void> task = scope.fork(Runnable runnable);
```

### Join (Esperar)

```java
// Esperar indefinidamente
scope.join();

// Esperar con timeout
scope.joinUntil(Instant.now().plusSeconds(5));
```

## Patrones Comunes

### Patrón 1: Parallel Aggregation

Ejecutar múltiples tareas y agregar resultados:

```java
public Summary fetchSummary(String id) throws Exception {
    try (var scope = StructuredTaskScope.open()) {
        var userTask = scope.fork(() -> fetchUser(id));
        var ordersTask = scope.fork(() -> fetchOrders(id));
        var prefsTask = scope.fork(() -> fetchPreferences(id));

        scope.join();

        return new Summary(
            userTask.get(),
            ordersTask.get(),
            prefsTask.get()
        );
    }
}
```

### Patrón 2: First Success

Ejecutar múltiples tareas y usar el primer resultado exitoso:

```java
public String fetchFromFastest(String query) throws Exception {
    try (var scope = StructuredTaskScope.open()) {
        var db = scope.fork(() -> queryDatabase(query));
        var cache = scope.fork(() -> queryCache(query));
        var api = scope.fork(() -> queryAPI(query));

        // Esperar a que al menos una complete
        scope.join();

        // Retornar el primero que completó exitosamente
        if (cache.state() == Subtask.State.SUCCESS) return cache.get();
        if (db.state() == Subtask.State.SUCCESS) return db.get();
        if (api.state() == Subtask.State.SUCCESS) return api.get();

        throw new Exception("Todas las fuentes fallaron");
    }
}
```

### Patrón 3: Fan-out/Fan-in

Procesar una colección en paralelo:

```java
public List<Result> processAll(List<Item> items) throws Exception {
    try (var scope = StructuredTaskScope.open()) {
        // Fan-out: crear tarea por cada item
        List<Subtask<Result>> tasks = items.stream()
            .map(item -> scope.fork(() -> process(item)))
            .collect(Collectors.toList());

        // Esperar todas
        scope.join();

        // Fan-in: recolectar resultados
        return tasks.stream()
            .filter(task -> task.state() == Subtask.State.SUCCESS)
            .map(Subtask::get)
            .collect(Collectors.toList());
    }
}
```

## Ejemplos Prácticos

### Ejemplo 1: Microservicios - Llamadas Paralelas

```java
public class OrderService {
    public OrderDetails getOrderDetails(String orderId) throws Exception {
        try (var scope = StructuredTaskScope.open()) {
            // Llamar a múltiples servicios en paralelo
            var orderTask = scope.fork(() ->
                orderClient.getOrder(orderId));

            var customerTask = scope.fork(() -> {
                String customerId = orderTask.get().getCustomerId();
                return customerClient.getCustomer(customerId);
            });

            var inventoryTask = scope.fork(() ->
                inventoryClient.checkAvailability(orderId));

            var shippingTask = scope.fork(() ->
                shippingClient.getShippingInfo(orderId));

            // Esperar todas las llamadas
            scope.join();

            // Construir respuesta agregada
            return new OrderDetails(
                orderTask.get(),
                customerTask.get(),
                inventoryTask.get(),
                shippingTask.get()
            );
        }
    }
}
```

### Ejemplo 2: Data Pipeline con Virtual Threads

```java
public class DataPipeline {
    public void processLargeDataset(List<Data> dataset) throws Exception {
        try (var scope = StructuredTaskScope.open(
            ThreadFactory.ofVirtual().factory())) {

            // Crear un virtual thread por cada elemento
            List<Subtask<ProcessedData>> tasks = dataset.stream()
                .map(data -> scope.fork(() -> {
                    // Cada paso del pipeline
                    Data validated = validate(data);
                    Data enriched = enrich(validated);
                    Data transformed = transform(enriched);
                    return new ProcessedData(transformed);
                }))
                .collect(Collectors.toList());

            // Esperar procesamiento completo
            scope.join();

            // Recolectar y guardar resultados
            List<ProcessedData> results = tasks.stream()
                .filter(t -> t.state() == Subtask.State.SUCCESS)
                .map(Subtask::get)
                .collect(Collectors.toList());

            saveResults(results);
        }
    }
}
```

### Ejemplo 3: Timeout y Fallback

```java
public String fetchWithTimeout(String id, Duration timeout) {
    try (var scope = StructuredTaskScope.open()) {
        var task = scope.fork(() -> expensiveOperation(id));

        // Esperar con timeout
        scope.joinUntil(Instant.now().plus(timeout));

        if (task.state() == Subtask.State.SUCCESS) {
            return task.get();
        } else {
            // Timeout o falló - usar fallback
            return fetchFromCache(id);
        }
    } catch (Exception e) {
        return getDefaultValue();
    }
}
```

## Ventajas

### 1. Safety (Seguridad)

- ✅ **No thread leaks**: Garantiza que todos los threads terminan
- ✅ **Resource management**: Automático con try-with-resources
- ✅ **Error propagation**: Los errores se propagan correctamente

### 2. Clarity (Claridad)

- ✅ **Código más legible**: Estructura clara padre-hijo
- ✅ **Intención explícita**: Se ve claramente qué tareas son paralelas
- ✅ **Debugging más fácil**: Stack traces más claros

### 3. Performance

- ✅ **Optimizado para Virtual Threads**: Millones de tareas concurrentes
- ✅ **Bajo overhead**: Mínimo costo de coordinación
- ✅ **Cancellation eficiente**: Cancela tareas automáticamente

### 4. Composición

- ✅ **Scopes anidados**: Jerarquía clara de tareas
- ✅ **Integración con Scoped Values**: Contexto compartido automático
- ✅ **Reutilizable**: Patrones componibles

## Limitaciones y Consideraciones

### 1. Preview Feature

```bash
# Requiere flag de preview
java --enable-preview YourApp
javac --enable-preview --release 25 YourApp.java
```

### 2. API en Evolución

La API ha cambiado significativamente entre versiones:
- Java 19-20: Incubator
- Java 21-24: Preview con cambios
- Java 25: Preview con API revisada

**Cambio importante en Java 25:**
```java
// Java 21-24 (Ya no funciona)
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    // ...
}

// Java 25 (Nueva API)
try (var scope = StructuredTaskScope.open()) {
    // ...
}
```

### 3. Exception Handling

```java
try (var scope = StructuredTaskScope.open()) {
    var task = scope.fork(() -> mightFail());

    scope.join();

    // Debe verificar estado manualmente
    if (task.state() == Subtask.State.FAILED) {
        Throwable exception = task.exception();
        // Manejar error
    }
}
```

### 4. Orden de Ejecución

No hay garantía del orden en que las tareas completan:

```java
var task1 = scope.fork(() -> operation1());  // Puede completar segundo
var task2 = scope.fork(() -> operation2());  // Puede completar primero

scope.join();
// No asumas nada sobre el orden de completación
```

## Mejores Prácticas

### 1. Usar try-with-resources

```java
// ✅ Correcto
try (var scope = StructuredTaskScope.open()) {
    // ...
}

// ❌ Incorrecto - puede causar leaks
var scope = StructuredTaskScope.open();
// ...
scope.close();  // Fácil de olvidar
```

### 2. Verificar Estados

```java
// ✅ Correcto
scope.join();
if (task.state() == Subtask.State.SUCCESS) {
    result = task.get();
} else {
    // Manejar error
}

// ❌ Arriesgado
result = task.get();  // Puede lanzar IllegalStateException
```

### 3. Scopes Pequeños

```java
// ✅ Correcto - scope enfocado
try (var scope = StructuredTaskScope.open()) {
    var t1 = scope.fork(() -> operation1());
    var t2 = scope.fork(() -> operation2());
    scope.join();
    return combine(t1.get(), t2.get());
}

// ❌ Problemático - scope muy grande
try (var scope = StructuredTaskScope.open()) {
    // Demasiadas operaciones
    // Difícil de razonar
}
```

## Referencias

### JEP y Documentación Oficial
- [JEP 505: Structured Concurrency (Fifth Preview)](https://openjdk.org/jeps/505)
- [JEP 462: Structured Concurrency (Second Preview)](https://openjdk.org/jeps/462)
- [Structured Concurrency API Docs](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/StructuredTaskScope.html)

### Artículos y Tutoriales
- [Structured Concurrency in JDK 25 - Rock the JVM](https://rockthejvm.com/articles/structured-concurrency-jdk-25)
- [Structured Concurrency Guide](https://www.happycoders.eu/java/structured-concurrency-structuredtaskscope/)

### Ejemplos en el Proyecto
- [`StructuredConcurrencyDemo.java`](../src/main/java/com/monghit/java25/features/StructuredConcurrencyDemo.java)

---

[⬅️ Volver al README](../README.md)
