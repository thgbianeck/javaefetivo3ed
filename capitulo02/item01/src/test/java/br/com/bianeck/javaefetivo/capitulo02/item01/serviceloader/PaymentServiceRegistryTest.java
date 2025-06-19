package br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader;

import br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.impl.CreditCardPaymentService;
import br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.impl.PixPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Testes unitários para PaymentServiceRegistry.
 *
 * Verifica se o service provider framework está funcionando
 * corretamente e demonstra a quinta vantagem dos métodos static factory.
 *
 * @author Thiago Bianeck
 */
class PaymentServiceRegistryTest {

    @BeforeEach
    void setUp() {
        // Limpa provedores antes de cada teste
        PaymentServiceRegistry.clearProviders();
    }

    @Test
    @DisplayName("Deve registrar provedor corretamente")
    void shouldRegisterProviderCorrectly() {
        // Given
        PaymentServiceProvider provider = new CreditCardPaymentService.Provider();

        // When
        PaymentServiceRegistry.registerProvider("CreditCard", provider);

        // Then
        assertTrue(PaymentServiceRegistry.isProviderRegistered("CreditCard"));
        Set<String> availableProviders = PaymentServiceRegistry.getAvailableProviders();
        assertTrue(availableProviders.contains("CreditCard"));
    }

    @Test
    @DisplayName("Deve falhar ao registrar provedor duplicado")
    void shouldFailToRegisterDuplicateProvider() {
        // Given
        PaymentServiceProvider provider1 = new CreditCardPaymentService.Provider();
        PaymentServiceProvider provider2 = new CreditCardPaymentService.Provider();

        // When
        PaymentServiceRegistry.registerProvider("CreditCard", provider1);

        // Then
        assertThrows(IllegalArgumentException.class, () ->
                PaymentServiceRegistry.registerProvider("CreditCard", provider2));
    }

    @Test
    @DisplayName("Deve desregistrar provedor corretamente")
    void shouldUnregisterProviderCorrectly() {
        // Given
        PaymentServiceProvider provider = new CreditCardPaymentService.Provider();
        PaymentServiceRegistry.registerProvider("CreditCard", provider);

        // When
        boolean unregistered = PaymentServiceRegistry.unregisterProvider("CreditCard");

        // Then
        assertTrue(unregistered);
        assertFalse(PaymentServiceRegistry.isProviderRegistered("CreditCard"));
    }

    @Test
    @DisplayName("Deve retornar false ao desregistrar provedor inexistente")
    void shouldReturnFalseWhenUnregisteringNonExistentProvider() {
        // When
        boolean unregistered = PaymentServiceRegistry.unregisterProvider("NonExistent");

        // Then
        assertFalse(unregistered);
    }

    @Test
    @DisplayName("Deve definir e usar provedor padrão")
    void shouldSetAndUseDefaultProvider() {
        // Given
        PaymentServiceRegistry.registerProvider("PIX", new PixPaymentService.Provider());
        PaymentServiceRegistry.setDefaultProvider("PIX");

        // When
        PaymentService service = PaymentServiceRegistry.newInstance();

        // Then
        assertNotNull(service);
        assertEquals("PIX Payment Service", service.getProviderName());
        assertTrue(service.supportsPaymentType("PIX"));
    }

    @Test
    @DisplayName("Deve falhar ao definir provedor padrão não registrado")
    void shouldFailToSetUnregisteredDefaultProvider() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                PaymentServiceRegistry.setDefaultProvider("NonExistent"));
    }

    @Test
    @DisplayName("Deve falhar ao criar instância sem provedor padrão")
    void shouldFailToCreateInstanceWithoutDefaultProvider() {
        // When & Then
        assertThrows(IllegalStateException.class, () ->
                PaymentServiceRegistry.newInstance());
    }

    @Test
    @DisplayName("Deve criar instância com provedor específico")
    void shouldCreateInstanceWithSpecificProvider() {
        // Given
        PaymentServiceRegistry.registerProvider("CreditCard", new CreditCardPaymentService.Provider());

        // When
        PaymentService service = PaymentServiceRegistry.newInstance("CreditCard");

        // Then
        assertNotNull(service);
        assertEquals("CreditCard Payment Service", service.getProviderName());
        assertTrue(service.supportsPaymentType("CREDIT_CARD"));
    }

    @Test
    @DisplayName("Deve falhar ao criar instância com provedor não registrado")
    void shouldFailToCreateInstanceWithUnregisteredProvider() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                PaymentServiceRegistry.newInstance("NonExistent"));
    }

    @Test
    @DisplayName("Deve criar instância otimizada para tipo de pagamento")
    void shouldCreateOptimizedInstanceForPaymentType() {
        // Given
        PaymentServiceRegistry.registerProvider("CreditCard", new CreditCardPaymentService.Provider());
        PaymentServiceRegistry.registerProvider("PIX", new PixPaymentService.Provider());

        // When
        PaymentService pixService = PaymentServiceRegistry.newInstanceForPaymentType("PIX");
        PaymentService creditCardService = PaymentServiceRegistry.newInstanceForPaymentType("CREDIT_CARD");

        // Then
        assertNotNull(pixService);
        assertTrue(pixService.supportsPaymentType("PIX"));
        assertEquals(1, pixService.getPriority()); // PIX tem prioridade alta

        assertNotNull(creditCardService);
        assertTrue(creditCardService.supportsPaymentType("CREDIT_CARD"));
        assertEquals(2, creditCardService.getPriority()); // CreditCard tem prioridade média
    }

    @Test
    @DisplayName("Deve escolher provedor com maior prioridade")
    void shouldChooseProviderWithHighestPriority() {
        // Given - registra dois provedores que suportam o mesmo tipo
        PaymentServiceRegistry.registerProvider("CreditCard", new CreditCardPaymentService.Provider());
        PaymentServiceRegistry.registerProvider("PIX", new PixPaymentService.Provider());

        // When - PIX suporta apenas PIX, mas vamos testar com CREDIT_CARD
        PaymentService service = PaymentServiceRegistry.newInstanceForPaymentType("CREDIT_CARD");

        // Then
        assertNotNull(service);
        assertTrue(service.supportsPaymentType("CREDIT_CARD"));
    }

    @Test
    @DisplayName("Deve falhar ao buscar provedor para tipo não suportado")
    void shouldFailToFindProviderForUnsupportedType() {
        // Given
        PaymentServiceRegistry.registerProvider("PIX", new PixPaymentService.Provider());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                PaymentServiceRegistry.newInstanceForPaymentType("CRYPTOCURRENCY"));
    }

    @Test
    @DisplayName("Deve retornar informações dos provedores")
    void shouldReturnProviderInfo() {
        // Given
        PaymentServiceRegistry.registerProvider("CreditCard", new CreditCardPaymentService.Provider());
        PaymentServiceRegistry.registerProvider("PIX", new PixPaymentService.Provider());

        // When
        Map<String, String[]> providerInfo = PaymentServiceRegistry.getProviderInfo();

        // Then
        assertNotNull(providerInfo);
        assertEquals(2, providerInfo.size());

        assertTrue(providerInfo.containsKey("CreditCard"));
        assertTrue(providerInfo.containsKey("PIX"));

        String[] creditCardTypes = providerInfo.get("CreditCard");
        assertTrue(java.util.Arrays.asList(creditCardTypes).contains("CREDIT_CARD"));
        assertTrue(java.util.Arrays.asList(creditCardTypes).contains("DEBIT_CARD"));

        String[] pixTypes = providerInfo.get("PIX");
        assertTrue(java.util.Arrays.asList(pixTypes).contains("PIX"));
    }

    @Test
    @DisplayName("Deve processar pagamento através do serviço")
    void shouldProcessPaymentThroughService() {
        // Given
        PaymentServiceRegistry.registerProvider("PIX", new PixPaymentService.Provider());
        PaymentService service = PaymentServiceRegistry.newInstance("PIX");

        // When
        PaymentService.PaymentProcessingResult result = service.processPayment(
                new BigDecimal("150.00"), "usuario@email.com");

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("PIX_"));
        assertEquals(new BigDecimal("150.00"), result.getProcessedAmount());
        assertEquals("PIX Payment Service", result.getProviderName());
    }

    @Test
    @DisplayName("Deve limpar provedores corretamente")
    void shouldClearProvidersCorrectly() {
        // Given
        PaymentServiceRegistry.registerProvider("CreditCard", new CreditCardPaymentService.Provider());
        PaymentServiceRegistry.registerProvider("PIX", new PixPaymentService.Provider());
        PaymentServiceRegistry.setDefaultProvider("PIX");

        // When
        PaymentServiceRegistry.clearProviders();

        // Then
        assertTrue(PaymentServiceRegistry.getAvailableProviders().isEmpty());
        assertFalse(PaymentServiceRegistry.isProviderRegistered("CreditCard"));
        assertFalse(PaymentServiceRegistry.isProviderRegistered("PIX"));

        // Deve falhar ao tentar criar instância após limpar
        assertThrows(IllegalStateException.class, () ->
                PaymentServiceRegistry.newInstance());
    }

    @Test
    @DisplayName("Deve falhar com parâmetros nulos")
    void shouldFailWithNullParameters() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
                PaymentServiceRegistry.registerProvider(null, new PixPaymentService.Provider()));

        assertThrows(NullPointerException.class, () ->
                PaymentServiceRegistry.registerProvider("PIX", null));

        assertThrows(NullPointerException.class, () ->
                PaymentServiceRegistry.newInstanceForPaymentType(null));
    }
}