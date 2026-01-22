# Module Import Declarations (JEP 476 - Final)

## ¿Qué es?

**Module Import Declarations** (JEP 476 - Final) permite importar todos los paquetes exportados por un módulo con una sola declaración, simplificando el código y haciendo más explícitas las dependencias entre módulos.

## Sintaxis

```java
import module <module-name>;
```

## Ejemplo Básico

### Antes (Java 24 y anteriores)

```java
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.function.Predicate;
// ... potencialmente muchos más
```

### Con Java 25

```java
import module java.base;

// Ahora tienes acceso a TODOS los paquetes exportados por java.base:
// - java.lang.*
// - java.util.*
// - java.io.*
// - java.math.*
// - java.net.*
// - java.time.*
// etc.
```

## Ventajas

### 1. Menos Código Boilerplate

```java
// Antes: 15+ líneas de imports
import java.util.*;
import java.io.*;
import java.net.*;
import java.time.*;
// ...

// Con Module Import: 1 línea
import module java.base;
```

### 2. Explícitas las Dependencias

```java
// Ahora es claro qué módulos usa tu código
import module java.base;
import module java.sql;
import module java.xml;

public class MyService {
    // ...
}
```

### 3. Refactoring Más Fácil

Cuando un módulo reorganiza sus paquetes internamente, tu código sigue funcionando si los paquetes siguen exportándose.

## Limitaciones

### Solo Paquetes Exportados

Module import **solo** importa paquetes que el módulo **exporta explícitamente**:

```java
// module-info.java del módulo
module com.example.mylib {
    exports com.example.mylib.api;      // ✅ Se importa
    exports com.example.mylib.util;     // ✅ Se importa

    // NO exports com.example.mylib.internal  ❌ NO se importa
}
```

### Requiere Módulos

Solo funciona con código modular. Si tu código no usa módulos, esta característica no aplica.

## Comparación con Wildcard Imports

### Wildcard Import (`import java.util.*`)

```java
import java.util.*;  // Importa todas las CLASES del paquete java.util
```

- Importa clases de **un solo paquete**
- No importa subpaquetes (ej: `java.util.stream` necesita import separado)
- Puede causar conflictos de nombres

### Module Import (`import module java.base`)

```java
import module java.base;  // Importa todos los PAQUETES exportados
```

- Importa **todos los paquetes** del módulo
- Incluye subpaquetes automáticamente
- Más explícito sobre dependencias

## Ejemplos de Uso

### Ejemplo 1: Módulo java.base

```java
import module java.base;

public class Example {
    public void demo() {
        // java.util
        List<String> list = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();

        // java.io
        File file = new File("test.txt");

        // java.time
        LocalDate date = LocalDate.now();

        // java.math
        BigDecimal value = new BigDecimal("123.45");
    }
}
```

### Ejemplo 2: Múltiples Módulos

```java
import module java.base;
import module java.sql;
import module java.xml;

public class DatabaseXmlService {
    public void processData() {
        // Acceso a clases de java.sql
        Connection conn = DriverManager.getConnection(url);

        // Acceso a clases de java.xml
        DocumentBuilder builder = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder();
    }
}
```

### Ejemplo 3: Módulo Personalizado

```java
// module-info.java
module com.mycompany.utils {
    exports com.mycompany.utils.collections;
    exports com.mycompany.utils.strings;
    exports com.mycompany.utils.dates;
}

// En otro módulo
import module com.mycompany.utils;

public class Client {
    public void use() {
        // Acceso a todos los paquetes exportados
        CustomList list = new CustomList();
        StringUtils.format("text");
        DateUtils.parse("2025-01-01");
    }
}
```

## Cuándo Usar

### ✅ Usar Module Import Cuando:

1. Usas muchos paquetes del mismo módulo
2. Quieres hacer explícitas las dependencias de módulos
3. Trabajas en un proyecto completamente modular
4. El módulo exporta muchos paquetes que necesitas

### ❌ No Usar Module Import Cuando:

1. Solo necesitas 1-2 clases de un módulo
2. Tu proyecto no usa módulos
3. Quieres imports más específicos para claridad

## Mejores Prácticas

### 1. Ser Específico Cuando Sea Posible

```java
// Si solo usas una clase, sé específico
import java.util.ArrayList;  // ✅ Claro

// Module import solo si usas muchas clases
import module java.base;     // ✅ OK si usas 5+ paquetes
```

### 2. Documentar Dependencias

```java
// Indica por qué importas el módulo completo
import module java.base;  // Usamos collections, streams, time, io

public class Service {
    // ...
}
```

### 3. Evitar Conflictos

```java
// Si hay conflictos, usa imports específicos
import module java.base;
import module com.example.utils;

// Si ambos tienen una clase List, especifica:
import java.util.List;  // ✅ Explícito cuál usar
```

## Referencias

- [JEP 476: Module Import Declarations](https://openjdk.org/jeps/476)
- [`ModuleImportDemo.java`](../src/main/java/com/monghit/java25/features/ModuleImportDemo.java)

---

[⬅️ Volver al README](../README.md)
