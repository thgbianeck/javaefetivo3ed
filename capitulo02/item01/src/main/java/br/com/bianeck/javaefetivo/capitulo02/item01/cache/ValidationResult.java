package br.com.bianeck.javaefetivo.capitulo02.item01.cache;

import java.util.Objects;

/**
 * Demonstra a segunda vantagem dos métodos static factory: não precisam criar
 * um novo objeto a cada invocação.
 * Esta classe implementa o padrão Flyweight para resultados de validação,
 * reutilizando instâncias comuns para melhorar a performance.
 * @author Thiago Bianeck
 */
public final class ValidationResult {

    /* Instâncias pré-criadas para casos comuns (padrão Flyweight)
     * Estas instâncias são reutilizadas para evitar a criação de novos objetos
     * sempre que um resultado de validação for necessário.
     * Isso economiza memória e melhora a performance em cenários onde
     * muitos resultados iguais são esperados.
     */
    private static final ValidationResult SUCCESS =
            new ValidationResult(true, "Validation successful");
    private static final ValidationResult INVALID_AMOUNT =
            new ValidationResult(false, "Invalid amount");
    private static final ValidationResult INSUFFICIENT_FUNDS =
            new ValidationResult(false, "Insufficient funds");
    private static final ValidationResult EXPIRED_CARD =
            new ValidationResult(false, "Card expired");
    private static final ValidationResult INVALID_CARD =
            new ValidationResult(false, "Invalid card number");

    private final boolean valid; // Indica se a validação foi bem-sucedida
    private final String message; // Mensagem associada ao resultado da validação

    /**
     * Construtor privado para forçar o uso dos métodos estáticos.
     * Este construtor é privado para evitar a criação de instâncias
     * diretamente, garantindo que o cliente utilize os métodos estáticos.
     *
     * @param valid indica se a validação foi bem-sucedida
     * @param message mensagem associada ao resultado da validação
     */
    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = Objects.requireNonNull(message, "Message cannot be null");
    }

    /**
     * Retorna uma instância de sucesso.
     * Sempre retorna a mesma instância (singleton para este caso).
     * @return instância de ValidationResult indicando sucesso
     */
    public static ValidationResult success() {
        return SUCCESS; // Reutiliza a instância
    }

    /**
     * Retorna uma instância para valor inválido.
     * Sempre retorna a mesma instância.
     * @return instância de ValidationResult para valor inválido
     */
    public static ValidationResult invalidAmount() {
        return INVALID_AMOUNT; // Reutiliza a instância
    }

    /**
     * Retorna uma instância para fundos insuficientes.
     * Sempre retorna a mesma instância.
     * @return instância de ValidationResult para fundos insuficientes
     */
    public static ValidationResult insufficientFunds() {
        return INSUFFICIENT_FUNDS; // Reutiliza a instância
    }

    /**
     * Retorna uma instância para cartão expirado.
     * Sempre retorna a mesma instância.
     * @return instância de ValidationResult para cartão expirado
     */
    public static ValidationResult expiredCard() {
        return EXPIRED_CARD; // Reutiliza a instância
    }

    /**
     * Retorna uma instância para cartão inválido.
     * Sempre retorna a mesma instância.
     * @return instância de ValidationResult para cartão inválido
     */
    public static ValidationResult invalidCard() {
        return INVALID_CARD; // Reutiliza a instância
    }

    /**
     * Cria uma instância customizada para casos específicos.
     * Este método cria uma nova instância apenas quando necessário.
     * @param valid se a validação foi bem-sucedida
     * @param message mensagem personalizada
     * @return nova instância de ValidationResult
     */
    public static ValidationResult custom(boolean valid, String message) {
        // Para casos customizados, criamos uma nova instância
        return new ValidationResult(valid, message);
    }

    /**
     * Método de conveniência que retorna sucesso ou falha baseado em condição.
     * Demonstra como static factories podem encapsular lógica de criação.
     * @param condition condição a ser avaliada
     * @param failureMessage mensagem em caso de falha
     * @return ValidationResult apropriado
     */
    public static ValidationResult fromCondition(boolean condition, String failureMessage) {
        return condition ? SUCCESS : custom(false, failureMessage);
    }


    /**
     * Verifica se a validação foi bem-sucedida.
     * @return true se a validação foi bem-sucedida, false caso contrário
     */
    public boolean isValid() { return valid; }

    /**
     * Retorna a mensagem associada ao resultado da validação.
     * @return mensagem de validação
     */
    public String getMessage() { return message; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValidationResult that = (ValidationResult) obj;
        return valid == that.valid && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, message);
    }

    @Override
    public String toString() {
        return String.format("ValidationResult{valid=%s, message='%s'}", valid, message);
    }
}