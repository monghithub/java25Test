# Instance Main Methods (JEP 495 - Final)

## ¿Qué es?

**Instance Main Methods** (JEP 495 - Final) permite escribir métodos `main` como **métodos de instancia** en lugar de métodos estáticos, simplificando el código para principiantes y scripts simples.

## Evolución del Método Main

### Java Tradicional (Hasta Java 22)

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
```

### Java 21-24: Main Simplificado

```java
// Sin modificadores de acceso ni String[] args
void main() {
    System.out.println("Hello World!");
}
```

### Java 25: Instance Main

```java
public class HelloWorld {
    // Puede ser método de instancia
    void main() {
        // Tiene acceso a campos de instancia
        System.out.println(message);
    }

    private String message = "Hello from instance!";
}
```

## Ventajas

### 1. Acceso a Campos de Instancia

```java
public class Counter {
    private int count = 0;

    void main() {
        // Acceso directo a campos de instancia
        count++;
        System.out.println("Count: " + count);

        // Llamar métodos de instancia
        incrementTwice();
        System.out.println("Count: " + count);
    }

    private void incrementTwice() {
        count += 2;
    }
}
```

### 2. Código Más Simple

```java
// Antes: Necesitabas crear instancia manualmente
public class OldWay {
    public static void main(String[] args) {
        OldWay instance = new OldWay();
        instance.run();
    }

    public void run() {
        System.out.println(this.message);
    }

    private String message = "Hello";
}

// Ahora: Automático
public class NewWay {
    void main() {
        System.out.println(this.message);
    }

    private String message = "Hello";
}
```

### 3. Mejor para Aprendizaje

```java
// Principiante no necesita entender static
void main() {
    System.out.println("Hello!");
}

// vs

public static void main(String[] args) {  // ¿Qué es public? ¿static? ¿String[]?
    System.out.println("Hello!");
}
```

## Variantes del Main Method

Java 25 soporta múltiples formas:

### 1. Main Tradicional (Sigue funcionando)

```java
public static void main(String[] args) {
    // ...
}
```

### 2. Main Simplificado

```java
void main() {
    // Sin modificadores, sin args
}
```

### 3. Main de Instancia

```java
// Sin static
void main() {
    // Puede acceder a this
}
```

### 4. Main de Instancia con Args

```java
void main(String[] args) {
    System.out.println("Args: " + String.join(", ", args));
}
```

## Orden de Prioridad

Si hay múltiples formas de main, Java busca en este orden:

1. `static void main(String[] args)`
2. `static void main()`
3. `void main(String[] args)` (instancia)
4. `void main()` (instancia)

```java
public class MultipleMains {
    // Este se ejecuta (más alta prioridad)
    public static void main(String[] args) {
        System.out.println("Static main with args");
    }

    // Este se ignora
    void main() {
        System.out.println("Instance main");
    }
}
```

## Ejemplos Prácticos

### Ejemplo 1: Script Simple

```java
// Archivo: Greet.java
void main() {
    System.out.println("¡Hola Mundo!");
}

// Ejecutar: java Greet.java
```

### Ejemplo 2: Con Estado

```java
public class InstanceMainDemo {
    private String message = "¡Hola desde Instance Main!";
    private int counter = 0;

    void main() {
        System.out.println("=== Instance Main Demo ===");
        System.out.println(message);

        incrementCounter();
        System.out.println("Counter: " + counter);

        demonstrateInstanceAccess();
    }

    private void incrementCounter() {
        counter++;
    }

    private void demonstrateInstanceAccess() {
        System.out.println("Acceso a métodos de instancia sin problema");
    }
}
```

### Ejemplo 3: Con Argumentos

```java
public class ArgsDemo {
    void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No arguments provided");
        } else {
            System.out.println("Arguments:");
            for (int i = 0; i < args.length; i++) {
                System.out.println("  [" + i + "] = " + args[i]);
            }
        }
    }
}

// Ejecutar: java ArgsDemo.java hello world
// Output:
// Arguments:
//   [0] = hello
//   [1] = world
```

### Ejemplo 4: Configuración de Instancia

```java
public class ConfigurableApp {
    private final String environment;
    private final int port;

    // Constructor se llama automáticamente
    public ConfigurableApp() {
        this.environment = System.getenv("APP_ENV");
        this.port = Integer.parseInt(
            System.getenv("APP_PORT")
        );
    }

    void main() {
        System.out.println("Starting application...");
        System.out.println("Environment: " + environment);
        System.out.println("Port: " + port);

        startServer();
    }

    private void startServer() {
        System.out.println("Server started on port " + port);
    }
}
```

## Cuándo Usar

### ✅ Usar Instance Main Cuando:

1. Scripts simples y prototipos
2. Programas educativos
3. Necesitas acceso a campos de instancia
4. Código de ejemplo y demos
5. Testing rápido de ideas

### ❌ Usar Static Main Cuando:

1. Aplicaciones de producción grandes
2. Frameworks que esperan static main
3. Compatibilidad con código legacy
4. Puntos de entrada de librerías

## Consideraciones

### Constructor por Defecto

Java crea automáticamente una instancia usando el constructor por defecto:

```java
public class NeedsNoArgConstructor {
    private final String value;

    // ❌ Error: No hay constructor sin argumentos
    public NeedsNoArgConstructor(String value) {
        this.value = value;
    }

    void main() {
        System.out.println(value);
    }
}

// ✅ Solución: Agregar constructor sin argumentos
public class Fixed {
    private final String value;

    public Fixed() {
        this.value = "default";
    }

    void main() {
        System.out.println(value);
    }
}
```

### Performance

No hay diferencia significativa de performance entre static y instance main para programas normales.

## Mejores Prácticas

### 1. Mantenerlo Simple

```java
// ✅ Bueno - simple y claro
void main() {
    processData();
}

// ❌ Evitar - demasiado complejo para main
void main() {
    // 100 líneas de código aquí
}
```

### 2. Usar para Prototipos

```java
// ✅ Perfecto para testing rápido
void main() {
    var list = List.of(1, 2, 3, 4, 5);
    var result = list.stream()
        .filter(n -> n % 2 == 0)
        .map(n -> n * 2)
        .toList();
    System.out.println(result);
}
```

### 3. Educación

```java
// ✅ Excelente para enseñar
// Estudiante no necesita entender static aún
void main() {
    System.out.println("Mi primer programa");
}
```

## Referencias

- [JEP 495: Simple Source Files and Instance Main Methods](https://openjdk.org/jeps/495)
- [`InstanceMainDemo.java`](../src/main/java/com/monghit/java25/features/InstanceMainDemo.java)
- [`SimplifiedMainDemo.java`](../src/main/java/com/monghit/java25/features/SimplifiedMainDemo.java)

---

[⬅️ Volver al README](../README.md)
