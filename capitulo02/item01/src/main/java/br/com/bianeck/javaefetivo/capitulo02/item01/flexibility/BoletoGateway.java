package br.com.bianeck.javaefetivo.capitulo02.item01.flexibility;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Implementação do gateway para pagamentos com boleto bancário.
 *
 * @author Thiago Bianeck
 */
class BoletoGateway implements PaymentGateway {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.015"); // 1.5%
    private static final String GATEWAY_NAME = "Boleto Gateway";

    @Override
    public PaymentResult processPayment(BigDecimal amount, String paymentData) {
        try {
            // Validação específica do boleto
            if (paymentData == null || paymentData.length() != 47) {
                return new PaymentResult(false, null, "Invalid boleto barcode", BigDecimal.ZERO, BigDecimal.ZERO);
            }

            // Boleto tem valor mínimo
            if (amount.compareTo(new BigDecimal("10.00")) < 0) {
                return new PaymentResult(false, null, "Amount below minimum for boleto",
                        BigDecimal.ZERO, BigDecimal.ZERO);
            }

            // Calcula taxa
            BigDecimal fee = amount.multiply(FEE_RATE);
            BigDecimal processedAmount = amount.subtract(fee);

            String transactionId = "BOL_" + UUID.randomUUID().toString().substring(0, 8);

            return new PaymentResult(true, transactionId, "Boleto generated successfully",
                    processedAmount, fee);

        } catch (Exception e) {
            return new PaymentResult(false, null, "Boleto processing error: " + e.getMessage(),
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
        // Boleto só está disponível em horário comercial (simulação)
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(18, 0));
    }

    @Override
    public String toString() {
        return String.format("%s (Fee: %s%%) - Available: %s",
                GATEWAY_NAME,
                FEE_RATE.multiply(new BigDecimal("100")),
                isAvailable() ? "Yes" : "No");
    }
}