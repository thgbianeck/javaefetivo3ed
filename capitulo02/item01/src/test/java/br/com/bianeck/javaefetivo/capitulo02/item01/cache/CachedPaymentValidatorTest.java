package br.com.bianeck.javaefetivo.capitulo02.item01.cache;

import br.com.bianeck.javaefetivo.capitulo02.item01.basic.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

/**
 * Testes unitários para CachedPaymentValidator.
 *
 * Verifica se o cache está funcionando corretamente e melhorando
 * a performance das validações.
 *
 * @author Thiago Bianeck
 */
class CachedPaymentValidatorTest {

    @BeforeEach
    void setUp() {
        // Limpa o cache antes de cada teste
        CachedPaymentValidator.clearCache();
    }

    @Test
    @DisplayName("Deve validar pagamento com cartão de crédito válido")
    void shouldValidateValidCreditCardPayment() {
        // Given
        PaymentMethod creditCard = PaymentMethod.creditCard("4222-1111-1111-1111", new BigDecimal("5000.00"));
        BigDecimal amount = new BigDecimal("100.00");

        // When
        ValidationResult result = CachedPaymentValidator.validatePayment(creditCard, amount);

        // Then
        assertTrue(result.isValid());
        assertEquals("Validation successful", result.getMessage());
    }

    @Test
    @DisplayName("Deve rejeitar pagamento com valor inválido")
    void shouldRejectPaymentWithInvalidAmount() {
        // Given
        PaymentMethod creditCard = PaymentMethod.creditCard("4111-1111-1111-1111", new BigDecimal("5000.00"));
        BigDecimal invalidAmount = BigDecimal.ZERO;

        // When
        ValidationResult result = CachedPaymentValidator.validatePayment(creditCard, invalidAmount);

        // Then
        assertFalse(result.isValid());
        assertSame(ValidationResult.invalidAmount(), result);
    }

    @Test
    @DisplayName("Deve rejeitar pagamento que excede limite")
    void shouldRejectPaymentExceedingLimit() {
        // Given
        PaymentMethod creditCard = PaymentMethod.creditCard("4111-1111-1111-1111", new BigDecimal("1000.00"));
        BigDecimal excessiveAmount = new BigDecimal("2000.00");

        // When
        ValidationResult result = CachedPaymentValidator.validatePayment(creditCard, excessiveAmount);

        // Then
        assertFalse(result.isValid());
        assertSame(ValidationResult.insufficientFunds(), result);
    }

    @Test
    @DisplayName("Deve rejeitar cartão expirado")
    void shouldRejectExpiredCard() {
        // Given - cartão que começa com 4000 simula cartão expirado
        PaymentMethod expiredCard = PaymentMethod.creditCard("4000-1111-1111-1111", new BigDecimal("5000.00"));
        BigDecimal amount = new BigDecimal("100.00");

        // When
        ValidationResult result = CachedPaymentValidator.validatePayment(expiredCard, amount);

        // Then
        assertFalse(result.isValid());
        assertSame(ValidationResult.expiredCard(), result);
    }

    @Test
    @DisplayName("Deve usar cache para validações repetidas")
    void shouldUseCacheForRepeatedValidations() {
        // Given
        PaymentMethod creditCard = PaymentMethod.creditCard("4111-1111-1111-1111", new BigDecimal("5000.00"));
        BigDecimal amount = new BigDecimal("100.00");

        // When - primeira validação (cache miss)
        ValidationResult result1 = CachedPaymentValidator.validatePayment(creditCard, amount);
        CachedPaymentValidator.CacheStats statsAfterFirst = CachedPaymentValidator.getCacheStats();

        // When - segunda validação (cache hit)
        ValidationResult result2 = CachedPaymentValidator.validatePayment(creditCard, amount);
        CachedPaymentValidator.CacheStats statsAfterSecond = CachedPaymentValidator.getCacheStats();

        // Then
        assertSame(result1, result2, "Deve retornar a mesma instância do cache");

        assertEquals(0, statsAfterFirst.getHits());
        assertEquals(1, statsAfterFirst.getMisses());

        assertEquals(1, statsAfterSecond.getHits());
        assertEquals(1, statsAfterSecond.getMisses());
        assertEquals(0.5, statsAfterSecond.getHitRatio(), 0.01);
    }

    @Test
    @DisplayName("Deve validar PIX com limite específico")
    void shouldValidatePixWithSpecificLimit() {
        // Given
        PaymentMethod pix = PaymentMethod.pix("usuario@email.com");
        BigDecimal validAmount = new BigDecimal("500.00");
        BigDecimal excessiveAmount = new BigDecimal("1500.00");

        // When
        ValidationResult validResult = CachedPaymentValidator.validatePayment(pix, validAmount);
        ValidationResult invalidResult = CachedPaymentValidator.validatePayment(pix, excessiveAmount);

        // Then
        assertTrue(validResult.isValid());
        assertFalse(invalidResult.isValid());
        assertTrue(invalidResult.getMessage().contains("PIX limit exceeded"));
    }

    @Test
    @DisplayName("Deve validar boleto com valor mínimo")
    void shouldValidateBankSlipWithMinimumAmount() {
        // Given
        PaymentMethod boleto = PaymentMethod.bankSlip("12345678901234567890123456789012345678901234567");
        BigDecimal validAmount = new BigDecimal("50.00");
        BigDecimal tooSmallAmount = new BigDecimal("5.00");

        // When
        ValidationResult validResult = CachedPaymentValidator.validatePayment(boleto, validAmount);
        ValidationResult invalidResult = CachedPaymentValidator.validatePayment(boleto, tooSmallAmount);

        // Then
        assertTrue(validResult.isValid());
        assertFalse(invalidResult.isValid());
        assertTrue(invalidResult.getMessage().contains("minimum amount not met"));
    }

    @Test
    @DisplayName("Deve limpar cache corretamente")
    void shouldClearCacheCorrectly() {
        // Given
        PaymentMethod creditCard = PaymentMethod.creditCard("4111-1111-1111-1111", new BigDecimal("5000.00"));
        BigDecimal amount = new BigDecimal("100.00");

        // When - adiciona item ao cache
        CachedPaymentValidator.validatePayment(creditCard, amount);
        CachedPaymentValidator.CacheStats statsBeforeClear = CachedPaymentValidator.getCacheStats();

        // When - limpa cache
        CachedPaymentValidator.clearCache();
        CachedPaymentValidator.CacheStats statsAfterClear = CachedPaymentValidator.getCacheStats();

        // Then
        assertTrue(statsBeforeClear.getSize() > 0);
        assertEquals(0, statsAfterClear.getSize());
        assertEquals(0, statsAfterClear.getHits());
        assertEquals(0, statsAfterClear.getMisses());
    }

    @Test
    @DisplayName("Deve calcular estatísticas do cache corretamente")
    void shouldCalculateCacheStatsCorrectly() {
        // Given
        PaymentMethod card1 = PaymentMethod.creditCard("4111-1111-1111-1111", new BigDecimal("5000.00"));
        PaymentMethod card2 = PaymentMethod.creditCard("4222-2222-2222-2222", new BigDecimal("3000.00"));
        BigDecimal amount = new BigDecimal("100.00");

        // When
        CachedPaymentValidator.validatePayment(card1, amount); // miss
        CachedPaymentValidator.validatePayment(card2, amount); // miss
        CachedPaymentValidator.validatePayment(card1, amount); // hit
        CachedPaymentValidator.validatePayment(card2, amount); // hit
        CachedPaymentValidator.validatePayment(card1, amount); // hit

        CachedPaymentValidator.CacheStats stats = CachedPaymentValidator.getCacheStats();

        // Then
        assertEquals(3, stats.getHits());
        assertEquals(2, stats.getMisses());
        assertEquals(2, stats.getSize());
        assertEquals(0.6, stats.getHitRatio(), 0.01); // 3/(3+2) = 0.6
    }
}