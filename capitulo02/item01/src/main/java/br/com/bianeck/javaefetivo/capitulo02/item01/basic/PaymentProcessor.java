package br.com.bianeck.javaefetivo.capitulo02.item01.basic;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Demonstra como métodos static factory podem ter nomes mais descritivos
 * que construtores, facilitando a compreensão do código.
 * Esta classe processa diferentes tipos de transações de pagamento.
 *
 * @author Thiago Bianeck
 */
public final class PaymentProcessor {

    private final String transactionId; // Identificador único da transação
    private final PaymentMethod paymentMethod; // Método de pagamento utilizado (cartão, PIX, etc.)
    private final BigDecimal amount; // Valor da transação
    private final LocalDateTime timestamp; // Data e hora
    private final String merchantId; // Identificador do comerciante ou estabelecimento
    private final ProcessingType processingType; // Tipo de processamento (imediato, agendado, recorrente)

    /**
     * Enum que define os tipos de processamento de pagamento.
     * Demonstra como métodos static factory podem retornar diferentes
     * tipos de processamento com base no contexto.
     */
    public enum ProcessingType {
        IMMEDIATE, SCHEDULED, RECURRING
    }

    /**
     * Construtor privado para forçar o uso dos métodos static factory.
     * Este construtor é privado para evitar a criação de instâncias
     * diretamente, garantindo que o cliente utilize os métodos estáticos.
     *
     * @param paymentMethod o método de pagamento utilizado
     * @param amount o valor da transação
     * @param merchantId o identificador do comerciante
     * @param processingType o tipo de processamento
     */
    private PaymentProcessor(
            PaymentMethod paymentMethod,
            BigDecimal amount,
            String merchantId,
            ProcessingType processingType) {
        this.transactionId = UUID.randomUUID().toString();
        this.paymentMethod = Objects.requireNonNull(paymentMethod);
        this.amount = Objects.requireNonNull(amount);
        this.merchantId = Objects.requireNonNull(merchantId);
        this.processingType = Objects.requireNonNull(processingType);
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Processa um pagamento imediatamente.
     * Nome claro indica que o pagamento será processado de forma imediata.
     * Este método é útil para transações que não precisam ser agendadas
     * ou recorrentes.
     * @param paymentMethod o método de pagamento a ser utilizado (cartão de crédito, PIX, etc.)
     * @param amount o valor da transação
     * @param merchantId o identificador do comerciante
     * @return uma instância de PaymentProcessor configurada para processamento imediato
     * @throws IllegalArgumentException se o valor for inválido (nulo ou negativo)
     */
    public static PaymentProcessor processImmediatePayment(
            PaymentMethod paymentMethod,
            BigDecimal amount,
            String merchantId) {
        validateAmount(amount);
        return new PaymentProcessor(paymentMethod, amount, merchantId, ProcessingType.IMMEDIATE);
    }

    /**
     * Agenda um pagamento para ser processado posteriormente.
     * Nome claro indica que o pagamento será agendado.
     * Este método é útil para transações que precisam ser
     * processadas em um momento futuro,
     * como pagamentos de contas ou assinaturas.
     * @param paymentMethod o método de pagamento a ser utilizado (cartão de crédito, PIX, etc.)
     * @param amount o valor da transação
     * @param merchantId o identificador do comerciante
     * @return uma instância de PaymentProcessor configurada para agendamento
     * @throws IllegalArgumentException se o valor for inválido (nulo ou negativo)
     */
    public static PaymentProcessor schedulePayment(
            PaymentMethod paymentMethod,
            BigDecimal amount,
            String merchantId) {
        validateAmount(amount);
        return new PaymentProcessor(paymentMethod, amount, merchantId, ProcessingType.SCHEDULED);
    }

    /**
     * Configura um pagamento recorrente.
     * Nome claro indica que o pagamento será configurado para ser
     * processado periodicamente, como mensalmente ou anualmente.
     * Este método é útil para assinaturas ou pagamentos regulares.
     * @param paymentMethod o método de pagamento a ser utilizado (cartão de crédito, PIX, etc.)
     * @param amount o valor da transação
     * @param merchantId o identificador do comerciante
     * @return uma instância de PaymentProcessor configurada para pagamentos recorrentes
     * @throws IllegalArgumentException se o valor for inválido (nulo ou negativo)
     */
    public static PaymentProcessor setupRecurringPayment(
            PaymentMethod paymentMethod,
            BigDecimal amount,
            String merchantId) {
        validateAmount(amount);
        return new PaymentProcessor(paymentMethod, amount, merchantId, ProcessingType.RECURRING);
    }

    /**
     * Valida o valor da transação.
     * Garante que o valor seja positivo e não exceda um limite máximo.
     * Este método é chamado antes de criar uma instância de PaymentProcessor
     * para garantir que os valores sejam válidos.
     *
     * @param amount o valor a ser validado
     * @throws IllegalArgumentException se o valor for nulo, negativo ou exceder o limite máximo
     */
    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount.compareTo(new BigDecimal("100000.00")) > 0) {
            throw new IllegalArgumentException("Amount exceeds maximum limit");
        }
    }

    /**
     * Retorna o ID da transação.
     * Este ID é gerado automaticamente quando a instância é criada.
     * @return o ID da transação
     */
    public String getTransactionId() { return transactionId; }

    /**
     * Retorna o método de pagamento utilizado.
     * Este método pode ser um cartão de crédito, PIX, etc.
     * @return o método de pagamento
     */
    public PaymentMethod getPaymentMethod() { return paymentMethod; }

    /**
     * Retorna o valor da transação.
     * Este é o valor que será processado pelo método de pagamento.
     * @return o valor da transação
     */
    public BigDecimal getAmount() { return amount; }

    /**
     * Retorna a data e hora em que a transação foi criada.
     * Este timestamp é gerado automaticamente quando a instância é criada.
     * @return a data e hora da transação
     */
    public LocalDateTime getTimestamp() { return timestamp; }

    /**
     * Retorna o identificador do comerciante.
     * Este ID é usado para identificar o comerciante ou estabelecimento
     * que está processando a transação.
     * @return o ID do comerciante
     */
    public String getMerchantId() { return merchantId; }

    /**
     * Retorna o tipo de processamento da transação.
     * Este pode ser imediato, agendado ou recorrente.
     * @return o tipo de processamento
     */
    public ProcessingType getProcessingType() { return processingType; }

    /**
     * Verifica se a transação é do tipo agendada.
     * Este método é útil para identificar transações que não são processadas imediatamente.
     * @return true se o processamento for agendado, false caso contrário
     */
    @Override
    public String toString() {
        return String.format("PaymentProcessor{id='%s', type=%s, amount=%s, merchant='%s'}",
                transactionId, processingType, amount, merchantId);
    }
}