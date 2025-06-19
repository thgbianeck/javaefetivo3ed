package br.com.bianeck.javaefetivo.capitulo02.item01.basic;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Demonstra a primeira vantagem dos métodos static factory: ter nomes descritivos.
 * Esta classe representa um método de pagamento no sistema empresarial.
 * Em vez de usar construtores com parâmetros confusos, utilizamos métodos
 * static factory com nomes claros que indicam exatamente o que está sendo criado.
 *
 * @author Thiago Bianeck
 * @version 1.0
 * @since 1.0
 */
public final class PaymentMethod {

    private final String type; // Tipos de métodos de pagamento
    private final String identifier; // Identificador único do método de pagamento, como número do cartão ou chave PIX
    private final BigDecimal limit; // Limite de transação para o método de pagamento
    private final boolean requiresAuthentication; // Indica se o método de pagamento requer autenticação adicional

    /**
     * Construtor privado para forçar o uso dos métodos static factory.
     * @param type o tipo do método de pagamento
     * @param identifier o identificador único
     * @param limit o limite de transação
     * @param requiresAuthentication se requer autenticação
     */
    private PaymentMethod(
            String type,
            String identifier,
            BigDecimal limit,
            boolean requiresAuthentication) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.identifier = Objects.requireNonNull(identifier, "Identifier cannot be null");
        this.limit = Objects.requireNonNull(limit, "Limit cannot be null");
        this.requiresAuthentication = requiresAuthentication;
    }

    /**
     * Cria um método de pagamento para cartão de crédito.
     * Exemplo de uso:
     * <pre>
     * PaymentMethod creditCard = PaymentMethod.creditCard("4111-1111-1111-1111", new BigDecimal("5000.00"));
     * </pre>
     *
     * @param cardNumber o número do cartão
     * @param limit o limite do cartão
     * @return uma instância de PaymentMethod configurada para cartão de crédito
     * @throws IllegalArgumentException se o número do cartão for inválido
     */
    public static PaymentMethod creditCard(String cardNumber, BigDecimal limit) {
        validateCreditCardNumber(cardNumber);
        return new PaymentMethod("CREDIT_CARD", cardNumber, limit, true);
    }

    /**
     * Cria um método de pagamento para PIX.
     * Exemplo de uso:
     * <pre>
     * PaymentMethod pix = PaymentMethod.pix("usuario@email.com");
     * </pre>
     *
     * @param pixKey a chave PIX
     * @return uma instância de PaymentMethod configurada para PIX
     */
    public static PaymentMethod pix(String pixKey) {
        validatePixKey(pixKey);
        return new PaymentMethod("PIX", pixKey, new BigDecimal("50000.00"), false);
    }

    /**
     * Cria um método de pagamento para boleto bancário.
     * Exemplo de uso:
     * <pre>
     * PaymentMethod boleto = PaymentMethod.bankSlip("12345678901234567890123456789012345678901234567890");
     * </pre>
     *
     * @param barcode o código de barras do boleto
     * @return uma instância de PaymentMethod configurada para boleto
     */
    public static PaymentMethod bankSlip(String barcode) {
        validateBankSlipBarcode(barcode);
        return new PaymentMethod("BANK_SLIP", barcode, new BigDecimal("10000.00"), false);
    }

    /**
     * Cria um método de pagamento para cartão de débito.
     * Demonstra como métodos static factory permitem múltiplos "construtores"
     * com a mesma assinatura de parâmetros, mas comportamentos diferentes.
     *
     * @param cardNumber o número do cartão
     * @param limit o limite do cartão
     * @return uma instância de PaymentMethod configurada para cartão de débito
     */
    public static PaymentMethod debitCard(String cardNumber, BigDecimal limit) {
        validateCreditCardNumber(cardNumber); // Mesma validação do cartão de crédito
        return new PaymentMethod("DEBIT_CARD", cardNumber, limit, true);
    }

    /**
     * Valida o número do cartão de crédito.
     *
     * Esta validação é simplificada e não usa o algoritmo de Luhn.
     * Em produção, deve-se usar uma biblioteca confiável para validação de cartões.
     *
     * @param cardNumber o número do cartão
     * @throws IllegalArgumentException se o número for inválido
     */
    private static void validateCreditCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number cannot be null or empty");
        }
        // Validação simplificada - em produção usaria algoritmo de Luhn
        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        if (cleanNumber.length() < 13 || cleanNumber.length() > 19) {
            throw new IllegalArgumentException("Invalid card number length");
        }
    }

    /**
     * Valida a chave PIX.
     *
     * A chave pode ser um e-mail, CPF, CNPJ ou número de telefone.
     *
     * @param pixKey a chave PIX
     * @throws IllegalArgumentException se a chave for inválida
     */
    private static void validatePixKey(String pixKey) {
        if (pixKey == null || pixKey.trim().isEmpty()) {
            throw new IllegalArgumentException("PIX key cannot be null or empty");
        }
        // Validação simplificada
        if (!pixKey.contains("@") && !pixKey.matches("\\d{11}") && !pixKey.matches("\\d{14}")) {
            throw new IllegalArgumentException("Invalid PIX key format");
        }
    }

    /**
     * Valida o código de barras do boleto bancário.
     *
     * O código deve ter exatamente 47 dígitos e conter apenas números.
     *
     * @param barcode o código de barras do boleto
     * @throws IllegalArgumentException se o código for inválido
     */
    private static void validateBankSlipBarcode(String barcode) {

        if (barcode == null || barcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Bank slip barcode cannot be null or empty");
        }
        if (barcode.length() != 47) {
            throw new IllegalArgumentException("Bank slip barcode must have exactly 47 digits");
        }
        if (!barcode.matches("\\d{47}")) {
            throw new IllegalArgumentException("Bank slip barcode must contain only digits");
        }
    }

    // Getters
    public String getType() { return type; }
    public String getIdentifier() { return identifier; }
    public BigDecimal getLimit() { return limit; }
    public boolean requiresAuthentication() { return requiresAuthentication; }

    /**
     * Mascara o identificador para exibição.
     * @return O identificador mascarado, mantendo os 4 primeiros e últimos dígitos visíveis.
     */
    private String maskIdentifier() {
        if (identifier.length() <= 4) return identifier;
        return identifier.substring(0, 4) + "****" + identifier.substring(identifier.length() - 4);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PaymentMethod that = (PaymentMethod) obj;
        return requiresAuthentication == that.requiresAuthentication &&
                Objects.equals(type, that.type) &&
                Objects.equals(identifier, that.identifier) &&
                Objects.equals(limit, that.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, identifier, limit, requiresAuthentication);
    }

    @Override
    public String toString() {
        return String.format("PaymentMethod{type='%s', identifier='%s', limit=%s, requiresAuth=%s}",
                type, maskIdentifier(), limit, requiresAuthentication);
    }
}
