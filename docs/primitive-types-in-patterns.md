# Primitive Types in Patterns (JEP 455 - Preview)

## Índice
- [¿Qué es?](#qué-es)
- [Motivación](#motivación)
- [Conceptos Clave](#conceptos-clave)
- [Sintaxis y Uso](#sintaxis-y-uso)
- [Ejemplos Detallados](#ejemplos-detallados)
- [Casos de Uso Prácticos](#casos-de-uso-prácticos)
- [Ventajas y Beneficios](#ventajas-y-beneficios)
- [Limitaciones](#limitaciones)
- [Comparación con Java 21](#comparación-con-java-21)
- [Referencias](#referencias)

## ¿Qué es?

Primitive Types in Patterns es una característica preview de Java 25 (JEP 455) que extiende el pattern matching para permitir el uso de **tipos primitivos** (`int`, `long`, `double`, `float`, `boolean`, `byte`, `short`, `char`) en todos los contextos de patrones, incluyendo `instanceof` y `switch`.

Anteriormente, el pattern matching en Java solo funcionaba con tipos de referencia (objetos). Ahora, Java 25 permite trabajar directamente con primitivos, eliminando la necesidad de boxing/unboxing manual.

## Motivación

### Problema antes de Java 25

Antes, para trabajar con valores primitivos en switch o instanceof, necesitabas:

```java
// Switch tradicional limitado
Object value = 42;
if (value instanceof Integer) {
    int i = (Integer) value;  // Unboxing manual
    if (i > 100) {
        System.out.println("Grande");
    }
}

// O con castings repetitivos
switch (value) {
    case Integer i -> {
        if (i > 100) System.out.println("Grande");
        else System.out.println("Pequeño");
    }
    default -> System.out.println("No es Integer");
}
```

### Solución con Java 25

```java
// Pattern matching directo con primitivos
Object value = 42;
return switch (value) {
    case int i when i > 100 -> "Grande";
    case int i -> "Pequeño";
    case double d -> "Double: " + d;
    default -> "Otro tipo";
};
```

## Conceptos Clave

### 1. Pattern Matching con Primitivos

Permite usar tipos primitivos directamente en patterns:

```java
Object obj = 42;

// instanceof con primitivos
if (obj instanceof int i) {
    System.out.println("Es un int: " + i);
}

// switch con primitivos
switch (obj) {
    case int i -> System.out.println("Integer: " + i);
    case double d -> System.out.println("Double: " + d);
    case boolean b -> System.out.println("Boolean: " + b);
    default -> System.out.println("Otro");
}
```

### 2. Guards (Condiciones adicionales)

Puedes añadir condiciones a los patterns:

```java
switch (value) {
    case int i when i < 0 -> "Negativo";
    case int i when i == 0 -> "Cero";
    case int i when i > 0 && i <= 100 -> "Positivo pequeño";
    case int i -> "Positivo grande";
    default -> "No es int";
}
```

### 3. Conversión Automática

Java maneja automáticamente las conversiones entre tipos primitivos boxed y unboxed:

```java
Integer boxed = 42;
switch (boxed) {
    case int i -> System.out.println("Unboxed: " + i);
}

Object obj = 42;  // Autoboxing
switch (obj) {
    case int i -> System.out.println("Matched: " + i);  // Funciona!
}
```

## Sintaxis y Uso

### Sintaxis Básica

```java
// instanceof con primitivos
if (object instanceof primitiveType variable) {
    // usar variable
}

// switch con primitivos
switch (expression) {
    case primitiveType variable -> expression;
    case primitiveType variable when guard -> expression;
}
```

### Tipos Primitivos Soportados

Todos los tipos primitivos de Java están soportados:

```java
switch (value) {
    case int i -> "int";
    case long l -> "long";
    case double d -> "double";
    case float f -> "float";
    case boolean b -> "boolean";
    case byte bt -> "byte";
    case short s -> "short";
    case char c -> "char";
    default -> "otro";
}
```

## Ejemplos Detallados

### Ejemplo 1: Validación de Rangos

```java
public String validateNumber(Object value) {
    return switch (value) {
        case int i when i < 0 ->
            "Número negativo: " + i;
        case int i when i == 0 ->
            "Cero";
        case int i when i > 0 && i <= 100 ->
            "Número positivo pequeño: " + i;
        case int i when i > 100 && i <= 1000 ->
            "Número positivo mediano: " + i;
        case int i ->
            "Número positivo grande: " + i;
        case null ->
            "Valor nulo";
        default ->
            "No es un número entero";
    };
}

// Uso
System.out.println(validateNumber(50));    // "Número positivo pequeño: 50"
System.out.println(validateNumber(-10));   // "Número negativo: -10"
System.out.println(validateNumber(5000));  // "Número positivo grande: 5000"
```

### Ejemplo 2: Conversión Segura entre Tipos

```java
public int safeConvertToInt(Object value) {
    return switch (value) {
        case int i -> i;
        case long l when l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE ->
            (int) l;
        case double d when d == Math.floor(d) ->
            (int) d;  // Solo si no tiene decimales
        case String s -> {
            try {
                yield Integer.parseInt(s);
            } catch (NumberFormatException e) {
                yield 0;
            }
        }
        case null -> 0;
        default -> 0;
    };
}

// Uso
System.out.println(safeConvertToInt(42));      // 42
System.out.println(safeConvertToInt(42L));     // 42
System.out.println(safeConvertToInt(42.0));    // 42
System.out.println(safeConvertToInt("42"));    // 42
System.out.println(safeConvertToInt(42.5));    // 0 (tiene decimales)
```

### Ejemplo 3: Procesamiento de Diferentes Tipos

```java
public String processValue(Object value) {
    return switch (value) {
        // Números enteros
        case int i when i % 2 == 0 ->
            "Entero par: " + i;
        case int i ->
            "Entero impar: " + i;

        // Números decimales
        case double d when d > 0 ->
            "Double positivo: " + String.format("%.2f", d);
        case double d ->
            "Double negativo o cero: " + String.format("%.2f", d);

        // Booleanos
        case boolean b ->
            "Booleano: " + (b ? "verdadero" : "falso");

        // Caracteres
        case char c when Character.isDigit(c) ->
            "Dígito: " + c;
        case char c when Character.isLetter(c) ->
            "Letra: " + c;
        case char c ->
            "Carácter especial: " + c;

        // Otros
        case String str ->
            "String: " + str;
        case null ->
            "Valor nulo";
        default ->
            "Tipo desconocido: " + value.getClass().getSimpleName();
    };
}
```

## Casos de Uso Prácticos

### 1. APIs que Retornan Object

```java
// API legacy que retorna Object
Object result = legacyApi.getValue();

// Procesamiento type-safe
String message = switch (result) {
    case int statusCode when statusCode >= 200 && statusCode < 300 ->
        "Éxito: " + statusCode;
    case int statusCode when statusCode >= 400 ->
        "Error: " + statusCode;
    case String errorMsg ->
        "Error con mensaje: " + errorMsg;
    case boolean success ->
        success ? "Operación exitosa" : "Operación fallida";
    default ->
        "Respuesta inesperada";
};
```

### 2. Deserialización JSON/Dynamic Data

```java
public Object parseJsonValue(JsonNode node) {
    return switch (node.getNodeType()) {
        case NUMBER -> {
            if (node.isInt()) yield node.asInt();
            else yield node.asDouble();
        }
        case BOOLEAN -> node.asBoolean();
        case STRING -> node.asText();
        default -> null;
    };
}

// Procesamiento con pattern matching
Object value = parseJsonValue(jsonNode);
String result = switch (value) {
    case int i -> "Número entero: " + i;
    case double d -> "Número decimal: " + d;
    case boolean b -> "Booleano: " + b;
    case String s -> "Texto: " + s;
    case null -> "Valor nulo";
    default -> "Tipo desconocido";
};
```

### 3. Configuración Dinámica

```java
public void applyConfiguration(String key, Object value) {
    switch (value) {
        case int port when port > 0 && port <= 65535 ->
            server.setPort(port);
        case boolean enabled ->
            feature.setEnabled(enabled);
        case double timeout when timeout > 0 ->
            connection.setTimeout(Duration.ofMillis((long)(timeout * 1000)));
        case String path ->
            config.setPath(Paths.get(path));
        default ->
            throw new IllegalArgumentException("Valor no soportado para " + key);
    }
}
```

## Ventajas y Beneficios

### 1. Código Más Conciso
- Elimina boxing/unboxing manual
- Reduce castings explícitos
- Menos código boilerplate

### 2. Type Safety Mejorado
- Errores de tipo detectados en compile-time
- El compilador garantiza exhaustividad
- Menos errores en runtime

### 3. Mejor Rendimiento
- Evita boxing innecesario
- Menos objetos temporales
- Mejor uso de memoria

### 4. Legibilidad
- Intención más clara
- Patrones más expresivos
- Código autodocumentado

## Limitaciones

### 1. Feature Preview
- Requiere `--enable-preview`
- Puede cambiar en futuras versiones
- No recomendado para producción hasta finalización

### 2. Solo en Contextos Específicos
- Funciona en `instanceof` y `switch`
- No disponible en otros contextos

### 3. Orden de Patterns Importa
```java
// Incorrecto - el segundo case nunca se alcanza
switch (value) {
    case int i -> "Cualquier int";
    case int i when i > 100 -> "Nunca se ejecuta!";  // Error de compilación
}

// Correcto - más específico primero
switch (value) {
    case int i when i > 100 -> "Int grande";
    case int i -> "Int normal";
}
```

## Comparación con Java 21

### Java 21 (Sin Primitive Patterns)

```java
Object value = getData();

// Verboso y propenso a errores
if (value instanceof Integer) {
    Integer boxed = (Integer) value;
    int i = boxed.intValue();  // Unboxing manual
    if (i > 100) {
        return "Grande";
    } else {
        return "Pequeño";
    }
} else if (value instanceof Double) {
    Double boxed = (Double) value;
    double d = boxed.doubleValue();
    return "Double: " + d;
}
return "Otro";
```

### Java 25 (Con Primitive Patterns)

```java
Object value = getData();

// Conciso y claro
return switch (value) {
    case int i when i > 100 -> "Grande";
    case int i -> "Pequeño";
    case double d -> "Double: " + d;
    default -> "Otro";
};
```

## Referencias

### JEP y Documentación Oficial
- [JEP 455: Primitive Types in Patterns](https://openjdk.org/jeps/455)
- [JEP 488: Primitive Types in Patterns (Second Preview)](https://openjdk.org/jeps/488)
- [JEP 507: Primitive Types in Patterns (Third Preview)](https://openjdk.org/jeps/507)

### Artículos y Tutoriales
- [Primitive Types in Patterns - SoftwareMill](https://softwaremill.com/primitive-types-in-patterns-instanceof-and-switch-in-java-23/)
- [Pattern Matching for Primitives - InfoQ](https://www.infoq.com/news/2024/02/java-enhances-pattern-matching/)

### Ejemplos en el Proyecto
- [`PrimitivePatternMatchingDemo.java`](../src/main/java/com/monghit/java25/features/PrimitivePatternMatchingDemo.java)

---

[⬅️ Volver al README](../README.md)
