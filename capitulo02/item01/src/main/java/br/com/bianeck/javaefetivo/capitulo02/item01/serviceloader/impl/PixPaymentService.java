package br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.impl;

import br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.PaymentService;
import br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader.PaymentServiceProvider;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementação de serviço de pagamento PIX.
 *
 * @author Thiago Bianeck
 */
public class PixPaymentService implements PaymentService {

    private static final String PROVIDER_NAME = "PIX Payment Service";
    private static final int PRIORITY = 1; // Alta prioridade (PIX é instantâneo e barato)

    @Override
    public PaymentProcessingResult processPayment(BigDecimal amount, String paymentData) {
        try {
            // Validações específicas do PIX
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return new PaymentProcessingResult(false, null, "Invalid amount",
                        BigDecimal.ZERO, PROVIDER_NAME);
            }

            if (paymentData == null || paymentData.trim().isEmpty()) {
                return new PaymentProcessingResult(false, null, "Invalid PIX key",
                        BigDecimal.ZERO, PROVIDER_NAME);
            }

            // PIX tem limite por transação
            if (amount.compareTo(new BigDecimal("50000.00")) > 0) {
                return new PaymentProcessingResult(false, null, "Amount exceeds PIX limit",
                        BigDecimal.ZERO, PROVIDER_NAME);
            }

            // Simula processamento instantâneo do PIX
            String transactionId = "PIX_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            return new PaymentProcessingResult(true, transactionId, "PIX payment processed instantly",
                    amount, PROVIDER_NAME);

        } catch (Exception e) {
            return new PaymentProcessingResult(false, null, "PIX processing error: " + e.getMessage(),
                    BigDecimal.ZERO, PROVIDER_NAME);
        }
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean supportsPaymentType(String paymentType) {
        return "PIX".equalsIgnoreCase(paymentType);
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    /**
     * Provider para o serviço PIX.
     */
    public static class Provider implements PaymentServiceProvider {

        @Override
        public PaymentService newService() {
            return new PixPaymentService();
        }

        @Override
        public String getProviderName() {
            return PROVIDER_NAME;
        }

        @Override
        public String[] getSupportedPaymentTypes() {
            return new String[]{"PIX"};
        }
    }
}