package br.com.bianeck.javaefetivo.capitulo02.item01.flexibility;

import java.math.BigDecimal;

/**
 * Interface que representa um gateway de pagamento.
 *
 * Demonstra a terceira vantagem dos métodos static factory: podem retornar
 * objetos de qualquer subtipo do tipo de retorno declarado.
 *
 * @author Thiago Bianeck
 */
public interface PaymentGateway {

    /**
     * Processa um pagamento através do gateway.
     *
     * @param amount valor a ser processado
     * @param paymentData dados específicos do pagamento
     * @return resultado do processamento
     */
    PaymentResult processPayment(BigDecimal amount, String paymentData);

    /**
     * Retorna o nome do gateway.
     *
     * @return nome do gateway
     */
    String getGatewayName();

    /**
     * Retorna as taxas aplicadas pelo gateway.
     *
     * @return taxa como percentual (ex: 0.03 para 3%)
     */
    BigDecimal getFeeRate();

    /**
     * Verifica se o gateway está disponível.
     *
     * @return true se disponível
     */
    boolean isAvailable();

    /**
     * Método static factory que retorna a implementação apropriada
     * baseada no tipo de pagamento.
     *
     * Este método demonstra como static factories podem ocultar as
     * classes de implementação, retornando diferentes subtipos
     * baseados nos parâmetros de entrada.
     *
     * @param paymentType tipo de pagamento
     * @return implementação apropriada do PaymentGateway
     * @throws IllegalArgumentException se o tipo não for suportado
     */
    static PaymentGateway forPaymentType(String paymentType) {
        switch (paymentType.toUpperCase()) {
            case "CREDIT_CARD":
            case "DEBIT_CARD":
                return new CreditCardGateway();
            case "PIX":
                return new PixGateway();
            case "BANK_SLIP":
                return new BoletoGateway();
            default:
                throw new IllegalArgumentException("Unsupported payment type: " + paymentType);
        }
    }

    /**
     * Método static factory que retorna o gateway com menor taxa.
     *
     * Demonstra como static factories podem implementar lógica de seleção
     * complexa, retornando diferentes implementações baseadas em critérios
     * de negócio.
     *
     * @return gateway com menor taxa disponível
     */
    static PaymentGateway withLowestFee() {
        PaymentGateway[] gateways = {
                new CreditCardGateway(),
                new PixGateway(),
                new BoletoGateway()
        };

        PaymentGateway lowest = gateways[0];
        for (PaymentGateway gateway : gateways) {
            if (gateway.isAvailable() &&
                    gateway.getFeeRate().compareTo(lowest.getFeeRate()) < 0) {
                lowest = gateway;
            }
        }

        return lowest;
    }

    /**
     * Método static factory que retorna gateway baseado no valor da transação.
     *
     * Demonstra como static factories podem escolher implementações
     * baseadas em regras de negócio complexas.
     *
     * @param amount valor da transação
     * @return gateway otimizado para o valor
     */
    static PaymentGateway optimizedForAmount(BigDecimal amount) {
        // Para valores pequenos, PIX é mais eficiente
        if (amount.compareTo(new BigDecimal("100.00")) <= 0) {
            return new PixGateway();
        }

        // Para valores médios, cartão de crédito
        if (amount.compareTo(new BigDecimal("5000.00")) <= 0) {
            return new CreditCardGateway();
        }

        // Para valores altos, boleto (menor taxa)
        return new BoletoGateway();
    }

    /**
     * Classe que representa o resultado de um processamento de pagamento.
     */
    final class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final String message;
        private final BigDecimal processedAmount;
        private final BigDecimal fee;

        /**
         * Construtor para o resultado do processamento de pagamento.
         *
         * @param success indica se o pagamento foi bem-sucedido
         * @param transactionId ID da transação processada
         * @param message mensagem de status do processamento
         * @param processedAmount valor processado
         * @param fee taxa aplicada ao processamento
         */
        public PaymentResult(boolean success, String transactionId, String message,
                             BigDecimal processedAmount, BigDecimal fee) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
            this.processedAmount = processedAmount;
            this.fee = fee;
        }

        /**
         * @return true se o pagamento foi bem-sucedido
         */
        public boolean isSuccess() { return success; }

        /**
         * @return ID da transação processada
         */
        public String getTransactionId() { return transactionId; }

        /**
         * @return mensagem de status do processamento
         */
        public String getMessage() { return message; }

        /**
         * @return valor processado
         */
        public BigDecimal getProcessedAmount() { return processedAmount; }

        /**
         * @return taxa aplicada ao processamento
         */
        public BigDecimal getFee() { return fee; }

        /**
         * Representação em string do resultado do processamento.
         *
         * @return string formatada com os detalhes do resultado
         */
        @Override
        public String toString() {
            return String.format("PaymentResult{success=%s, txId='%s', amount=%s, fee=%s}",
                    success, transactionId, processedAmount, fee);
        }
    }
}