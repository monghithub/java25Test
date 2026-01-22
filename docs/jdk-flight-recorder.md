# JDK Flight Recorder Enhancements (Java 25)

## Índice
- [¿Qué es JDK Flight Recorder?](#qué-es-jdk-flight-recorder)
- [Novedades en Java 25](#novedades-en-java-25)
- [Conceptos Básicos](#conceptos-básicos)
- [Uso Básico](#uso-básico)
- [Eventos Personalizados](#eventos-personalizados)
- [Streaming API](#streaming-api)
- [Análisis de Datos](#análisis-de-datos)
- [Ejemplos Prácticos](#ejemplos-prácticos)
- [Mejores Prácticas](#mejores-prácticas)
- [Referencias](#referencias)

## ¿Qué es JDK Flight Recorder?

**JDK Flight Recorder (JFR)** es un framework de profiling y diagnóstico de bajo overhead integrado en la JVM desde Java 11. Permite grabar eventos de la JVM y de aplicaciones con un impacto mínimo en el rendimiento.

### Características Principales

- ✅ **Bajo overhead**: < 1% de impacto en producción
- ✅ **Always-on**: Puede estar habilitado permanentemente
- ✅ **Eventos detallados**: GC, threads, I/O, allocaciones, locks, etc.
- ✅ **Custom events**: Define tus propios eventos de aplicación
- ✅ **Streaming**: Consume eventos en tiempo real
- ✅ **Análisis post-mortem**: Graba para análisis posterior

## Novedades en Java 25

### 1. Eventos Mejorados

```java
// Nuevos eventos automáticos en Java 25:
// - Virtual Thread events (creación, park, unpark)
// - Scoped Values events
// - Structured Concurrency events
// - Memory allocation tracking mejorado
```

### 2. Mejor Integración con Virtual Threads

```java
// JFR ahora rastrea virtual threads de forma más eficiente
// - Lifecycle events
// - Pinning events (cuando virtual thread bloquea carrier thread)
// - Scheduling events
```

### 3. Streaming API Mejorado

```java
// API más ergonómica para streaming
// - Mejor filtrado de eventos
// - Reducción de latencia
// - Consumo de recursos optimizado
```

### 4. Configuraciones Predefinidas Mejoradas

```bash
# Nuevos perfiles optimizados
-XX:StartFlightRecording=settings=profile  # Balanced
-XX:StartFlightRecording=settings=detailed # Más eventos
```

## Conceptos Básicos

### 1. Eventos

Los eventos son el elemento fundamental de JFR. Representan algo que ocurrió en la JVM:

```java
// Tipos de eventos:
// - Instant events: Ocurren en un momento específico
// - Duration events: Tienen inicio y fin
// - Sample events: Tomados periódicamente
```

**Categorías de eventos:**
- **JVM Internals**: GC, Class loading, JIT compilation
- **Runtime**: Thread creation, Exceptions, Locks
- **Operating System**: CPU, Memory, I/O
- **Application**: Custom events definidos por usuario

### 2. Recordings

Una grabación es una sesión de recolección de eventos:

```java
// Tipos de recordings:
// - Time-fixed: Duración específica
// - Continuous: Hasta que se detenga manualmente
// - Profiling: Para development/staging
// - Production: Minimal overhead para producción
```

### 3. Configuración

Controla qué eventos grabar y con qué detalle:

```
default.jfc   - Minimal overhead (~1%)
profile.jfc   - Más detalle (~2%)
custom.jfc    - Configuración personalizada
```

## Uso Básico

### Habilitar JFR desde Línea de Comandos

```bash
# Grabar por 60 segundos
java -XX:StartFlightRecording=duration=60s,filename=recording.jfr MyApp

# Grabar continuamente
java -XX:StartFlightRecording=filename=recording.jfr MyApp

# Con configuración específica
java -XX:StartFlightRecording=settings=profile,filename=recording.jfr MyApp

# Múltiples opciones
java -XX:StartFlightRecording=duration=120s,filename=app.jfr,settings=profile MyApp
```

### Controlar JFR en Runtime

```bash
# Usando jcmd (JDK Command)

# Ver recordings activos
jcmd <pid> JFR.check

# Iniciar recording
jcmd <pid> JFR.start name=MyRecording settings=profile

# Detener recording y guardar
jcmd <pid> JFR.dump name=MyRecording filename=recording.jfr

# Detener recording
jcmd <pid> JFR.stop name=MyRecording
```

### Usar JFR Programáticamente

```java
import jdk.jfr.Recording;
import jdk.jfr.Configuration;

public class JFRDemo {
    public void recordApplication() throws Exception {
        // Crear recording con configuración default
        try (Recording recording = new Recording()) {
            // Configurar
            recording.setMaxAge(Duration.ofMinutes(10));
            recording.setMaxSize(100_000_000); // 100 MB

            // Iniciar
            recording.start();

            // Ejecutar tu aplicación
            runApplication();

            // Detener y guardar
            recording.dump(Path.of("recording.jfr"));
        }
    }

    private void runApplication() {
        // Tu código aquí
    }
}
```

## Eventos Personalizados

### Crear Eventos Custom

```java
import jdk.jfr.*;

@Name("com.myapp.OrderProcessed")
@Label("Order Processed")
@Description("Evento cuando se procesa una orden")
@Category("Application")
public class OrderProcessedEvent extends Event {
    @Label("Order ID")
    public String orderId;

    @Label("Amount")
    public double amount;

    @Label("Customer ID")
    public String customerId;

    @Label("Processing Time")
    @Timespan(Timespan.MILLISECONDS)
    public long processingTime;
}
```

### Usar Eventos Custom

```java
public class OrderService {
    public void processOrder(Order order) {
        // Crear evento
        OrderProcessedEvent event = new OrderProcessedEvent();
        event.begin(); // Iniciar medición de tiempo

        try {
            // Procesar orden
            event.orderId = order.getId();
            event.customerId = order.getCustomerId();
            event.amount = order.getAmount();

            // Lógica de procesamiento
            doProcessOrder(order);

        } finally {
            event.end(); // Finalizar y grabar tiempo

            if (event.shouldCommit()) {
                event.processingTime = event.getDuration();
                event.commit(); // Grabar evento
            }
        }
    }

    private void doProcessOrder(Order order) {
        // Implementación
    }
}
```

### Eventos con Configuración

```java
@Name("com.myapp.DatabaseQuery")
@Label("Database Query")
@StackTrace(false)  // No capturar stack trace
@Threshold("100 ms")  // Solo si dura > 100ms
public class DatabaseQueryEvent extends Event {
    @Label("Query")
    public String query;

    @Label("Rows Returned")
    public int rowCount;

    @Label("Execution Time")
    @Timespan(Timespan.MILLISECONDS)
    public long executionTime;
}

// Uso
public List<User> queryUsers(String sql) {
    DatabaseQueryEvent event = new DatabaseQueryEvent();
    event.begin();

    try {
        event.query = sql;
        List<User> results = jdbcTemplate.query(sql, userMapper);
        event.rowCount = results.size();
        return results;
    } finally {
        event.end();
        if (event.shouldCommit()) {
            event.executionTime = event.getDuration();
            event.commit();
        }
    }
}
```

## Streaming API

### Consumir Eventos en Tiempo Real

```java
import jdk.jfr.consumer.*;

public class JFRStreamingDemo {
    public void monitorGarbageCollection() throws Exception {
        // Abrir stream desde recording activo
        try (RecordingStream stream = new RecordingStream()) {
            // Suscribirse a eventos de GC
            stream.onEvent("jdk.GarbageCollection", event -> {
                System.out.printf("GC occurred: %s, duration: %d ms%n",
                    event.getString("name"),
                    event.getDuration().toMillis());
            });

            // Suscribirse a allocaciones grandes
            stream.onEvent("jdk.ObjectAllocationSample", event -> {
                long size = event.getLong("weight");
                if (size > 1_000_000) { // > 1 MB
                    System.out.printf("Large allocation: %d bytes of %s%n",
                        size,
                        event.getClass("objectClass").getName());
                }
            });

            // Habilitar eventos
            stream.enable("jdk.GarbageCollection");
            stream.enable("jdk.ObjectAllocationSample")
                  .withStackTrace();

            // Iniciar streaming
            stream.start();
        }
    }
}
```

### Filtrar y Procesar Eventos

```java
public class EventProcessor {
    public void monitorApplicationPerformance() throws Exception {
        try (RecordingStream stream = new RecordingStream()) {
            // Monitorear eventos custom
            stream.onEvent("com.myapp.OrderProcessed", event -> {
                double amount = event.getDouble("amount");
                long processingTime = event.getLong("processingTime");

                // Alertar si procesamiento es lento
                if (processingTime > 5000) { // > 5 segundos
                    System.err.printf("SLOW ORDER: %s took %d ms%n",
                        event.getString("orderId"),
                        processingTime);
                }

                // Agregar métricas
                updateMetrics(amount, processingTime);
            });

            // Monitorear excepciones
            stream.onEvent("jdk.ExceptionThrown", event -> {
                String message = event.getString("message");
                String thrownClass = event.getClass("thrownClass").getName();

                System.err.printf("Exception: %s - %s%n",
                    thrownClass, message);
            });

            stream.enable("com.myapp.OrderProcessed");
            stream.enable("jdk.ExceptionThrown");

            stream.start();
        }
    }

    private void updateMetrics(double amount, long processingTime) {
        // Actualizar métricas de monitoreo
    }
}
```

### Streaming con Virtual Threads

```java
public class VirtualThreadMonitor {
    public void monitorVirtualThreads() throws Exception {
        try (RecordingStream stream = new RecordingStream()) {
            // Monitorear creación de virtual threads
            stream.onEvent("jdk.VirtualThreadStart", event -> {
                System.out.printf("Virtual thread started: %s%n",
                    event.getThread("eventThread").getJavaName());
            });

            // Monitorear pinning (problema de performance)
            stream.onEvent("jdk.VirtualThreadPinned", event -> {
                System.err.printf("WARNING: Virtual thread pinned for %d ms%n",
                    event.getDuration().toMillis());
            });

            stream.enable("jdk.VirtualThreadStart");
            stream.enable("jdk.VirtualThreadPinned")
                  .withThreshold(Duration.ofMillis(20));

            stream.start();
        }
    }
}
```

## Análisis de Datos

### Leer Recording File

```java
import jdk.jfr.consumer.*;
import java.nio.file.Path;

public class RecordingAnalyzer {
    public void analyzeRecording(Path recordingFile) throws Exception {
        try (RecordingFile recording = new RecordingFile(recordingFile)) {
            while (recording.hasMoreEvents()) {
                RecordedEvent event = recording.readEvent();

                if (event.getEventType().getName().equals("jdk.GarbageCollection")) {
                    analyzeGCEvent(event);
                } else if (event.getEventType().getName().equals("jdk.ObjectAllocationSample")) {
                    analyzeAllocationEvent(event);
                }
            }
        }
    }

    private void analyzeGCEvent(RecordedEvent event) {
        String gcName = event.getString("name");
        Duration duration = event.getDuration();
        long sumOfPauses = event.getLong("sumOfPauses");

        System.out.printf("GC: %s, duration: %d ms, pauses: %d ms%n",
            gcName, duration.toMillis(), sumOfPauses / 1_000_000);
    }

    private void analyzeAllocationEvent(RecordedEvent event) {
        RecordedClass allocatedClass = event.getClass("objectClass");
        long size = event.getLong("weight");

        System.out.printf("Allocation: %s, size: %d bytes%n",
            allocatedClass.getName(), size);
    }
}
```

### Generar Estadísticas

```java
public class JFRStatistics {
    public void generateStats(Path recordingFile) throws Exception {
        Map<String, Long> gcCounts = new HashMap<>();
        Map<String, Duration> gcDurations = new HashMap<>();
        long totalAllocations = 0;

        try (RecordingFile recording = new RecordingFile(recordingFile)) {
            while (recording.hasMoreEvents()) {
                RecordedEvent event = recording.readEvent();

                switch (event.getEventType().getName()) {
                    case "jdk.GarbageCollection" -> {
                        String gcName = event.getString("name");
                        gcCounts.merge(gcName, 1L, Long::sum);
                        gcDurations.merge(gcName, event.getDuration(), Duration::plus);
                    }
                    case "jdk.ObjectAllocationSample" -> {
                        totalAllocations += event.getLong("weight");
                    }
                }
            }
        }

        // Imprimir estadísticas
        System.out.println("\n=== GC Statistics ===");
        gcCounts.forEach((name, count) -> {
            Duration totalDuration = gcDurations.get(name);
            System.out.printf("%s: %d occurrences, total time: %d ms%n",
                name, count, totalDuration.toMillis());
        });

        System.out.printf("\nTotal allocations: %d MB%n",
            totalAllocations / 1024 / 1024);
    }
}
```

## Ejemplos Prácticos

### Ejemplo 1: Profiling de Método

```java
@Name("com.myapp.MethodProfiling")
@Label("Method Execution")
@Category("Application")
public class MethodProfilingEvent extends Event {
    @Label("Method Name")
    public String methodName;

    @Label("Execution Time")
    @Timespan(Timespan.MILLISECONDS)
    public long executionTime;
}

public class ProfiledService {
    public void expensiveOperation(String param) {
        MethodProfilingEvent event = new MethodProfilingEvent();
        event.begin();

        try {
            // Operación costosa
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            event.end();
            event.methodName = "expensiveOperation";
            event.executionTime = event.getDuration();
            event.commit();
        }
    }
}
```

### Ejemplo 2: Monitoreo de Recursos

```java
public class ResourceMonitor {
    public void startMonitoring() throws Exception {
        try (RecordingStream stream = new RecordingStream()) {
            // CPU usage
            stream.onEvent("jdk.CPULoad", event -> {
                float jvmUser = event.getFloat("jvmUser");
                float machineTotal = event.getFloat("machineTotal");

                if (machineTotal > 0.8) {
                    System.err.printf("HIGH CPU: JVM: %.1f%%, Total: %.1f%%%n",
                        jvmUser * 100, machineTotal * 100);
                }
            });

            // Memory usage
            stream.onEvent("jdk.GCHeapSummary", event -> {
                long heapUsed = event.getLong("heapUsed");
                long heapMax = event.getLong("heapSpace.committedSize");

                double usagePercent = (double) heapUsed / heapMax * 100;

                if (usagePercent > 90) {
                    System.err.printf("HIGH MEMORY: %.1f%% used%n", usagePercent);
                }
            });

            stream.enable("jdk.CPULoad").withPeriod(Duration.ofSeconds(1));
            stream.enable("jdk.GCHeapSummary");

            stream.start();
        }
    }
}
```

### Ejemplo 3: Auditoría de Seguridad

```java
@Name("com.myapp.SecurityEvent")
@Label("Security Event")
@Category("Security")
public class SecurityEvent extends Event {
    @Label("Event Type")
    public String eventType;

    @Label("User")
    public String user;

    @Label("Resource")
    public String resource;

    @Label("Success")
    public boolean success;
}

public class SecurityAuditor {
    public void logAccessAttempt(String user, String resource, boolean success) {
        SecurityEvent event = new SecurityEvent();
        event.eventType = "ACCESS_ATTEMPT";
        event.user = user;
        event.resource = resource;
        event.success = success;
        event.commit();

        // También grabar a log tradicional
        if (!success) {
            logger.warn("Failed access attempt: user={}, resource={}",
                user, resource);
        }
    }

    public void monitorSecurityEvents() throws Exception {
        try (RecordingStream stream = new RecordingStream()) {
            stream.onEvent("com.myapp.SecurityEvent", event -> {
                if (!event.getBoolean("success")) {
                    // Alerta de seguridad
                    alertSecurityTeam(
                        event.getString("user"),
                        event.getString("resource")
                    );
                }
            });

            stream.enable("com.myapp.SecurityEvent");
            stream.start();
        }
    }

    private void alertSecurityTeam(String user, String resource) {
        // Enviar alerta
    }
}
```

## Mejores Prácticas

### 1. Minimizar Overhead

```java
// ✅ Correcto - verificar antes de grabar
event.begin();
try {
    operation();
} finally {
    event.end();
    if (event.shouldCommit()) {  // Solo si pasa threshold
        event.commit();
    }
}

// ❌ Evitar - grabar siempre
event.begin();
operation();
event.end();
event.commit();  // Graba aunque no sea necesario
```

### 2. Usar Thresholds

```java
// ✅ Correcto - solo eventos lentos
@Threshold("100 ms")
public class SlowOperationEvent extends Event {
    // Solo graba si dura > 100ms
}

// ❌ Evitar - todos los eventos
public class OperationEvent extends Event {
    // Graba TODO, mucho overhead
}
```

### 3. Categorizar Eventos

```java
// ✅ Correcto - bien categorizado
@Category({"Application", "Business", "Orders"})
public class OrderEvent extends Event { }

@Category({"Application", "Performance"})
public class PerformanceEvent extends Event { }

// Fácil de filtrar y analizar
```

### 4. Nombres Descriptivos

```java
// ✅ Correcto
@Name("com.mycompany.myapp.OrderProcessed")
@Label("Order Processed")
public class OrderProcessedEvent extends Event { }

// ❌ Evitar
@Name("Event1")
@Label("E1")
public class E1 extends Event { }
```

## Herramientas de Análisis

### JDK Mission Control (JMC)

```bash
# Descargar JMC
# https://jdk.java.net/jmc/

# Abrir recording
jmc recording.jfr

# JMC proporciona:
# - Visualización gráfica de eventos
# - Análisis de GC
# - Thread analysis
# - Method profiling
# - Memory leak detection
```

### Análisis con jfr CLI

```bash
# Print eventos
jfr print recording.jfr

# Print eventos específicos
jfr print --events jdk.GarbageCollection recording.jfr

# Generar resumen
jfr summary recording.jfr

# Filtrar por categoría
jfr print --categories "GC" recording.jfr
```

## Referencias

### Documentación Oficial
- [JDK Flight Recorder Guide](https://docs.oracle.com/en/java/javase/25/jfapi/)
- [JFR API Documentation](https://docs.oracle.com/en/java/javase/25/docs/api/jdk.jfr/jdk/jfr/package-summary.html)
- [Java Flight Recorder Events](https://bestsolution-at.github.io/jfr-doc/)

### Herramientas
- [JDK Mission Control](https://jdk.java.net/jmc/) - GUI para análisis de JFR
- [JFR Analytics](https://github.com/flight-recorder/jfr-analytics) - Analytics framework

### Artículos
- [Getting Started with JFR](https://www.baeldung.com/java-flight-recorder-monitoring)
- [JFR Streaming API](https://inside.java/2020/11/09/streaming-jfr/)
- [Custom JFR Events](https://www.morling.dev/blog/rest-api-monitoring-with-custom-jdk-flight-recorder-events/)

### Ejemplos en el Proyecto
- [`JFRDemo.java`](../src/main/java/com/monghit/java25/features/JFRDemo.java)

---

[⬅️ Volver al README](../README.md)
