package br.com.bianeck.javaefetivo.capitulo02.item01.examples;

import br.com.bianeck.javaefetivo.capitulo02.item01.basic.PaymentMethod;
import br.com.bianeck.javaefetivo.capitulo02.item01.basic.PaymentProcessor;
import br.com.bianeck.javaefetivo.capitulo02.item01.cache.CachedPaymentValidator;
import br.com.bianeck.javaefetivo.capitulo02.item01.cache.ValidationResult;
import br.com.bianeck.javaefetivo.capitulo02.item01.flexibility.PaymentGateway;
import br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.PaymentService;
import br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.PaymentServiceRegistry;
import br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.impl.CreditCardPaymentService;
import br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.impl.PixPaymentService;
import br.com.bianeck.javaefetivo.capitulo02.item01.variation.TransactionSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

/**
 * Teste de integração que demonstra o uso conjunto de todas as
 * implementações do projeto, simulando um fluxo completo de pagamento.
 *
 * @author Thiago Bianeck
 */
class PaymentSystemIntegrationTest {

    @BeforeEach
    void setUp() {
        // Limpa caches e registros antes de cada teste
        CachedPaymentValidator.clearCache();
        PaymentServiceRegistry.clearProviders();

        // Registra provedores de serviço
        PaymentServiceRegistry.registerProvider("CreditCard", new CreditCardPaymentService.Provider());
        PaymentServiceRegistry.registerProvider("PIX", new PixPaymentService.Provider());
        PaymentServiceRegistry.setDefaultProvider("PIX");
    }

    @Test
    @DisplayName("Deve processar fluxo completo de pagamento com cartão de crédito")
    void shouldProcessCompleteCreditCardPaymentFlow() {
        // 1. Criar método de pagamento usando static factory com nome descritivo
        PaymentMethod creditCard = PaymentMethod.creditCard("4222-1111-1111-1111", new BigDecimal("5000.00"));
        assertNotNull(creditCard);
        assertEquals("CREDIT_CARD", creditCard.getType());

        // 2. Validar pagamento usando cache
        BigDecimal amount = new BigDecimal("250.00");
        ValidationResult validation = CachedPaymentValidator.validatePayment(creditCard, amount);
        assertTrue(validation.isValid());

        // 3. Criar processador com nome descritivo
        PaymentProcessor processor = PaymentProcessor.processImmediatePayment(
                creditCard, amount, "MERCHANT_001");
        assertNotNull(processor);
        assertEquals(PaymentProcessor.ProcessingType.IMMEDIATE, processor.getProcessingType());

        // 4. Obter gateway otimizado usando static factory
        PaymentGateway gateway = PaymentGateway.optimizedForAmount(amount);
        assertNotNull(gateway);

        // 5. Processar pagamento
        PaymentGateway.PaymentResult gatewayResult = gateway.processPayment(amount, "4111-1111-1111-1111");
        assertTrue(gatewayResult.isSuccess());
        assertNotNull(gatewayResult.getTransactionId());

        // 6. Usar service provider framework
        PaymentService service = PaymentServiceRegistry.newInstanceForPaymentType("CREDIT_CARD");
        PaymentService.PaymentProcessingResult serviceResult = service.processPayment(amount, "4111-1111-1111-1111");
        assertTrue(serviceResult.isSuccess());

        // 7. Armazenar transação em conjunto otimizado
        TransactionSet transactions = TransactionSet.empty();
        transactions.add(processor.getTransactionId());
        transactions.add(gatewayResult.getTransactionId());
        transactions.add(serviceResult.getTransactionId());

        assertEquals(3, transactions.size());
        assertTrue(transactions.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Deve processar fluxo completo de pagamento PIX")
    void shouldProcessCompletePixPaymentFlow() {
        // 1. Criar método de pagamento PIX
        PaymentMethod pix = PaymentMethod.pix("usuario@email.com");
        assertNotNull(pix);
        assertEquals("PIX", pix.getType());

        // 2. Validar pagamento (primeira vez - cache miss)
        BigDecimal amount = new BigDecimal("100.00");
        ValidationResult validation1 = CachedPaymentValidator.validatePayment(pix, amount);
        assertTrue(validation1.isValid());

        // 3. Validar novamente (cache hit)
        ValidationResult validation2 = CachedPaymentValidator.validatePayment(pix, amount);
        assertTrue(validation2.isValid());
        assertSame(validation1, validation2); // Mesma instância do cache

        // 4. Verificar estatísticas do cache
        CachedPaymentValidator.CacheStats stats = CachedPaymentValidator.getCacheStats();
        assertEquals(1, stats.getHits());
        assertEquals(1, stats.getMisses());

        // 5. Criar processador agendado
        PaymentProcessor processor = PaymentProcessor.schedulePayment(pix, amount, "MERCHANT_002");
        assertEquals(PaymentProcessor.ProcessingType.SCHEDULED, processor.getProcessingType());

        // 6. Usar gateway com menor taxa
        PaymentGateway gateway = PaymentGateway.withLowestFee();
        PaymentGateway.PaymentResult result = gateway.processPayment(amount, "usuario@email.com");
        assertTrue(result.isSuccess());

        // 7. Usar service provider padrão (PIX)
        PaymentService defaultService = PaymentServiceRegistry.newInstance();
        assertEquals("PIX Payment Service", defaultService.getProviderName());

        PaymentService.PaymentProcessingResult serviceResult = defaultService.processPayment(amount, "usuario@email.com");
        assertTrue(serviceResult.isSuccess());
        assertTrue(serviceResult.getMessage().contains("instantly"));

        // 8. Criar conjunto de transações grande para demonstrar variação de implementação
        TransactionSet largeTransactionSet = TransactionSet.withCapacity(100);
        assertEquals("LargeTransactionSet", largeTransactionSet.getImplementationType());

        // Adicionar múltiplas transações
        for (int i = 1; i <= 10; i++) {
            largeTransactionSet.add("PIX_TX_" + i);
        }
        assertEquals(10, largeTransactionSet.size());
    }

    @Test
    @DisplayName("Deve demonstrar reutilização de instâncias com ValidationResult")
    void shouldDemonstrateInstanceReuseWithValidationResult() {
        // Criar múltiplos métodos de pagamento
        PaymentMethod card1 = PaymentMethod.creditCard("4222-1111-1111-1111", new BigDecimal("5000.00"));
        PaymentMethod card2 = PaymentMethod.creditCard("4222-2222-2222-2222", new BigDecimal("3000.00"));
        PaymentMethod expiredCard = PaymentMethod.creditCard("4000-1111-1111-1111", new BigDecimal("2000.00"));

        BigDecimal validAmount = new BigDecimal("100.00");
        BigDecimal invalidAmount = BigDecimal.ZERO;

        // Validações que devem retornar instâncias reutilizadas
        ValidationResult success1 = CachedPaymentValidator.validatePayment(card1, validAmount);
        ValidationResult success2 = CachedPaymentValidator.validatePayment(card2, validAmount);
        ValidationResult invalid1 = CachedPaymentValidator.validatePayment(card1, invalidAmount);
        ValidationResult invalid2 = CachedPaymentValidator.validatePayment(card2, invalidAmount);
        ValidationResult expired1 = CachedPaymentValidator.validatePayment(expiredCard, validAmount);
        ValidationResult expired2 = CachedPaymentValidator.validatePayment(expiredCard, new BigDecimal("200.00"));

        // Verificar reutilização de instâncias (padrão Flyweight)
        assertSame(ValidationResult.success(), success1);
        assertSame(ValidationResult.success(), success2);
        assertSame(success1, success2);

        assertSame(ValidationResult.invalidAmount(), invalid1);
        assertSame(ValidationResult.invalidAmount(), invalid2);
        assertSame(invalid1, invalid2);

        assertSame(ValidationResult.expiredCard(), expired1);
        assertSame(ValidationResult.expiredCard(), expired2);
        assertSame(expired1, expired2);
    }

    @Test
    @DisplayName("Deve demonstrar flexibilidade de tipos de retorno")
    void shouldDemonstrateReturnTypeFlexibility() {
        // Diferentes gateways para diferentes tipos, mas todos implementam PaymentGateway
        PaymentGateway creditCardGateway = PaymentGateway.forPaymentType("CREDIT_CARD");
        PaymentGateway pixGateway = PaymentGateway.forPaymentType("PIX");
        PaymentGateway boletoGateway = PaymentGateway.forPaymentType("BANK_SLIP");

        // Cliente não sabe qual implementação específica está usando
        assertNotNull(creditCardGateway);
        assertNotNull(pixGateway);
        assertNotNull(boletoGateway);

        // Mas todos têm comportamentos diferentes
        assertTrue(creditCardGateway.getGatewayName().contains("CreditCard"));
        assertTrue(pixGateway.getGatewayName().contains("PIX"));
        assertTrue(boletoGateway.getGatewayName().contains("Boleto"));

        // Taxas diferentes
        assertNotEquals(creditCardGateway.getFeeRate(), pixGateway.getFeeRate());
        assertNotEquals(pixGateway.getFeeRate(), boletoGateway.getFeeRate());

        // Gateway otimizado pode retornar qualquer uma das implementações
        PaymentGateway optimizedSmall = PaymentGateway.optimizedForAmount(new BigDecimal("50.00"));
        PaymentGateway optimizedLarge = PaymentGateway.optimizedForAmount(new BigDecimal("10000.00"));

        // Verificar que retornam implementações diferentes baseadas no valor
        assertNotEquals(optimizedSmall.getGatewayName(), optimizedLarge.getGatewayName());
    }

    @Test
    @DisplayName("Deve demonstrar variação de classes baseada em parâmetros")
    void shouldDemonstrateClassVariationBasedOnParameters() {
        // Diferentes implementações baseadas no tamanho
        TransactionSet small1 = TransactionSet.withCapacity(10);
        TransactionSet small2 = TransactionSet.withCapacity(64);
        TransactionSet large1 = TransactionSet.withCapacity(65);
        TransactionSet large2 = TransactionSet.withCapacity(1000);

        // Verificar que retornam implementações apropriadas
        assertEquals("SmallTransactionSet", small1.getImplementationType());
        assertEquals("SmallTransactionSet", small2.getImplementationType());
        assertEquals("LargeTransactionSet", large1.getImplementationType());
        assertEquals("LargeTransactionSet", large2.getImplementationType());

        // Adicionar transações para testar comportamento
        for (int i = 1; i <= 50; i++) {
            small1.add("SMALL_TX_" + i);
            large1.add("LARGE_TX_" + i);
        }

        assertEquals(50, small1.size());
        assertEquals(50, large1.size());

        // Ambos funcionam corretamente, mas com implementações otimizadas diferentes
        assertTrue(small1.contains("SMALL_TX_25"));
        assertTrue(large1.contains("LARGE_TX_25"));
    }

    @Test
    @DisplayName("Deve demonstrar service provider framework completo")
    void shouldDemonstrateCompleteServiceProviderFramework() {
        // Verificar provedores registrados
        assertTrue(PaymentServiceRegistry.isProviderRegistered("CreditCard"));
        assertTrue(PaymentServiceRegistry.isProviderRegistered("PIX"));

        // Obter informações dos provedores
        var providerInfo = PaymentServiceRegistry.getProviderInfo();
        assertEquals(2, providerInfo.size());

        // Testar diferentes formas de obter serviços

        // 1. Serviço padrão
        PaymentService defaultService = PaymentServiceRegistry.newInstance();
        assertEquals("PIX Payment Service", defaultService.getProviderName());

        // 2. Serviço específico
        PaymentService creditCardService = PaymentServiceRegistry.newInstance("CreditCard");
        assertEquals("CreditCard Payment Service", creditCardService.getProviderName());

        // 3. Serviço otimizado por tipo
        PaymentService pixOptimized = PaymentServiceRegistry.newInstanceForPaymentType("PIX");
        PaymentService creditOptimized = PaymentServiceRegistry.newInstanceForPaymentType("CREDIT_CARD");

        assertTrue(pixOptimized.supportsPaymentType("PIX"));
        assertTrue(creditOptimized.supportsPaymentType("CREDIT_CARD"));

        // Verificar prioridades (PIX tem prioridade maior = número menor)
        assertTrue(pixOptimized.getPriority() < creditOptimized.getPriority());

        // Processar pagamentos com diferentes serviços
        BigDecimal amount = new BigDecimal("200.00");

        var pixResult = pixOptimized.processPayment(amount, "usuario@email.com");
        var creditResult = creditOptimized.processPayment(amount, "4222-1111-1111-1111");

        assertTrue(pixResult.isSuccess());
        assertTrue(creditResult.isSuccess());

        assertTrue(pixResult.getTransactionId().startsWith("PIX_"));
        assertTrue(creditResult.getTransactionId().startsWith("CC_"));
    }

    @Test
    @DisplayName("Deve demonstrar benefícios de performance do cache")
    void shouldDemonstratePerformanceBenefitsOfCache() {
        PaymentMethod testCard = PaymentMethod.creditCard("4222-1111-1111-1111", new BigDecimal("5000.00"));
        BigDecimal amount = new BigDecimal("100.00");

        // Primeira validação (cache miss)
        long start1 = System.nanoTime();
        ValidationResult result1 = CachedPaymentValidator.validatePayment(testCard, amount);
        long time1 = System.nanoTime() - start1;

        // Segunda validação (cache hit)
        long start2 = System.nanoTime();
        ValidationResult result2 = CachedPaymentValidator.validatePayment(testCard, amount);
        long time2 = System.nanoTime() - start2;

        // Terceira validação (cache hit)
        long start3 = System.nanoTime();
        ValidationResult result3 = CachedPaymentValidator.validatePayment(testCard, amount);
        long time3 = System.nanoTime() - start3;

        // Verificar que retorna a mesma instância (cache funcionando)
        assertSame(result1, result2);
        assertSame(result2, result3);

        // Verificar estatísticas
        var stats = CachedPaymentValidator.getCacheStats();
        assertEquals(2, stats.getHits());
        assertEquals(1, stats.getMisses());
        assertEquals(0.67, stats.getHitRatio(), 0.01); // 2/(2+1)

        // Cache hits geralmente são mais rápidos (embora em testes unitários a diferença pode ser mínima)
        // O importante é que a mesma instância é retornada
        assertTrue(time2 <= time1 * 2); // Permite alguma variação por causa do JIT
        assertTrue(time3 <= time1 * 2);
    }

    @Test
    @DisplayName("Deve simular cenário de produção completo")
    void shouldSimulateCompleteProductionScenario() {
        // Cenário: Processamento de múltiplos pagamentos de diferentes tipos

        // 1. Criar diferentes métodos de pagamento
        PaymentMethod[] paymentMethods = {
                PaymentMethod.creditCard("4222-1111-1111-1111", new BigDecimal("5000.00")),
                PaymentMethod.debitCard("4222-2222-2222-2222", new BigDecimal("2000.00")),
                PaymentMethod.pix("usuario@email.com"),
                PaymentMethod.bankSlip("12345678901234567890123456789012345678901234567")
        };

        // 2. Diferentes valores para testar otimizações
        BigDecimal[] amounts = {
                new BigDecimal("50.00"),    // Pequeno - deve usar PIX
                new BigDecimal("500.00"),   // Médio - deve usar cartão
                new BigDecimal("5000.00"),  // Alto - deve usar boleto
                new BigDecimal("100.00")    // Teste geral
        };

        // 3. Conjunto para armazenar todas as transações
        TransactionSet allTransactions = TransactionSet.withCapacity(100);

        // 4. Processar cada combinação
        int successfulTransactions = 0;

        for (PaymentMethod method : paymentMethods) {
            for (BigDecimal amount : amounts) {
                try {
                    // Validar pagamento
                    ValidationResult validation = CachedPaymentValidator.validatePayment(method, amount);

                    if (validation.isValid()) {
                        // Criar processador
                        PaymentProcessor processor = PaymentProcessor.processImmediatePayment(
                                method, amount, "MERCHANT_PROD");

                        // Obter gateway otimizado
                        PaymentGateway gateway = PaymentGateway.optimizedForAmount(amount);

                        // Processar através do gateway
                        PaymentGateway.PaymentResult gatewayResult = gateway.processPayment(
                                amount, method.getIdentifier());

                        if (gatewayResult.isSuccess()) {
                            // Usar service provider
                            PaymentService service = PaymentServiceRegistry.newInstanceForPaymentType(method.getType());
                            PaymentService.PaymentProcessingResult serviceResult = service.processPayment(
                                    amount, method.getIdentifier());

                            if (serviceResult.isSuccess()) {
                                // Armazenar transações
                                allTransactions.add(processor.getTransactionId());
                                allTransactions.add(gatewayResult.getTransactionId());
                                allTransactions.add(serviceResult.getTransactionId());

                                successfulTransactions++;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Em produção, logar erro e continuar
                    System.err.println("Erro processando " + method.getType() + " com valor " + amount + ": " + e.getMessage());
                }
            }
        }

        // 5. Verificar resultados
        assertTrue(successfulTransactions > 0, "Deve ter pelo menos uma transação bem-sucedida");
        assertTrue(allTransactions.size() > 0, "Deve ter transações armazenadas");
        assertTrue(allTransactions.getTotalAmount().compareTo(BigDecimal.ZERO) > 0, "Deve ter valor total positivo");

        // 6. Verificar estatísticas do cache
        var cacheStats = CachedPaymentValidator.getCacheStats();
        assertTrue(cacheStats.getHits() > 0, "Deve ter hits no cache");
        assertTrue(cacheStats.getHitRatio() > 0, "Deve ter taxa de hit positiva");

        // 7. Verificar que diferentes implementações foram usadas
        assertEquals("LargeTransactionSet", allTransactions.getImplementationType(),
                "Deve usar implementação otimizada para muitas transações");

        System.out.println("Cenário de produção concluído:");
        System.out.println("- Transações bem-sucedidas: " + successfulTransactions);
        System.out.println("- Total de IDs de transação armazenados: " + allTransactions.size());
        System.out.println("- Valor total processado: " + allTransactions.getTotalAmount());
        System.out.println("- Estatísticas do cache: " + cacheStats);
        System.out.println("- Implementação do conjunto: " + allTransactions.getImplementationType());
    }
}