package br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader;

import java.math.BigDecimal;

/**
 * Interface de serviço que representa um provedor de pagamento.
 *
 * Esta é a "service interface" no padrão Service Provider Framework,
 * demonstrando a quinta vantagem dos métodos static factory.
 *
 * @author Thiago Bianeck
 */
public interface PaymentService {

    /**
     * Processa um pagamento.
     *
     * @param amount valor do pagamento
     * @param paymentData dados do pagamento
     * @return resultado do processamento
     */
    PaymentProcessingResult processPayment(BigDecimal amount, String paymentData);

    /**
     * Retorna o nome do provedor de pagamento.
     *
     * @return nome do provedor
     */
    String getProviderName();

    /**
     * Verifica se o provedor suporta o tipo de pagamento.
     *
     * @param paymentType tipo de pagamento
     * @return true se suportado
     */
    boolean supportsPaymentType(String paymentType);

    /**
     * Retorna a prioridade do provedor (menor número = maior prioridade).
     *
     * @return prioridade do provedor
     */
    int getPriority();

    /**
     * Classe que representa o resultado do processamento.
     */
    final class PaymentProcessingResult {
        private final boolean success;
        private final String transactionId;
        private final String message;
        private final BigDecimal processedAmount;
        private final String providerName;

        /**
         * Construtor para o resultado do processamento de pagamento.
         *
         * @param success indica se o processamento foi bem-sucedido
         * @param transactionId ID da transação processada
         * @param message mensagem de status do processamento
         * @param processedAmount valor processado
         * @param providerName nome do provedor de pagamento
         */
        public PaymentProcessingResult(boolean success, String transactionId, String message,
                                       BigDecimal processedAmount, String providerName) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
            this.processedAmount = processedAmount;
            this.providerName = providerName;
        }


        /**
         * @return true se o processamento foi bem-sucedido
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
         * @return nome do provedor de pagamento
         */
        public String getProviderName() { return providerName; }

        /**
         * Retorna uma representação em string do resultado do processamento.
         *
         * @return string formatada com os detalhes do resultado
         */
        @Override
        public String toString() {
            return String.format("PaymentResult{success=%s, txId='%s', provider='%s', amount=%s}",
                    success, transactionId, providerName, processedAmount);
        }
    }
}