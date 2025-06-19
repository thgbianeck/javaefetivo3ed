package br.com.bianeck.javaefetivo.capitulo02.item01.flexibility;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementação do gateway para pagamentos com cartão de crédito/débito.
 *
 * Esta classe não é pública no package, demonstrando como static factories
 * podem ocultar implementações específicas.
 *
 * @author Thiago Bianeck
 */
class CreditCardGateway implements PaymentGateway {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.035"); // 3.5%
    private static final String GATEWAY_NAME = "CreditCard Gateway";

    @Override
    public PaymentResult processPayment(BigDecimal amount, String paymentData) {
        // Simula processamento de cartão de crédito
        try {
            // Simula validação do cartão
            if (paymentData == null || paymentData.trim().isEmpty()) {
                return new PaymentResult(false, null, "Invalid card data", BigDecimal.ZERO, BigDecimal.ZERO);
            }

            // Simula falha para cartões específicos
            if (paymentData.contains("4000")) {
                return new PaymentResult(false, null, "Card declined", BigDecimal.ZERO, BigDecimal.ZERO);
            }

            // Calcula taxa
            BigDecimal fee = amount.multiply(FEE_RATE);
            BigDecimal processedAmount = amount.subtract(fee);

            String transactionId = "CC_" + UUID.randomUUID().toString().substring(0, 8);

            return new PaymentResult(true, transactionId, "Payment processed successfully",
                    processedAmount, fee);

        } catch (Exception e) {
            return new PaymentResult(false, null, "Processing error: " + e.getMessage(),
                    BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    @Override
    public String getGatewayName() {
        return GATEWAY_NAME;
    }

    @Override
    public BigDecimal getFeeRate() {
        return FEE_RATE;
    }

    @Override
    public boolean isAvailable() {
        // Simula verificação de disponibilidade do gateway
        return true; // Sempre disponível para demonstração
    }

    @Override
    public String toString() {
        return String.format("%s (Fee: %s%%)", GATEWAY_NAME, FEE_RATE.multiply(new BigDecimal("100")));
    }
}