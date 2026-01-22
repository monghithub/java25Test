# Stable Values (JEP 572 - Preview)

## Índice
- [¿Qué son los Stable Values?](#qué-son-los-stable-values)
- [Problema que Resuelve](#problema-que-resuelve)
- [Conceptos Clave](#conceptos-clave)
- [API y Uso](#api-y-uso)
- [Ejemplos Prácticos](#ejemplos-prácticos)
- [Comparación con Alternativas](#comparación-con-alternativas)
- [Rendimiento](#rendimiento)
- [Referencias](#referencias)

## ¿Qué son los Stable Values?

**Stable Values** (JEP 572 - Preview) es una API para **inmutabilidad diferida** (deferred immutability). Permite crear valores que se inicializan **una sola vez** de forma **perezosa** (lazy) y **thread-safe**, después de lo cual son completamente **inmutables**.

### Características Principales

- ✅ **Lazy initialization**: Se inicializa solo cuando se accede por primera vez
- ✅ **At-most-once**: Se establece máximo una vez, nunca cambia después
- ✅ **Thread-safe**: Seguro para acceso concurrente sin sincronización adicional
- ✅ **Performance**: Más eficiente que volatile o AtomicReference
- ✅ **Memoria eficiente**: Mejor que inicialización eager

## Problema que Resuelve

### Problema 1: Inicialización Costosa

```java
// Problema: Se inicializa siempre, aunque no se use
public class Logger {
    private final String config = loadExpensiveConfig();  // Siempre se ejecuta!

    public Logger() {
        // config ya inicializado, cueste lo que cueste
    }
}
```

### Problema 2: Thread Safety Complejo

```java
// Double-checked locking - complejo y propenso a errores
public class Singleton {
    private volatile Instance instance;

    public Instance getInstance() {
        if (instance == null) {  // Primera verificación (sin lock)
            synchronized (this) {
                if (instance == null) {  // Segunda verificación (con lock)
                    instance = createInstance();  // Costoso
                }
            }
        }
        return instance;
    }
}
```

### Solución: Stable Values

```java
public class Logger {
    private final StableValue<String> config = StableValue.of();

    public String getConfig() {
        return config.orElseSet(() -> loadExpensiveConfig());
        // Solo se ejecuta la primera vez, thread-safe automáticamente
    }
}
```

## Conceptos Clave

### 1. Creación

```java
// Crear un StableValue vacío
StableValue<String> value = StableValue.of();

// Crear con tipo específico
StableValue<DatabaseConnection> connection = StableValue.of();
StableValue<Configuration> config = StableValue.of();
```

### 2. Inicialización

```java
// orElseSet: Establece el valor si no está inicializado
String result = value.orElseSet(() -> {
    // Esta función se ejecuta SOLO la primera vez
    System.out.println("Inicializando...");
    return expensiveComputation();
});
```

### 3. Inmutabilidad

```java
StableValue<String> value = StableValue.of();

// Primera llamada: inicializa
String v1 = value.orElseSet(() -> "initial");  // "initial"

// Llamadas subsecuentes: retorna el mismo valor
String v2 = value.orElseSet(() -> "different");  // "initial" (no cambia!)
String v3 = value.orElseSet(() -> "another");    // "initial" (no cambia!)
```

## API y Uso

### Métodos Principales

#### `StableValue.of()`

Crea un nuevo StableValue sin inicializar:

```java
StableValue<String> value = StableValue.of();
```

#### `orElseSet(Supplier<T>)`

Obtiene el valor, inicializándolo si es necesario:

```java
T result = value.orElseSet(() -> {
    // Código de inicialización
    return computeValue();
});
```

### Uso Básico

```java
public class Example {
    private final StableValue<String> lazyValue = StableValue.of();

    public String getValue() {
        return lazyValue.orElseSet(() -> {
            System.out.println("Computing...");
            return "Computed Value";
        });
    }
}

// Uso
Example ex = new Example();
ex.getValue();  // Imprime "Computing...", retorna "Computed Value"
ex.getValue();  // No imprime nada, retorna "Computed Value"
ex.getValue();  // No imprime nada, retorna "Computed Value"
```

## Ejemplos Prácticos

### Ejemplo 1: Lazy Configuration Loading

```java
public class AppConfig {
    private final StableValue<Properties> config = StableValue.of();
    private final StableValue<DatabaseConnection> dbConnection = StableValue.of();

    public Properties getConfig() {
        return config.orElseSet(() -> {
            System.out.println("Loading configuration from file...");
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));
            return props;
        });
    }

    public DatabaseConnection getDbConnection() {
        return dbConnection.orElseSet(() -> {
            System.out.println("Establishing database connection...");
            Properties cfg = getConfig();  // Usa config lazy
            return new DatabaseConnection(
                cfg.getProperty("db.url"),
                cfg.getProperty("db.user"),
                cfg.getProperty("db.password")
            );
        });
    }
}
```

### Ejemplo 2: Singleton Pattern Simplificado

```java
public class ServiceRegistry {
    private static final StableValue<ServiceRegistry> INSTANCE =
        StableValue.of();

    private ServiceRegistry() {
        // Constructor privado
    }

    public static ServiceRegistry getInstance() {
        return INSTANCE.orElseSet(() -> {
            System.out.println("Creating ServiceRegistry instance...");
            return new ServiceRegistry();
        });
    }
}

// Uso - Thread-safe automáticamente
ServiceRegistry reg1 = ServiceRegistry.getInstance();
ServiceRegistry reg2 = ServiceRegistry.getInstance();
// reg1 == reg2 (misma instancia)
```

### Ejemplo 3: Cálculos Costosos

```java
public class DataAnalyzer {
    private final StableValue<Statistics> stats = StableValue.of();
    private final List<Data> data;

    public DataAnalyzer(List<Data> data) {
        this.data = data;
    }

    public Statistics getStatistics() {
        return stats.orElseSet(() -> {
            System.out.println("Computing statistics...");
            // Cálculo costoso que solo se ejecuta una vez
            double mean = calculateMean(data);
            double stdDev = calculateStdDev(data, mean);
            double median = calculateMedian(data);
            return new Statistics(mean, stdDev, median);
        });
    }

    // Los métodos siguientes usan la misma instancia de statistics
    public double getMean() {
        return getStatistics().mean();
    }

    public double getStdDev() {
        return getStatistics().stdDev();
    }
}
```

### Ejemplo 4: Thread Safety en Concurrencia

```java
public class ConcurrentInitializer {
    private final StableValue<String> sharedValue = StableValue.of();

    public void demonstrateConcurrency() throws InterruptedException {
        // Crear múltiples threads que intentan inicializar
        Thread t1 = Thread.ofVirtual().start(() -> {
            String value = sharedValue.orElseSet(() -> {
                System.out.println("Thread 1 inicializando...");
                try { Thread.sleep(100); } catch (Exception e) {}
                return "valor-thread-1";
            });
            System.out.println("Thread 1 obtuvo: " + value);
        });

        Thread t2 = Thread.ofVirtual().start(() -> {
            String value = sharedValue.orElseSet(() -> {
                System.out.println("Thread 2 inicializando...");
                try { Thread.sleep(100); } catch (Exception e) {}
                return "valor-thread-2";
            });
            System.out.println("Thread 2 obtuvo: " + value);
        });

        t1.join();
        t2.join();

        // Solo UN thread inicializa, ambos obtienen el mismo valor
        // Output posible:
        // Thread 1 inicializando...
        // Thread 1 obtuvo: valor-thread-1
        // Thread 2 obtuvo: valor-thread-1
    }
}
```

## Comparación con Alternativas

### 1. vs Eager Initialization

```java
// Eager - Siempre paga el costo
public class EagerExample {
    private final ExpensiveObject obj = new ExpensiveObject();  // ❌ Siempre se crea
}

// Stable Values - Solo si se usa
public class LazyExample {
    private final StableValue<ExpensiveObject> obj = StableValue.of();

    public ExpensiveObject getObj() {
        return obj.orElseSet(() -> new ExpensiveObject());  // ✅ Solo si se usa
    }
}
```

### 2. vs Volatile

```java
// Volatile - No lazy, requiere lógica manual
public class VolatileExample {
    private volatile ExpensiveObject obj;  // ❌ No es lazy

    public VolatileExample() {
        this.obj = new ExpensiveObject();  // Siempre se crea
    }
}

// Stable Values - Lazy automático
public class StableExample {
    private final StableValue<ExpensiveObject> obj = StableValue.of();

    public ExpensiveObject getObj() {
        return obj.orElseSet(() -> new ExpensiveObject());  // ✅ Lazy
    }
}
```

### 3. vs Double-Checked Locking

```java
// Double-checked locking - Complejo y verboso
public class DCLExample {
    private volatile ExpensiveObject obj;

    public ExpensiveObject getObj() {
        if (obj == null) {  // ❌ Complejo
            synchronized (this) {
                if (obj == null) {
                    obj = new ExpensiveObject();
                }
            }
        }
        return obj;
    }
}

// Stable Values - Simple y claro
public class StableExample {
    private final StableValue<ExpensiveObject> obj = StableValue.of();

    public ExpensiveObject getObj() {
        return obj.orElseSet(() -> new ExpensiveObject());  // ✅ Simple
    }
}
```

### 4. vs AtomicReference

```java
// AtomicReference - Mutable, no garantiza once
public class AtomicExample {
    private final AtomicReference<ExpensiveObject> obj =
        new AtomicReference<>();

    public ExpensiveObject getObj() {
        obj.compareAndSet(null, new ExpensiveObject());  // ❌ Puede cambiar
        return obj.get();
    }
}

// Stable Values - Inmutable después de set
public class StableExample {
    private final StableValue<ExpensiveObject> obj = StableValue.of();

    public ExpensiveObject getObj() {
        return obj.orElseSet(() -> new ExpensiveObject());  // ✅ Inmutable
    }
}
```

## Rendimiento

### Benchmarks Aproximados

| Operación | Tiempo | Memoria |
|-----------|--------|---------|
| volatile read | ~5 ns | 8 bytes |
| AtomicReference | ~10 ns | 16 bytes |
| **StableValue** | **~3 ns** | **8 bytes** |
| synchronized | ~25 ns | Variable |

### Ventajas de Rendimiento

1. **Lectura rápida**: Comparable a leer un campo final
2. **Sin overhead de sincronización**: Una vez inicializado
3. **Cache-friendly**: Mejor localidad de datos
4. **Bajo consumo de memoria**: Mínimo overhead

## Mejores Prácticas

### 1. Usar como Final Fields

```java
// ✅ Correcto
public class Service {
    private final StableValue<Config> config = StableValue.of();
}

// ❌ Incorrecto
public class Service {
    private StableValue<Config> config = StableValue.of();  // No final
}
```

### 2. Operaciones Idempotentes

```java
// ✅ Correcto - operación idempotente
config.orElseSet(() -> loadConfig());  // Siempre retorna lo mismo

// ❌ Problemático - no idempotente
config.orElseSet(() -> UUID.randomUUID().toString());  // Diferente cada vez
```

### 3. Evitar Side Effects en Initializer

```java
// ✅ Correcto - sin side effects
value.orElseSet(() -> {
    return computePureValue();
});

// ❌ Problemático - con side effects
value.orElseSet(() -> {
    counter++;  // Side effect!
    return computeValue();
});
```

## Limitaciones

### 1. Preview Feature

Requiere `--enable-preview`:

```bash
javac --enable-preview --release 25 Example.java
java --enable-preview Example
```

### 2. Solo en java.lang

```java
import java.lang.StableValue;  // ✅ Correcto

// NO en jdk.incubator
```

### 3. Renombrado Futuro

En Java 26 se planea renombrar a `LazyConstant` o `ComputedConstant`.

## Referencias

### JEP y Documentación Oficial
- [JEP 572: Stable Values (Preview)](https://openjdk.org/jeps/572)
- [JEP 502: Stable Values](https://openjdk.org/jeps/502)
- [StableValue API Docs](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/StableValue.html)

### Artículos
- [Stable Values in Java 25 | Baeldung](https://www.baeldung.com/java-25-stable-values)
- [Java 25 Stable Values API - InfoQ](https://www.infoq.com/news/2025/06/java25-stable-values-api-startup/)
- [JEP 502 Explained - SoftwareMill](https://softwaremill.com/jep-502-stable-values-new-feature-of-java-25-explained/)

### Ejemplos en el Proyecto
- [`StableValuesDemo.java`](../src/main/java/com/monghit/java25/features/StableValuesDemo.java)

---

[⬅️ Volver al README](../README.md)
