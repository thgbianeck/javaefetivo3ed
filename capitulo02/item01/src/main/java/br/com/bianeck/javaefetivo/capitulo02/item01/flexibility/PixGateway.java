package br.com.bianeck.javaefetivo.capitulo02.item01.flexibility;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementação do gateway para pagamentos PIX.
 *
 * @author Thiago Bianeck
 */
class PixGateway implements PaymentGateway {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.005"); // 0.5%
    private static final String GATEWAY_NAME = "PIX Gateway";

    @Override
    public PaymentResult processPayment(BigDecimal amount, String paymentData) {
        try {
            // Validação específica do PIX
            if (paymentData == null || paymentData.trim().isEmpty()) {
                return new PaymentResult(false, null, "Invalid PIX key", BigDecimal.ZERO, BigDecimal.ZERO);
            }

            // PIX tem limite por transação
            if (amount.compareTo(new BigDecimal("50000.00")) > 0) {
                return new PaymentResult(false, null, "Amount exceeds PIX limit", BigDecimal.ZERO, BigDecimal.ZERO);
            }

            // Calcula taxa (muito baixa para PIX)
            BigDecimal fee = amount.multiply(FEE_RATE);
            BigDecimal processedAmount = amount.subtract(fee);

            String transactionId = "PIX_" + UUID.randomUUID().toString().substring(0, 8);

            return new PaymentResult(true, transactionId, "PIX payment processed successfully",
                    processedAmount, fee);

        } catch (Exception e) {
            return new PaymentResult(false, null, "PIX processing error: " + e.getMessage(),
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
        // PIX está disponível 24/7
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s (Fee: %s%%)", GATEWAY_NAME, FEE_RATE.multiply(new BigDecimal("100")));
    }
}