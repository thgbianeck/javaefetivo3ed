package br.com.bianeck.javaefetivo.capitulo02.item01.cache;

import br.com.bianeck.javaefetivo.capitulo02.item01.basic.PaymentMethod;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Demonstra o uso de cache em métodos static factory para melhorar performance.
 * Esta classe valida métodos de pagamento e cacheia os resultados para
 * evitar reprocessamento desnecessário.
 *
 * @author Thiago Bianeck
 */
public final class CachedPaymentValidator {

    /**
     * Cache para armazenar resultados de validação de métodos de pagamento.
     * Utiliza ConcurrentHashMap para acesso seguro em ambientes multithread.
     */
    private static final ConcurrentMap<String, ValidationResult> validationCache =
            new ConcurrentHashMap<>();

    private static volatile long cacheHits = 0; // Contador de acertos no cache
    private static volatile long cacheMisses = 0; // Contador de faltas no cache

    /**
     * Construtor privado para evitar instância da classe.
     * Esta é uma classe utilitária que não deve ser instanciada.
     */
    private CachedPaymentValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Valida um método de pagamento, utilizando cache para melhorar performance.
     *
     * Este método demonstra como static factories podem implementar caching
     * transparente para o cliente.
     *
     * @param paymentMethod método de pagamento a ser validado
     * @param amount valor da transação
     * @return resultado da validação (possivelmente do cache)
     */
    public static ValidationResult validatePayment(
            PaymentMethod paymentMethod,
            BigDecimal amount) {
        String cacheKey = generateCacheKey(paymentMethod, amount);

        // Tenta obter do cache primeiro
        ValidationResult cachedResult = validationCache.get(cacheKey);
        if (cachedResult != null) {
            cacheHits++;
            return cachedResult; // Retorna instância do cache
        }

        // Se não estiver no cache, executa a validação
        cacheMisses++;
        ValidationResult result = performValidation(paymentMethod, amount);

        // Armazena no cache para futuras consultas
        validationCache.put(cacheKey, result);

        return result;
    }

    /**
     * Realiza a validação do método de pagamento.
     * Este método simula uma validação complexa que justifica o uso de cache.
     *
     * @param paymentMethod método de pagamento a ser validado
     * @param amount valor da transação
     * @return resultado da validação
     */
    private static ValidationResult performValidation(
            PaymentMethod paymentMethod,
            BigDecimal amount) {
        // Simula validação complexa que justifica o cache

        // Valida valor
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult.invalidAmount();
        }

        // Valida limite do método de pagamento
        if (amount.compareTo(paymentMethod.getLimit()) > 0) {
            return ValidationResult.insufficientFunds();
        }

        // Validações específicas por tipo
        switch (paymentMethod.getType()) {
            case "CREDIT_CARD":
            case "DEBIT_CARD":
                return validateCardPayment(paymentMethod, amount);
            case "PIX":
                return validatePixPayment(paymentMethod, amount);
            case "BANK_SLIP":
                return validateBankSlipPayment(paymentMethod, amount);
            default:
                return ValidationResult.custom(false, "Unsupported payment method");
        }
    }

    /**
     * Valida um pagamento com cartão de crédito ou débito.
     * Simula validações como cartão expirado ou inválido.
     *
     * @param paymentMethod método de pagamento a ser validado
     * @param amount valor da transação
     * @return resultado da validação
     */
    private static ValidationResult validateCardPayment(
            PaymentMethod paymentMethod,
            BigDecimal amount) {
        // Simula validação de cartão (em produção, consultaria APIs externas)
        String identifier = paymentMethod.getIdentifier();

        // Simula cartão expirado
        if (identifier.startsWith("4000")) {
            return ValidationResult.expiredCard();
        }

        // Simula cartão inválido
        if (identifier.startsWith("4111")) {
            return ValidationResult.invalidCard();
        }

        return ValidationResult.success();
    }

    /**
     * Valida um pagamento via PIX.
     * Simula validações como limite de transação.
     * @param paymentMethod método de pagamento a ser validado
     * @param amount valor da transação
     * @return resultado da validação
     */
    private static ValidationResult validatePixPayment(
            PaymentMethod paymentMethod,
            BigDecimal amount) {
        // PIX tem limite menor para demonstração
        if (amount.compareTo(new BigDecimal("1000.00")) > 0) {
            return ValidationResult.custom(false, "PIX limit exceeded for this transaction");
        }

        return ValidationResult.success();
    }

    /**
     * Valida um pagamento via boleto bancário.
     * Simula validações como valor mínimo permitido.
     *
     * @param paymentMethod método de pagamento a ser validado
     * @param amount valor da transação
     * @return resultado da validação
     */
    private static ValidationResult validateBankSlipPayment(
            PaymentMethod paymentMethod,
            BigDecimal amount) {
        // Boleto não pode ser usado para valores muito pequenos
        if (amount.compareTo(new BigDecimal("10.00")) < 0) {
            return ValidationResult.custom(false, "Bank slip minimum amount not met");
        }

        return ValidationResult.success();
    }

    /**
     * Gera uma chave única para o cache baseada no tipo de pagamento e valor.
     * Esta chave é usada para armazenar e recuperar resultados de validação do cache.
     *
     * @param paymentMethod método de pagamento
     * @param amount valor da transação
     * @return chave única para o cache
     */
    private static String generateCacheKey(
            PaymentMethod paymentMethod,
            BigDecimal amount) {
        return paymentMethod.getType() + ":" +
                paymentMethod.getIdentifier().hashCode() + ":" +
                amount.toString();
    }

    /**
     * Limpa o cache de validação, reiniciando contadores de acertos e faltas.
     * Útil para testes ou quando se deseja reiniciar o estado do cache.
     */
    public static void clearCache() {
        validationCache.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }

    /**
     * Obtém as estatísticas do cache, incluindo acertos, faltas e tamanho atual.
     *
     * @return um objeto CacheStats contendo as estatísticas do cache
     */
    public static CacheStats getCacheStats() {
        return new CacheStats(cacheHits, cacheMisses, validationCache.size());
    }

    /**
     * Resultado da validação de um método de pagamento.
     * Contém informações sobre o sucesso da validação e mensagens de erro, se houver.
     */
    public static final class CacheStats {
        private final long hits;
        private final long misses;
        private final int size;

        /**
         * Construtor privado para criar uma instância de CacheStats.
         *
         * @param hits número de acertos no cache
         * @param misses número de faltas no cache
         * @param size tamanho atual do cache
         */
        private CacheStats(long hits, long misses, int size) {
            this.hits = hits;
            this.misses = misses;
            this.size = size;
        }

        /**
         * Obtém o número de acertos no cache.
         *
         * @return número de acertos
         */
        public long getHits() { return hits; }

        /**
         * Obtém o número de faltas no cache.
         *
         * @return número de faltas
         */
        public long getMisses() { return misses; }

        /**
         * Obtém o tamanho atual do cache.
         *
         * @return tamanho do cache
         */
        public int getSize() { return size; }

        /**
         * Calcula a taxa de acerto do cache.
         *
         * @return taxa de acerto como um valor entre 0.0 e 1.0
         */
        public double getHitRatio() {
            long total = hits + misses;
            return total == 0 ? 0.0 : (double) hits / total;
        }

        @Override
        public String toString() {
            return String.format("CacheStats{hits=%d, misses=%d, size=%d, hitRatio=%.2f%%}",
                    hits, misses, size, getHitRatio() * 100);
        }
    }
}