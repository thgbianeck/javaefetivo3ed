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

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Classe de demonstração que exemplifica todas as vantagens dos métodos static factory
 * apresentadas no Item 1 do Java Efetivo.
 * Esta classe serve como um exemplo prático de como usar todas as implementações
 * criadas neste projeto.
 *
 * @author Thiago Bianeck
 */
public class PaymentSystemDemo {

    /**
     * Método principal que executa a demonstração das vantagens dos métodos static factory.
     * Este método chama outros métodos para demonstrar cada uma das vantagens
     * discutidas no Item 1 do Java Efetivo.
     * @param args argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        System.out.println("=== DEMONSTRAÇÃO: MÉTODOS STATIC FACTORY ===\n");

        demonstrateNamingAdvantage();
        demonstrateCachingAdvantage();
        demonstrateFlexibilityAdvantage();
        demonstrateVariationAdvantage();
        demonstrateServiceProviderFramework();

        System.out.println("\n=== FIM DA DEMONSTRAÇÃO ===");
    }

    /**
     * Demonstra a primeira vantagem: nomes descritivos.
     */
    private static void demonstrateNamingAdvantage() {
        System.out.println("1. VANTAGEM DOS NOMES DESCRITIVOS");
        System.out.println("==================================");

        // Em vez de construtores confusos, usamos métodos com nomes claros
        PaymentMethod creditCard = PaymentMethod.creditCard("4111-1111-1111-1111", new BigDecimal("5000.00"));
        PaymentMethod pix = PaymentMethod.pix("usuario@email.com");
        PaymentMethod boleto = PaymentMethod.bankSlip("12345678901234567890123456789012345678901234567");

        System.out.println("Métodos de pagamento criados com nomes descritivos:");
        System.out.println("- Cartão de Crédito: " + creditCard);
        System.out.println("- PIX: " + pix);
        System.out.println("- Boleto: " + boleto);

        // Processadores com nomes que indicam claramente o comportamento
        PaymentProcessor immediate = PaymentProcessor.processImmediatePayment(creditCard, new BigDecimal("100.00"), "MERCHANT_001");
        PaymentProcessor scheduled = PaymentProcessor.schedulePayment(pix, new BigDecimal("50.00"), "MERCHANT_002");
        PaymentProcessor recurring = PaymentProcessor.setupRecurringPayment(boleto, new BigDecimal("200.00"), "MERCHANT_003");

        System.out.println("\nProcessadores com comportamentos específicos:");
        System.out.println("- Imediato: " + immediate);
        System.out.println("- Agendado: " + scheduled);
        System.out.println("- Recorrente: " + recurring);
        System.out.println();
    }

    /**
     * Demonstra a segunda vantagem: reutilização de instâncias e cache.
     */
    private static void demonstrateCachingAdvantage() {
        System.out.println("2. VANTAGEM DO CACHE E CONTROLE DE INSTÂNCIAS");
        System.out.println("==============================================");

        // Demonstra reutilização de instâncias
        ValidationResult success1 = ValidationResult.success();
        ValidationResult success2 = ValidationResult.success();
        ValidationResult invalidAmount1 = ValidationResult.invalidAmount();
        ValidationResult invalidAmount2 = ValidationResult.invalidAmount();

        System.out.println("Reutilização de instâncias (padrão Flyweight):");
        System.out.println("- success1 == success2: " + (success1 == success2));
        System.out.println("- invalidAmount1 == invalidAmount2: " + (invalidAmount1 == invalidAmount2));

        // Demonstra cache em validações
        PaymentMethod testCard = PaymentMethod.creditCard("4111-1111-1111-1111", new BigDecimal("1000.00"));
        BigDecimal amount = new BigDecimal("100.00");

        System.out.println("\nDemonstrando cache de validações:");

        // Primeira validação (cache miss)
        long start = System.nanoTime();
        ValidationResult result1 = CachedPaymentValidator.validatePayment(testCard, amount);
        long time1 = System.nanoTime() - start;

        // Segunda validação (cache hit)
        start = System.nanoTime();
        ValidationResult result2 = CachedPaymentValidator.validatePayment(testCard, amount);
        long time2 = System.nanoTime() - start;

        System.out.println("- Primeira validação: " + result1 + " (tempo: " + time1 + "ns)");
        System.out.println("- Segunda validação: " + result2 + " (tempo: " + time2 + "ns)");
        System.out.println("- Mesma instância: " + (result1 == result2));
        System.out.println("- Estatísticas do cache: " + CachedPaymentValidator.getCacheStats());
        System.out.println();
    }

    /**
     * Demonstra a terceira vantagem: podem retornar subtipos.
     */
    private static void demonstrateFlexibilityAdvantage() {
        System.out.println("3. VANTAGEM DA FLEXIBILIDADE DE TIPOS");
        System.out.println("=====================================");

        // Cliente não sabe qual implementação específica está recebendo
        PaymentGateway creditCardGateway = PaymentGateway.forPaymentType("CREDIT_CARD");
        PaymentGateway pixGateway = PaymentGateway.forPaymentType("PIX");
        PaymentGateway boletoGateway = PaymentGateway.forPaymentType("BANK_SLIP");

        System.out.println("Gateways criados por tipo (implementações ocultas):");
        System.out.println("- " + creditCardGateway);
        System.out.println("- " + pixGateway);
        System.out.println("- " + boletoGateway);

        // Seleção automática baseada em critérios
        PaymentGateway lowestFee = PaymentGateway.withLowestFee();
        PaymentGateway optimizedFor100 = PaymentGateway.optimizedForAmount(new BigDecimal("100.00"));
        PaymentGateway optimizedFor5000 = PaymentGateway.optimizedForAmount(new BigDecimal("5000.00"));

        System.out.println("\nSeleção automática de gateway:");
        System.out.println("- Menor taxa: " + lowestFee);
        System.out.println("- Otimizado para R$ 100: " + optimizedFor100);
        System.out.println("- Otimizado para R$ 5000: " + optimizedFor5000);

        // Processamento de pagamentos
        PaymentGateway.PaymentResult result = creditCardGateway.processPayment(
                new BigDecimal("250.00"), "4111-1111-1111-1111");
        System.out.println("\nResultado do processamento: " + result);
        System.out.println();
    }

    /**
     * Demonstra a quarta vantagem: classe retornada pode variar.
     */
    private static void demonstrateVariationAdvantage() {
        System.out.println("4. VANTAGEM DA VARIAÇÃO DE CLASSES");
        System.out.println("==================================");

        // Diferentes implementações baseadas no tamanho
        TransactionSet smallSet = TransactionSet.withCapacity(10);
        TransactionSet largeSet = TransactionSet.withCapacity(100);

        System.out.println("Conjuntos com implementações otimizadas:");
        System.out.println("- Pequeno (10 elementos): " + smallSet.getImplementationType());
        System.out.println("- Grande (100 elementos): " + largeSet.getImplementationType());

        // Adicionando transações
        for (int i = 1; i <= 5; i++) {
            smallSet.add("TX_SMALL_" + i);
            largeSet.add("TX_LARGE_" + i);
        }

        System.out.println("\nApós adicionar transações:");
        System.out.println("- " + smallSet);
        System.out.println("- " + largeSet);

        // Criação a partir de coleção existente
        TransactionSet fromCollection = TransactionSet.of(Arrays.asList("TX1", "TX2", "TX3"));
        TransactionSet fromVarargs = TransactionSet.of("TXA", "TXB", "TXC", "TXD");

        System.out.println("\nCriados a partir de dados existentes:");
        System.out.println("- Da coleção: " + fromCollection);
        System.out.println("- De varargs: " + fromVarargs);
        System.out.println();
    }

    /**
     * Demonstra a quinta vantagem: service provider framework.
     */
    private static void demonstrateServiceProviderFramework() {
        System.out.println("5. VANTAGEM DO SERVICE PROVIDER FRAMEWORK");
        System.out.println("==========================================");

        // Registra provedores de serviço
        PaymentServiceRegistry.registerProvider("CreditCard", new CreditCardPaymentService.Provider());
        PaymentServiceRegistry.registerProvider("PIX", new PixPaymentService.Provider());

        // Define provedor padrão
        PaymentServiceRegistry.setDefaultProvider("PIX");

        System.out.println("Provedores registrados: " + PaymentServiceRegistry.getAvailableProviders());
        System.out.println("Informações dos provedores:");
        PaymentServiceRegistry.getProviderInfo().forEach((name, types) ->
                System.out.println("- " + name + ": " + Arrays.toString(types)));

        // Usa o serviço padrão
        PaymentService defaultService = PaymentServiceRegistry.newInstance();
        PaymentService.PaymentProcessingResult result1 = defaultService.processPayment(
                new BigDecimal("150.00"), "usuario@email.com");

        System.out.println("\nUsando provedor padrão:");
        System.out.println("- Resultado: " + result1);

        // Usa provedor específico
        PaymentService creditCardService = PaymentServiceRegistry.newInstance("CreditCard");
        PaymentService.PaymentProcessingResult result2 = creditCardService.processPayment(
                new BigDecimal("300.00"), "4111-1111-1111-1111");

        System.out.println("\nUsando provedor específico:");
        System.out.println("- Resultado: " + result2);

        // Usa provedor otimizado para tipo de pagamento
        PaymentService optimizedService = PaymentServiceRegistry.newInstanceForPaymentType("CREDIT_CARD");
        PaymentService.PaymentProcessingResult result3 = optimizedService.processPayment(
                new BigDecimal("500.00"), "4111-1111-1111-1111");

        System.out.println("\nUsando provedor otimizado:");
        System.out.println("- Resultado: " + result3);

        // Limpa para não afetar outros testes
        PaymentServiceRegistry.clearProviders();
        System.out.println();
    }
}