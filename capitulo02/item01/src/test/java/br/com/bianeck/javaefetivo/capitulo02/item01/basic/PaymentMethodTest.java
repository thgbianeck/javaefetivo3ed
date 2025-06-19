package br.com.bianeck.javaefetivo.capitulo02.item01.basic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

/**
 * Testes unitários para a classe PaymentMethod.
 *
 * Verifica se os métodos static factory funcionam corretamente e
 * demonstram as vantagens apresentadas no Item 1.
 *
 * @author Thiago Bianeck
 */
class PaymentMethodTest {

    @Test
    @DisplayName("Deve criar cartão de crédito com dados válidos")
    void shouldCreateCreditCardWithValidData() {
        // Given
        String cardNumber = "4111-1111-1111-1111";
        BigDecimal limit = new BigDecimal("5000.00");

        // When
        PaymentMethod creditCard = PaymentMethod.creditCard(cardNumber, limit);

        // Then
        assertNotNull(creditCard);
        assertEquals("CREDIT_CARD", creditCard.getType());
        assertEquals(cardNumber, creditCard.getIdentifier());
        assertEquals(limit, creditCard.getLimit());
        assertTrue(creditCard.requiresAuthentication());
    }

    @Test
    @DisplayName("Deve criar PIX com chave válida")
    void shouldCreatePixWithValidKey() {
        // Given
        String pixKey = "usuario@email.com";

        // When
        PaymentMethod pix = PaymentMethod.pix(pixKey);

        // Then
        assertNotNull(pix);
        assertEquals("PIX", pix.getType());
        assertEquals(pixKey, pix.getIdentifier());
        assertEquals(new BigDecimal("50000.00"), pix.getLimit());
        assertFalse(pix.requiresAuthentication());
    }

    @Test
    @DisplayName("Deve criar boleto com código de barras válido")
    void shouldCreateBankSlipWithValidBarcode() {
        // Given
        String barcode = "12345678901234567890123456789012345678901234567"; // 47 dígitos

        // When
        PaymentMethod boleto = PaymentMethod.bankSlip(barcode);

        // Then
        assertNotNull(boleto);
        assertEquals("BANK_SLIP", boleto.getType());
        assertEquals(barcode, boleto.getIdentifier());
        assertEquals(new BigDecimal("10000.00"), boleto.getLimit());
        assertFalse(boleto.requiresAuthentication());
    }

    @Test
    @DisplayName("Deve criar cartão de débito diferente do crédito")
    void shouldCreateDebitCardDifferentFromCredit() {
        // Given
        String cardNumber = "4111-1111-1111-1111";
        BigDecimal limit = new BigDecimal("2000.00");

        // When
        PaymentMethod creditCard = PaymentMethod.creditCard(cardNumber, limit);
        PaymentMethod debitCard = PaymentMethod.debitCard(cardNumber, limit);

        // Then
        assertNotEquals(creditCard.getType(), debitCard.getType());
        assertEquals("CREDIT_CARD", creditCard.getType());
        assertEquals("DEBIT_CARD", debitCard.getType());

        // Ambos requerem autenticação
        assertTrue(creditCard.requiresAuthentication());
        assertTrue(debitCard.requiresAuthentication());
    }

    @Test
    @DisplayName("Deve falhar ao criar cartão com número inválido")
    void shouldFailToCreateCardWithInvalidNumber() {
        // Given
        String invalidCardNumber = "123"; // Muito curto
        BigDecimal limit = new BigDecimal("1000.00");

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                PaymentMethod.creditCard(invalidCardNumber, limit));
    }

    @Test
    @DisplayName("Deve falhar ao criar PIX com chave inválida")
    void shouldFailToCreatePixWithInvalidKey() {
        // Given
        String invalidPixKey = "chave-invalida";

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                PaymentMethod.pix(invalidPixKey));
    }

    @Test
    @DisplayName("Deve falhar ao criar boleto com código inválido")
    void shouldFailToCreateBankSlipWithInvalidBarcode() {
        // Given
        String invalidBarcode = "123456789"; // Muito curto

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                PaymentMethod.bankSlip(invalidBarcode));
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode corretamente")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        String cardNumber = "4111-1111-1111-1111";
        BigDecimal limit = new BigDecimal("5000.00");

        // When
        PaymentMethod card1 = PaymentMethod.creditCard(cardNumber, limit);
        PaymentMethod card2 = PaymentMethod.creditCard(cardNumber, limit);
        PaymentMethod card3 = PaymentMethod.debitCard(cardNumber, limit);

        // Then
        assertEquals(card1, card2);
        assertEquals(card1.hashCode(), card2.hashCode());
        assertNotEquals(card1, card3); // Tipos diferentes
    }

    @Test
    @DisplayName("Deve mascarar identificador no toString")
    void shouldMaskIdentifierInToString() {
        // Given
        String cardNumber = "4111-1111-1111-1111";
        BigDecimal limit = new BigDecimal("5000.00");

        // When
        PaymentMethod card = PaymentMethod.creditCard(cardNumber, limit);
        String toString = card.toString();

        // Then
        assertTrue(toString.contains("4111"));
        assertTrue(toString.contains("1111"));
        assertTrue(toString.contains("****"));
        assertFalse(toString.contains("4111-1111-1111-1111")); // Número completo não deve aparecer
    }
}