# Compact Object Headers (JEP 519 - Product)

## Índice
- [¿Qué es?](#qué-es)
- [Problema que Resuelve](#problema-que-resuelve)
- [Cómo Funciona](#cómo-funciona)
- [Impacto en Rendimiento](#impacto-en-rendimiento)
- [Beneficios](#beneficios)
- [Limitaciones](#limitaciones)
- [Verificación](#verificación)
- [Referencias](#referencias)

## ¿Qué es?

**Compact Object Headers** (JEP 519 - Product) es una mejora a nivel de JVM que reduce el tamaño del header de los objetos Java de **96 bits a 64 bits** en arquitecturas de 64 bits, mejorando significativamente el uso de memoria y el rendimiento del garbage collector.

### Características Principales

- ✅ **Reducción de memoria**: 25-30% menos memoria por objeto en promedio
- ✅ **Mejor cache locality**: Más objetos caben en la caché del CPU
- ✅ **GC más eficiente**: Menos memoria que escanear durante garbage collection
- ✅ **Transparente**: No requiere cambios en el código de aplicación
- ✅ **Habilitado por defecto**: Activo automáticamente en Java 25

## Problema que Resuelve

### Header de Objeto Tradicional (Hasta Java 24)

Cada objeto Java tiene un header invisible que contiene metadata:

```
┌─────────────────────────────────────────────┐
│  Object Header (96 bits / 12 bytes)         │
├─────────────────────────────────────────────┤
│  Mark Word (64 bits)                        │
│  - Identity hash code                       │
│  - GC age                                   │
│  - Lock state                               │
│  - Bias lock info                           │
├─────────────────────────────────────────────┤
│  Class Pointer (32 bits compressed)         │
│  - Referencia a la clase del objeto         │
├─────────────────────────────────────────────┤
│  Array Length (32 bits, solo arrays)        │
└─────────────────────────────────────────────┘
```

### Problema: Overhead Significativo

```java
// Ejemplo: Array de un millón de objetos pequeños
class Point {
    int x;  // 4 bytes
    int y;  // 4 bytes
}
// Total por objeto:
// - Header: 12 bytes
// - Fields: 8 bytes
// - Padding: 4 bytes (alineación)
// TOTAL: 24 bytes (50% es overhead!)

Point[] points = new Point[1_000_000];
// Overhead total: 12 MB solo en headers
```

### Solución: Compact Object Headers

Reduce el header de **96 bits (12 bytes)** a **64 bits (8 bytes)**:

```
┌─────────────────────────────────────────────┐
│  Compact Header (64 bits / 8 bytes)         │
├─────────────────────────────────────────────┤
│  Bits 0-7:   GC age                         │
│  Bits 8-23:  Identity hash code (parcial)   │
│  Bits 24-55: Class pointer (comprimido)     │
│  Bits 56-63: Lock/monitor state             │
└─────────────────────────────────────────────┘
```

**Ahorro:** 4 bytes por objeto = **33% menos overhead**

## Cómo Funciona

### 1. Compresión del Class Pointer

**Antes:**
```
Class Pointer: 32 bits (comprimido con CompressedOops)
Mark Word: 64 bits
Total: 96 bits
```

**Con Compact Headers:**
```
Toda la información en: 64 bits
- Class pointer más comprimido (32 bits → 24-28 bits)
- Mark word reorganizado y optimizado
```

### 2. Reorganización del Mark Word

La JVM reorganiza cómo almacena:
- **Identity hash code**: Calculado lazy, almacenado fuera del header cuando es necesario
- **GC age**: Reducido de 4 bits a menos bits (suficiente para la mayoría de casos)
- **Lock state**: Optimizado usando lock records externos

### 3. Técnicas de Compresión

#### Compressed Class Pointers

```java
// En Java 24 y anteriores
Object obj = new MyClass();
// Header: 12 bytes (8 + 4)

// En Java 25 con Compact Headers
Object obj = new MyClass();
// Header: 8 bytes
// Ahorro: 33%
```

#### Identity Hash Code Lazy

```java
// Hash code no se calcula hasta que se necesita
Object obj = new Object();
// Header: 8 bytes (sin hash code aún)

int hash = obj.hashCode();
// Ahora se calcula y almacena (potencialmente fuera del header)
```

## Impacto en Rendimiento

### Benchmarks Aproximados

| Métrica | Java 24 | Java 25 | Mejora |
|---------|---------|---------|--------|
| Heap size (app típica) | 1.0 GB | 0.7-0.8 GB | 20-30% |
| GC pause time | 100 ms | 70-85 ms | 15-30% |
| Cache misses | Baseline | -15% | 15% mejora |
| Throughput | Baseline | +5-10% | 5-10% mejora |

### Casos de Uso Beneficiados

#### 1. Aplicaciones con Muchos Objetos Pequeños

```java
// Caso común: DTOs, Value Objects
class User {
    long id;
    String name;
    int age;
}

// Con 1 millón de usuarios:
// Java 24: ~12 MB solo en headers
// Java 25: ~8 MB solo en headers
// Ahorro: 4 MB
```

#### 2. Colecciones Grandes

```java
List<Integer> numbers = new ArrayList<>();
for (int i = 0; i < 1_000_000; i++) {
    numbers.add(i);  // Cada Integer es un objeto
}

// Java 24: ~12 MB en headers de Integer
// Java 25: ~8 MB en headers de Integer
// Ahorro: 4 MB
```

#### 3. Caches en Memoria

```java
Map<String, CachedObject> cache = new HashMap<>();
// Con 100,000 entries
// Ahorro: ~400 KB solo en headers
```

### Gráfico de Impacto

```
Memoria usada por 1M objetos pequeños (8 bytes de datos):

Java 24:  ████████████████████████  24 MB (12 bytes header + 8 bytes data + 4 padding)
Java 25:  ████████████████          16 MB (8 bytes header + 8 bytes data)

Ahorro:   ████████                  8 MB (33% reducción)
```

## Beneficios

### 1. Reducción de Memoria

```java
public class MemoryDemo {
    static class SmallObject {
        int value;
    }

    public static void main(String[] args) {
        List<SmallObject> objects = new ArrayList<>();

        // Crear 10 millones de objetos
        for (int i = 0; i < 10_000_000; i++) {
            objects.add(new SmallObject());
        }

        // Java 24: ~240 MB
        // Java 25: ~160 MB
        // Ahorro: ~80 MB (33%)

        System.gc();
        long memory = Runtime.getRuntime().totalMemory() -
                      Runtime.getRuntime().freeMemory();
        System.out.println("Memory used: " + memory / 1024 / 1024 + " MB");
    }
}
```

### 2. Mejor Rendimiento del GC

```java
// Con headers más pequeños:
// - Menos memoria que escanear
// - Pausas de GC más cortas
// - Menos fragmentación

public class GCDemo {
    public static void main(String[] args) {
        // Crear y descartar muchos objetos
        for (int i = 0; i < 1000; i++) {
            List<Object> temp = new ArrayList<>();
            for (int j = 0; j < 10000; j++) {
                temp.add(new Object());
            }
            // temp se descarta, triggering GC
        }

        // Java 25: GC más rápido debido a headers compactos
    }
}
```

### 3. Cache Efficiency

```java
// Más objetos caben en L1/L2/L3 cache
// = Menos cache misses
// = Mejor rendimiento general

public class CacheDemo {
    static class Node {
        int data;
        Node next;
    }

    public static void main(String[] args) {
        // Crear linked list
        Node head = new Node();
        Node current = head;
        for (int i = 0; i < 1000; i++) {
            current.next = new Node();
            current = current.next;
        }

        // Recorrer lista (cache-sensitive operation)
        long start = System.nanoTime();
        current = head;
        int sum = 0;
        while (current != null) {
            sum += current.data;
            current = current.next;
        }
        long end = System.nanoTime();

        // Java 25: Potencialmente más rápido por mejor cache locality
    }
}
```

### 4. Scaling Mejorado

```java
// Puedes tener más objetos en el mismo heap
// Útil para:
// - Microservicios con heap limitado
// - Contenedores con límites de memoria
// - Caches grandes en memoria

public class ScalingDemo {
    public static void main(String[] args) {
        // Con -Xmx1g:
        // Java 24: ~40M objetos pequeños
        // Java 25: ~55M objetos pequeños
        // = 37% más capacidad
    }
}
```

## Limitaciones

### 1. Es una Mejora de JVM

**No requiere cambios en código**, pero tampoco puedes controlarlo:

```java
// ❌ No hay API para esto
// No puedes habilitar/deshabilitar por clase
// No puedes customizar el header

public class MyClass {
    // Automáticamente usa compact headers
    // Sin forma de opt-out por clase
}
```

### 2. Minimal en Objetos Grandes

```java
// Para objetos con muchos campos, el ahorro es proporcionalmente menor

class LargeObject {
    // 100 campos de 8 bytes cada uno
    long field1, field2, /*...*/ field100;
}
// Total data: 800 bytes
// Header: 12 bytes (Java 24) vs 8 bytes (Java 25)
// Ahorro: 4 bytes de 812 bytes = 0.5% (insignificante)
```

### 3. Beneficio Depende del Workload

```java
// Máximo beneficio: muchos objetos pequeños
class Point { int x, y; }  // ✅ Gran beneficio

// Beneficio moderado: objetos medianos
class User { /* 10-20 campos */ }  // ✅ Beneficio moderado

// Beneficio mínimo: pocos objetos grandes
class HugeCache { byte[] data = new byte[1_000_000]; }  // ⚠️ Beneficio mínimo
```

## Verificación

### Cómo Verificar que Está Activo

```bash
# Ver flags de JVM
java -XX:+PrintFlagsFinal -version | grep UseCompactObjectHeaders

# Debería mostrar:
# bool UseCompactObjectHeaders = true
```

### Comparar Uso de Memoria

```java
public class HeaderSizeDemo {
    public static void main(String[] args) throws Exception {
        // Usar JOL (Java Object Layout) para inspeccionar
        // Requiere dependency: org.openjdk.jol:jol-core

        Object obj = new Object();

        // Java 24:
        // OFFSET  SIZE   TYPE DESCRIPTION
        // 0     12       (object header)

        // Java 25:
        // OFFSET  SIZE   TYPE DESCRIPTION
        // 0      8       (object header)

        System.out.println(org.openjdk.jol.info.ClassLayout.parseInstance(obj).toPrintable());
    }
}
```

### Benchmarking

```java
import java.util.*;

public class CompactHeaderBenchmark {
    static class SmallObject {
        int value;
    }

    public static void main(String[] args) {
        int count = 10_000_000;

        // Medir memoria antes
        System.gc();
        long memBefore = Runtime.getRuntime().totalMemory() -
                        Runtime.getRuntime().freeMemory();

        // Crear objetos
        List<SmallObject> objects = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            objects.add(new SmallObject());
        }

        // Medir memoria después
        System.gc();
        long memAfter = Runtime.getRuntime().totalMemory() -
                       Runtime.getRuntime().freeMemory();

        long usedMemory = (memAfter - memBefore) / 1024 / 1024;
        double bytesPerObject = (double)(memAfter - memBefore) / count;

        System.out.printf("Objects: %,d%n", count);
        System.out.printf("Memory used: %d MB%n", usedMemory);
        System.out.printf("Bytes per object: %.2f%n", bytesPerObject);

        // Java 24: ~24 bytes/object
        // Java 25: ~16 bytes/object
    }
}
```

## Mejores Prácticas

### 1. Confiar en la JVM

```java
// ✅ Correcto - dejar que la JVM optimice
public class Service {
    private List<Data> cache = new ArrayList<>();

    public void addData(Data data) {
        cache.add(data);
    }
}

// ❌ No intentar "ayudar" a la JVM
// No hay nada que hacer específicamente para compact headers
```

### 2. Diseñar Objetos Eficientes

```java
// ✅ Bueno - objeto pequeño se beneficia más
class Point {
    int x, y;
}

// ⚠️ Considerar - tal vez primitives son mejor
class HeavyPoint {
    Integer x, y;  // Cada Integer es un objeto con header
    // 2 objetos extra = 2 headers extra
}

// ✅ Mejor para performance
class EfficientPoint {
    int x, y;  // Primitivos, sin headers adicionales
}
```

### 3. Monitorear Memoria

```java
// Usar herramientas de profiling para ver el impacto
// - VisualVM
// - JProfiler
// - YourKit
// - JOL (Java Object Layout)

public class MemoryMonitor {
    public static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        System.out.printf("Used: %d MB / Total: %d MB%n",
            usedMemory / 1024 / 1024,
            totalMemory / 1024 / 1024);
    }
}
```

## Comparación: Java 24 vs Java 25

### Objeto Simple

```java
class Simple {
    int value;
}

// Java 24:
// Header: 12 bytes
// Field:   4 bytes
// Padding: 4 bytes (alignment)
// TOTAL:  20 bytes

// Java 25:
// Header: 8 bytes
// Field:  4 bytes
// Padding: 4 bytes
// TOTAL: 16 bytes
// AHORRO: 20%
```

### Array de Objetos

```java
Object[] array = new Object[1000];

// Java 24:
// Array header: 16 bytes
// 1000 × (object header 12 bytes) = 12,000 bytes
// TOTAL: ~12 KB

// Java 25:
// Array header: 16 bytes
// 1000 × (object header 8 bytes) = 8,000 bytes
// TOTAL: ~8 KB
// AHORRO: 33%
```

## Referencias

### JEP y Documentación Oficial
- [JEP 519: Compact Object Headers (Experimental)](https://openjdk.org/jeps/519)
- [Java 25 Release Notes](https://jdk.java.net/25/release-notes)

### Artículos Técnicos
- [Compact Object Headers in Java 25 - Inside Java](https://inside.java/2024/08/compact-object-headers/)
- [Memory Efficiency with Compact Headers - Oracle Blog](https://blogs.oracle.com/javamagazine/)

### Herramientas
- [JOL (Java Object Layout)](https://openjdk.org/projects/code-tools/jol/) - Para inspeccionar layouts de objetos
- [VisualVM](https://visualvm.github.io/) - Profiling de memoria

---

[⬅️ Volver al README](../README.md)
