package br.com.bianeck.javaefetivo.capitulo02.item01.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ValidationResult.
 *
 * Verifica se o padrão Flyweight está funcionando corretamente
 * e se as instâncias estão sendo reutilizadas.
 *
 * @author Thiago Bianeck
 */
class ValidationResultTest {

    @Test
    @DisplayName("Deve reutilizar instância de sucesso")
    void shouldReuseSameSuccessInstance() {
        // When
        ValidationResult success1 = ValidationResult.success();
        ValidationResult success2 = ValidationResult.success();

        // Then
        assertSame(success1, success2, "Deve retornar a mesma instância");
        assertTrue(success1.isValid());
        assertEquals("Validation successful", success1.getMessage());
    }

    @Test
    @DisplayName("Deve reutilizar instância de valor inválido")
    void shouldReuseSameInvalidAmountInstance() {
        // When
        ValidationResult invalid1 = ValidationResult.invalidAmount();
        ValidationResult invalid2 = ValidationResult.invalidAmount();

        // Then
        assertSame(invalid1, invalid2, "Deve retornar a mesma instância");
        assertFalse(invalid1.isValid());
        assertEquals("Invalid amount", invalid1.getMessage());
    }

    @Test
    @DisplayName("Deve reutilizar instância de fundos insuficientes")
    void shouldReuseSameInsufficientFundsInstance() {
        // When
        ValidationResult insufficient1 = ValidationResult.insufficientFunds();
        ValidationResult insufficient2 = ValidationResult.insufficientFunds();

        // Then
        assertSame(insufficient1, insufficient2, "Deve retornar a mesma instância");
        assertFalse(insufficient1.isValid());
        assertEquals("Insufficient funds", insufficient1.getMessage());
    }

    @Test
    @DisplayName("Deve reutilizar instância de cartão expirado")
    void shouldReuseSameExpiredCardInstance() {
        // When
        ValidationResult expired1 = ValidationResult.expiredCard();
        ValidationResult expired2 = ValidationResult.expiredCard();

        // Then
        assertSame(expired1, expired2, "Deve retornar a mesma instância");
        assertFalse(expired1.isValid());
        assertEquals("Card expired", expired1.getMessage());
    }

    @Test
    @DisplayName("Deve reutilizar instância de cartão inválido")
    void shouldReuseSameInvalidCardInstance() {
        // When
        ValidationResult invalid1 = ValidationResult.invalidCard();
        ValidationResult invalid2 = ValidationResult.invalidCard();

        // Then
        assertSame(invalid1, invalid2, "Deve retornar a mesma instância");
        assertFalse(invalid1.isValid());
        assertEquals("Invalid card number", invalid1.getMessage());
    }

    @Test
    @DisplayName("Deve criar novas instâncias para resultados customizados")
    void shouldCreateNewInstancesForCustomResults() {
        // When
        ValidationResult custom1 = ValidationResult.custom(false, "Custom error 1");
        ValidationResult custom2 = ValidationResult.custom(false, "Custom error 2");
        ValidationResult custom3 = ValidationResult.custom(false, "Custom error 1");

        // Then
        assertNotSame(custom1, custom2, "Instâncias customizadas devem ser diferentes");
        assertNotSame(custom1, custom3, "Mesmo com mesma mensagem, devem ser diferentes");

        assertFalse(custom1.isValid());
        assertEquals("Custom error 1", custom1.getMessage());
        assertEquals("Custom error 2", custom2.getMessage());
    }

    @Test
    @DisplayName("Deve criar resultado baseado em condição")
    void shouldCreateResultFromCondition() {
        // When
        ValidationResult successFromCondition = ValidationResult.fromCondition(true, "Should not appear");
        ValidationResult failureFromCondition = ValidationResult.fromCondition(false, "Condition failed");

        // Then
        assertSame(ValidationResult.success(), successFromCondition,
                "Condição verdadeira deve retornar instância de sucesso");

        assertFalse(failureFromCondition.isValid());
        assertEquals("Condition failed", failureFromCondition.getMessage());
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode corretamente")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        ValidationResult success1 = ValidationResult.success();
        ValidationResult success2 = ValidationResult.success();
        ValidationResult custom1 = ValidationResult.custom(true, "Validation successful");
        ValidationResult invalid = ValidationResult.invalidAmount();

        // Then
        assertEquals(success1, success2);
        assertEquals(success1.hashCode(), success2.hashCode());

        assertEquals(success1, custom1); // Mesmo conteúdo
        assertEquals(success1.hashCode(), custom1.hashCode());

        assertNotEquals(success1, invalid);
        assertNotEquals(success1.hashCode(), invalid.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString informativo")
    void shouldHaveInformativeToString() {
        // Given
        ValidationResult success = ValidationResult.success();
        ValidationResult invalid = ValidationResult.invalidAmount();
        ValidationResult custom = ValidationResult.custom(false, "Custom message");

        // Then
        assertTrue(success.toString().contains("valid=true"));
        assertTrue(success.toString().contains("Validation successful"));

        assertTrue(invalid.toString().contains("valid=false"));
        assertTrue(invalid.toString().contains("Invalid amount"));

        assertTrue(custom.toString().contains("valid=false"));
        assertTrue(custom.toString().contains("Custom message"));
    }
}