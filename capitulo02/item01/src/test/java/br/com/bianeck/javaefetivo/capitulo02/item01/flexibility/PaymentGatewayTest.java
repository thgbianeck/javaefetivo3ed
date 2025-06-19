package br.com.bianeck.javaefetivo.capitulo02.item01.flexibility;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

/**
 * Testes unitários para PaymentGateway e suas implementações.
 *
 * Verifica se os métodos static factory retornam as implementações
 * corretas baseadas nos parâmetros de entrada.
 *
 * @author Thiago Bianeck
 */
class PaymentGatewayTest {

    @Test
    @DisplayName("Deve retornar gateway de cartão de crédito para tipo CREDIT_CARD")
    void shouldReturnCreditCardGatewayForCreditCardType() {
        // When
        PaymentGateway gateway = PaymentGateway.forPaymentType("CREDIT_CARD");

        // Then
        assertNotNull(gateway);
        assertTrue(gateway.getGatewayName().contains("CreditCard"));
        assertTrue(gateway.isAvailable());
        assertTrue(gateway.getFeeRate().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Deve retornar gateway PIX para tipo PIX")
    void shouldReturnPixGatewayForPixType() {
        // When
        PaymentGateway gateway = PaymentGateway.forPaymentType("PIX");

        // Then
        assertNotNull(gateway);
        assertTrue(gateway.getGatewayName().contains("PIX"));
        assertTrue(gateway.isAvailable());
    }

    @Test
    @DisplayName("Deve retornar gateway de boleto para tipo BANK_SLIP")
    void shouldReturnBoletoGatewayForBankSlipType() {
        // When
        PaymentGateway gateway = PaymentGateway.forPaymentType("BANK_SLIP");

        // Then
        assertNotNull(gateway);
        assertTrue(gateway.getGatewayName().contains("Boleto"));
    }

    @Test
    @DisplayName("Deve falhar para tipo de pagamento não suportado")
    void shouldFailForUnsupportedPaymentType() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                PaymentGateway.forPaymentType("CRYPTOCURRENCY"));
    }

    @Test
    @DisplayName("Deve ser case insensitive para tipos de pagamento")
    void shouldBeCaseInsensitiveForPaymentTypes() {
        // When
        PaymentGateway gateway1 = PaymentGateway.forPaymentType("credit_card");
        PaymentGateway gateway2 = PaymentGateway.forPaymentType("CREDIT_CARD");
        PaymentGateway gateway3 = PaymentGateway.forPaymentType("Credit_Card");

        // Then
        assertEquals(gateway1.getGatewayName(), gateway2.getGatewayName());
        assertEquals(gateway2.getGatewayName(), gateway3.getGatewayName());
    }

    @Test
    @DisplayName("Deve retornar gateway com menor taxa")
    void shouldReturnGatewayWithLowestFee() {
        // When
        PaymentGateway lowestFeeGateway = PaymentGateway.withLowestFee();

        // Then
        assertNotNull(lowestFeeGateway);
        assertTrue(lowestFeeGateway.isAvailable());

        // Verifica se realmente tem a menor taxa comparando com outros
        PaymentGateway creditCard = PaymentGateway.forPaymentType("CREDIT_CARD");
        PaymentGateway pix = PaymentGateway.forPaymentType("PIX");
        PaymentGateway boleto = PaymentGateway.forPaymentType("BANK_SLIP");

        BigDecimal lowestFee = lowestFeeGateway.getFeeRate();
        assertTrue(lowestFee.compareTo(creditCard.getFeeRate()) <= 0 ||
                lowestFee.compareTo(pix.getFeeRate()) <= 0 ||
                lowestFee.compareTo(boleto.getFeeRate()) <= 0);
    }

    @Test
    @DisplayName("Deve otimizar gateway para valores pequenos")
    void shouldOptimizeGatewayForSmallAmounts() {
        // Given
        BigDecimal smallAmount = new BigDecimal("50.00");

        // When
        PaymentGateway gateway = PaymentGateway.optimizedForAmount(smallAmount);

        // Then
        assertNotNull(gateway);
        assertTrue(gateway.getGatewayName().contains("PIX")); // PIX é otimizado para valores pequenos
    }

    @Test
    @DisplayName("Deve otimizar gateway para valores médios")
    void shouldOptimizeGatewayForMediumAmounts() {
        // Given
        BigDecimal mediumAmount = new BigDecimal("1000.00");

        // When
        PaymentGateway gateway = PaymentGateway.optimizedForAmount(mediumAmount);

        // Then
        assertNotNull(gateway);
        assertTrue(gateway.getGatewayName().contains("CreditCard")); // Cartão para valores médios
    }

    @Test
    @DisplayName("Deve otimizar gateway para valores altos")
    void shouldOptimizeGatewayForHighAmounts() {
        // Given
        BigDecimal highAmount = new BigDecimal("10000.00");

        // When
        PaymentGateway gateway = PaymentGateway.optimizedForAmount(highAmount);

        // Then
        assertNotNull(gateway);
        assertTrue(gateway.getGatewayName().contains("Boleto")); // Boleto para valores altos
    }

    @Test
    @DisplayName("Deve processar pagamento com cartão de crédito")
    void shouldProcessCreditCardPayment() {
        // Given
        PaymentGateway gateway = PaymentGateway.forPaymentType("CREDIT_CARD");
        BigDecimal amount = new BigDecimal("250.00");
        String cardData = "4111-1111-1111-1111";

        // When
        PaymentGateway.PaymentResult result = gateway.processPayment(amount, cardData);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("CC_"));
        assertEquals(amount.subtract(result.getFee()), result.getProcessedAmount());
        assertTrue(result.getFee().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Deve rejeitar cartão que começa com 4000")
    void shouldRejectCardStartingWith4000() {
        // Given
        PaymentGateway gateway = PaymentGateway.forPaymentType("CREDIT_CARD");
        BigDecimal amount = new BigDecimal("100.00");
        String declinedCardData = "4000-1111-1111-1111";

        // When
        PaymentGateway.PaymentResult result = gateway.processPayment(amount, declinedCardData);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNull(result.getTransactionId());
        assertTrue(result.getMessage().contains("declined"));
    }

    @Test
    @DisplayName("Deve processar pagamento PIX")
    void shouldProcessPixPayment() {
        // Given
        PaymentGateway gateway = PaymentGateway.forPaymentType("PIX");
        BigDecimal amount = new BigDecimal("150.00");
        String pixKey = "usuario@email.com";

        // When
        PaymentGateway.PaymentResult result = gateway.processPayment(amount, pixKey);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("PIX_"));
        assertTrue(result.getMessage().contains("instantly"));
    }

    @Test
    @DisplayName("Deve rejeitar PIX acima do limite")
    void shouldRejectPixAboveLimit() {
        // Given
        PaymentGateway gateway = PaymentGateway.forPaymentType("PIX");
        BigDecimal excessiveAmount = new BigDecimal("60000.00");
        String pixKey = "usuario@email.com";

        // When
        PaymentGateway.PaymentResult result = gateway.processPayment(excessiveAmount, pixKey);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("exceeds PIX limit"));
    }

    @Test
    @DisplayName("Deve processar boleto")
    void shouldProcessBankSlip() {
        // Given
        PaymentGateway gateway = PaymentGateway.forPaymentType("BANK_SLIP");
        BigDecimal amount = new BigDecimal("500.00");
        String barcode = "12345678901234567890123456789012345678901234567";

        // When
        PaymentGateway.PaymentResult result = gateway.processPayment(amount, barcode);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("BOL_"));
        assertTrue(result.getMessage().contains("generated"));
    }

    @Test
    @DisplayName("Deve rejeitar boleto com valor abaixo do mínimo")
    void shouldRejectBankSlipBelowMinimum() {
        // Given
        PaymentGateway gateway = PaymentGateway.forPaymentType("BANK_SLIP");
        BigDecimal tooSmallAmount = new BigDecimal("5.00");
        String barcode = "12345678901234567890123456789012345678901234567";

        // When
        PaymentGateway.PaymentResult result = gateway.processPayment(tooSmallAmount, barcode);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("below minimum"));
    }
}