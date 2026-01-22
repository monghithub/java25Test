package com.monghit.java25.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para PrimitivePatternMatchingDemo
 */
class PrimitivePatternMatchingDemoTest {

    private PrimitivePatternMatchingDemo demo;

    @BeforeEach
    void setUp() {
        demo = new PrimitivePatternMatchingDemo();
    }

    // ==================== processPrimitive Tests ====================

    @Test
    void processPrimitive_withLargeInteger_shouldReturnLargeMessage() {
        String result = demo.processPrimitive(150);
        assertThat(result).isEqualTo("Integer grande: 150");
    }

    @Test
    void processPrimitive_withSmallInteger_shouldReturnSmallMessage() {
        String result = demo.processPrimitive(50);
        assertThat(result).isEqualTo("Integer pequeño: 50");
    }

    @Test
    void processPrimitive_withLong_shouldReturnLongMessage() {
        String result = demo.processPrimitive(999L);
        assertThat(result).isEqualTo("Long: 999");
    }

    @Test
    void processPrimitive_withDouble_shouldReturnDoubleMessage() {
        String result = demo.processPrimitive(3.14);
        assertThat(result).isEqualTo("Double: 3.14");
    }

    @Test
    void processPrimitive_withFloat_shouldReturnFloatMessage() {
        String result = demo.processPrimitive(2.5f);
        assertThat(result).startsWith("Float: 2.5");
    }

    @Test
    void processPrimitive_withBoolean_shouldReturnBooleanMessage() {
        String result = demo.processPrimitive(true);
        assertThat(result).isEqualTo("Boolean: true");
    }

    @Test
    void processPrimitive_withByte_shouldReturnByteMessage() {
        byte b = 127;
        String result = demo.processPrimitive(b);
        assertThat(result).isEqualTo("Byte: 127");
    }

    @Test
    void processPrimitive_withShort_shouldReturnShortMessage() {
        short s = 1000;
        String result = demo.processPrimitive(s);
        assertThat(result).isEqualTo("Short: 1000");
    }

    @Test
    void processPrimitive_withCharacter_shouldReturnCharMessage() {
        String result = demo.processPrimitive('A');
        assertThat(result).isEqualTo("Char: A");
    }

    @Test
    void processPrimitive_withString_shouldReturnStringMessage() {
        String result = demo.processPrimitive("test");
        assertThat(result).isEqualTo("String: test");
    }

    @Test
    void processPrimitive_withNull_shouldReturnNullMessage() {
        String result = demo.processPrimitive(null);
        assertThat(result).isEqualTo("Null value");
    }

    // ==================== checkPrimitiveType Tests ====================

    @Test
    void checkPrimitiveType_withInteger_shouldRecognizeInt() {
        String result = demo.checkPrimitiveType(42);
        assertThat(result).isEqualTo("Es un int con valor: 42");
    }

    @Test
    void checkPrimitiveType_withDouble_shouldRecognizeDouble() {
        String result = demo.checkPrimitiveType(3.14);
        assertThat(result).isEqualTo("Es un double con valor: 3.14");
    }

    @Test
    void checkPrimitiveType_withBoolean_shouldRecognizeBoolean() {
        String result = demo.checkPrimitiveType(false);
        assertThat(result).isEqualTo("Es un boolean con valor: false");
    }

    @Test
    void checkPrimitiveType_withString_shouldNotRecognize() {
        String result = demo.checkPrimitiveType("test");
        assertThat(result).isEqualTo("No es un primitivo reconocido");
    }

    @Test
    void checkPrimitiveType_withNull_shouldNotRecognize() {
        String result = demo.checkPrimitiveType(null);
        assertThat(result).isEqualTo("No es un primitivo reconocido");
    }

    // ==================== safeConvertToInt Tests ====================

    @ParameterizedTest
    @MethodSource("provideConversionCases")
    void safeConvertToInt_shouldConvertCorrectly(Object input, int expected) {
        int result = demo.safeConvertToInt(input);
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> provideConversionCases() {
        return Stream.of(
                Arguments.of(42, 42),
                Arguments.of(100L, 100),
                Arguments.of(3.14, 3),
                Arguments.of("123", 123),
                Arguments.of(null, 0)
        );
    }

    @Test
    void safeConvertToInt_withInvalidString_shouldThrowException() {
        org.junit.jupiter.api.Assertions.assertThrows(
                NumberFormatException.class,
                () -> demo.safeConvertToInt("abc")
        );
    }

    // ==================== validateNumber Tests ====================

    @Test
    void validateNumber_withNegative_shouldReturnNegativeMessage() {
        String result = demo.validateNumber(-5);
        assertThat(result).isEqualTo("Número negativo");
    }

    @Test
    void validateNumber_withZero_shouldReturnZeroMessage() {
        String result = demo.validateNumber(0);
        assertThat(result).isEqualTo("Cero");
    }

    @Test
    void validateNumber_withSmallPositive_shouldReturnSmallPositiveMessage() {
        String result = demo.validateNumber(50);
        assertThat(result).isEqualTo("Número positivo pequeño");
    }

    @Test
    void validateNumber_withLargePositive_shouldReturnLargePositiveMessage() {
        String result = demo.validateNumber(150);
        assertThat(result).isEqualTo("Número positivo grande");
    }

    @Test
    void validateNumber_withNull_shouldReturnNullMessage() {
        String result = demo.validateNumber(null);
        assertThat(result).isEqualTo("Valor nulo");
    }

    @Test
    void validateNumber_withNonInteger_shouldReturnNotIntegerMessage() {
        String result = demo.validateNumber("test");
        assertThat(result).isEqualTo("No es un entero");
    }

    @Test
    void validateNumber_atBoundary100_shouldReturnSmallPositive() {
        String result = demo.validateNumber(100);
        assertThat(result).isEqualTo("Número positivo pequeño");
    }

    @Test
    void validateNumber_atBoundary101_shouldReturnLargePositive() {
        String result = demo.validateNumber(101);
        assertThat(result).isEqualTo("Número positivo grande");
    }
}
