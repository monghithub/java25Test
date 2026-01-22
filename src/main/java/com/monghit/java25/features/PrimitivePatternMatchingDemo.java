package com.monghit.java25.features;

import org.springframework.stereotype.Service;

/**
 * Demo de Primitive Types in Patterns (JEP 455 - Preview)
 *
 * Java 25 permite usar tipos primitivos en pattern matching,
 * extendiendo instanceof y switch para trabajar con todos los tipos primitivos.
 */
@Service
public class PrimitivePatternMatchingDemo {

    /**
     * Ejemplo de pattern matching con primitivos en switch
     * En Java 25, se pueden usar tipos primitivos directamente en patterns
     */
    public String processPrimitive(Object value) {
        return switch (value) {
            case Integer i when i > 100 -> "Integer grande: " + i;
            case Integer i -> "Integer pequeño: " + i;
            case Long l -> "Long: " + l;
            case Double d -> "Double: " + d;
            case Float f -> "Float: " + f;
            case Boolean b -> "Boolean: " + b;
            case Byte bt -> "Byte: " + bt;
            case Short s -> "Short: " + s;
            case Character c -> "Char: " + c;
            case String str -> "String: " + str;
            case null -> "Null value";
            default -> "Tipo desconocido: " + value.getClass().getSimpleName();
        };
    }

    /**
     * Ejemplo de instanceof con primitivos
     * Nota: En Java 25, instanceof puede trabajar con tipos boxed
     */
    public String checkPrimitiveType(Object value) {
        if (value instanceof Integer i) {
            return "Es un int con valor: " + i;
        } else if (value instanceof Double d) {
            return "Es un double con valor: " + d;
        } else if (value instanceof Boolean b) {
            return "Es un boolean con valor: " + b;
        }
        return "No es un primitivo reconocido";
    }

    /**
     * Ejemplo de conversión segura con pattern matching
     */
    public int safeConvertToInt(Object value) {
        return switch (value) {
            case Integer i -> i;
            case Long l -> l.intValue();
            case Double d -> d.intValue();
            case String s -> Integer.parseInt(s);
            case null -> 0;
            default -> 0;
        };
    }

    /**
     * Ejemplo de validación con guards
     */
    public String validateNumber(Object value) {
        return switch (value) {
            case Integer i when i < 0 -> "Número negativo";
            case Integer i when i == 0 -> "Cero";
            case Integer i when i > 0 && i <= 100 -> "Número positivo pequeño";
            case Integer i -> "Número positivo grande";
            case null -> "Valor nulo";
            default -> "No es un entero";
        };
    }
}
