package br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.impl;


import br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.PaymentService;
import br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.PaymentServiceProvider;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementação de serviço de pagamento para cartões de crédito.
 *
 * @author Thiago Bianeck
 */
public class CreditCardPaymentService implements PaymentService {

    /**
     * Nome do provedor de serviço de pagamento.
     * Este nome é usado para identificar o serviço no sistema.
     */
    private static final String PROVIDER_NAME = "CreditCard Payment Service";
    private static final int PRIORITY = 2; // Prioridade do serviço, onde 1 é mais alto

    /**
     * Processa um pagamento usando cartão de crédito.
     *
     * @param amount o valor a ser processado
     * @param paymentData dados do cartão de crédito (número, validade, etc.)
     *
     * @return resultado do processamento do pagamento
     */
    @Override
    public PaymentProcessingResult processPayment(BigDecimal amount, String paymentData) {
        try {
            // Simula processamento de cartão de crédito
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return new PaymentProcessingResult(false, null, "Invalid amount",
                        BigDecimal.ZERO, PROVIDER_NAME);
            }

            if (paymentData == null || paymentData.trim().isEmpty()) {
                return new PaymentProcessingResult(false, null, "Invalid card data",
                        BigDecimal.ZERO, PROVIDER_NAME);
            }

            // Simula validação de cartão
            if (paymentData.startsWith("4000")) {
                return new PaymentProcessingResult(false, null, "Card declined",
                        BigDecimal.ZERO, PROVIDER_NAME);
            }

            // Simula processamento bem-sucedido
            String transactionId = "CC_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            return new PaymentProcessingResult(true, transactionId, "Payment processed successfully",
                    amount, PROVIDER_NAME);

        } catch (Exception e) {
            return new PaymentProcessingResult(false, null, "Processing error: " + e.getMessage(),
                    BigDecimal.ZERO, PROVIDER_NAME);
        }
    }

    /**
     * Retorna o nome do provedor de serviço de pagamento.
     *
     * @return o nome do provedor
     */
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * Verifica se o serviço suporta o tipo de pagamento especificado.
     *
     * @param paymentType o tipo de pagamento a ser verificado
     * @return true se o tipo de pagamento for suportado, false caso contrário
     */
    @Override
    public boolean supportsPaymentType(String paymentType) {
        return "CREDIT_CARD".equalsIgnoreCase(paymentType) ||
                "DEBIT_CARD".equalsIgnoreCase(paymentType);
    }

    /**
     * Retorna a prioridade do serviço de pagamento.
     * Serviços com prioridade mais alta são chamados primeiro.
     *
     * @return a prioridade do serviço
     */
    @Override
    public int getPriority() {
        return PRIORITY;
    }

    /**
     * Classe interna que implementa o provedor de serviços de pagamento.
     * Usada pelo ServiceLoader para instanciar o serviço.
     */
    public static class Provider implements PaymentServiceProvider {

        /**
         * Nome do provedor de serviço de pagamento.
         * Este nome é usado para identificar o serviço no sistema.
         *
         * @return o nome do provedor
         */
        @Override
        public PaymentService newService() {
            return new CreditCardPaymentService();
        }

        /**
         * Nome do provedor de serviço de pagamento.
         * Este nome é usado para identificar o serviço no sistema.
         *
         * @return o nome do provedor
         */
        @Override
        public String getProviderName() {
            return PROVIDER_NAME;
        }

        /**
         * Retorna os tipos de pagamento suportados por este serviço.
         *
         * @return um array de strings representando os tipos de pagamento suportados
         */
        @Override
        public String[] getSupportedPaymentTypes() {
            return new String[]{"CREDIT_CARD", "DEBIT_CARD"};
        }
    }
}