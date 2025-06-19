package br.com.bianeck.javaefetivo.capitulo02.item01.variation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

/**
 * Demonstra a quarta vantagem dos métodos static factory: a classe do objeto
 * retornado pode variar de chamada para chamada.
 *
 * Similar ao EnumSet do Java, esta classe retorna diferentes implementações
 * baseadas no número de transações, otimizando performance e uso de memória.
 *
 * @author Thiago Bianeck
 */
public abstract class TransactionSet implements Iterable<String> {

    /**
     * Limite que determina qual implementação usar.
     * Transações <= 64: SmallTransactionSet (otimizado para poucos elementos)
     * Transações > 64: LargeTransactionSet (otimizado para muitos elementos)
     */
    private static final int SMALL_SET_THRESHOLD = 64;

    /**
     * Método static factory que retorna a implementação apropriada
     * baseada no número de transações esperadas.
     *
     * O cliente não sabe qual implementação específica está recebendo,
     * apenas que é um TransactionSet.
     *
     * @param expectedSize número esperado de transações
     * @return implementação otimizada para o tamanho
     */
    public static TransactionSet withCapacity(int expectedSize) {
        if (expectedSize <= 0) {
            throw new IllegalArgumentException("Expected size must be positive");
        }

        // Retorna implementação otimizada baseada no tamanho
        if (expectedSize <= SMALL_SET_THRESHOLD) {
            return new SmallTransactionSet();
        } else {
            return new LargeTransactionSet(expectedSize);
        }
    }

    /**
     * Método static factory para conjunto vazio.
     * Retorna sempre a implementação pequena para começar.
     *
     * @return conjunto vazio de transações
     */
    public static TransactionSet empty() {
        return new SmallTransactionSet();
    }

    /**
     * Método static factory que cria um conjunto a partir de transações existentes.
     * Escolhe automaticamente a implementação baseada no número de elementos.
     *
     * @param transactions transações iniciais
     * @return conjunto otimizado contendo as transações
     */
    public static TransactionSet of(Collection<String> transactions) {
        if (transactions == null) {
            throw new IllegalArgumentException("Transactions cannot be null");
        }

        TransactionSet set = withCapacity(transactions.size());
        for (String transaction : transactions) {
            set.add(transaction);
        }
        return set;
    }

    /**
     * Método static factory para criar conjunto com transações específicas.
     *
     * @param transactions transações a serem incluídas
     * @return conjunto contendo as transações
     */
    public static TransactionSet of(String... transactions) {
        if (transactions == null) {
            throw new IllegalArgumentException("Transactions cannot be null");
        }

        TransactionSet set = withCapacity(transactions.length);
        for (String transaction : transactions) {
            set.add(transaction);
        }
        return set;
    }

    /**
    * Adiciona uma transação ao conjunto.
    * Retorna true se a transação foi adicionada com sucesso,
    * false se já existia.
    *
    * @param transactionId ID da transação a ser adicionada
    * @return true se a transação foi adicionada, false se já existia
    */
    public abstract boolean add(String transactionId);

    /**
     * Remove uma transação do conjunto.
     * Retorna true se a transação foi removida com sucesso,
     * false se não existia.
     *
     * @param transactionId ID da transação a ser removida
     * @return true se a transação foi removida, false se não existia
     */
    public abstract boolean remove(String transactionId);

    /**
     * Verifica se uma transação existe no conjunto.
     *
     * @param transactionId ID da transação a ser verificada
     * @return true se a transação existir, false caso contrário
     */
    public abstract boolean contains(String transactionId);

    /**
     * Retorna um iterador para percorrer as transações.
     *
     * @return um iterador sobre as transações
     */
    public abstract int size();

    /**
     * Verifica se o conjunto está vazio.
     *
     * @return true se não houver transações, false caso contrário
     */
    public abstract boolean isEmpty();

    /**
     * Limpa todas as transações do conjunto.
     * Após este método, o conjunto deve estar vazio.
     */
    public abstract void clear();

    /**
     * Retorna o total de transações no conjunto.
     * Este método pode ser usado para calcular o valor total
     * ou outras métricas relacionadas às transações.
     *
     * @return o valor total das transações
     */
    public abstract BigDecimal getTotalAmount();

    /**
     * Retorna o tipo de implementação utilizada.
     * Isso pode ser útil para depuração ou otimização.
     *
     * @return uma string representando o tipo de implementação
     */
    public abstract String getImplementationType();

    /**
     * Adiciona todas as transações de uma coleção ao conjunto.
     * Retorna true se pelo menos uma transação foi adicionada,
     * false se todas já existiam.
     *
     * @param transactions coleção de transações a serem adicionadas
     * @return true
     * se pelo menos uma transação foi adicionada, false caso contrário
     */
    public boolean addAll(Collection<String> transactions) {
        boolean modified = false;
        for (String transaction : transactions) {
            if (add(transaction)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getImplementationType()).append("{size=").append(size()).append(", transactions=[");

        Iterator<String> it = iterator();
        boolean first = true;
        while (it.hasNext() && sb.length() < 200) { // Limita o tamanho da string
            if (!first) sb.append(", ");
            sb.append(it.next());
            first = false;
        }

        if (it.hasNext()) {
            sb.append(", ...");
        }

        sb.append("]}");
        return sb.toString();
    }
}