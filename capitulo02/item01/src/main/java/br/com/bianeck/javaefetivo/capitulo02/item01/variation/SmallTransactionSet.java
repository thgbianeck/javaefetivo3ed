package br.com.bianeck.javaefetivo.capitulo02.item01.variation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementação otimizada para pequenos conjuntos de transações (≤ 64 elementos).
 *
 * Usa uma lista simples para armazenamento, que é mais eficiente em memória
 * e performance para poucos elementos.
 *
 * Esta classe não é pública, demonstrando como static factories podem
 * ocultar implementações específicas.
 *
 * @author Thiago Bianeck
 */
class SmallTransactionSet extends TransactionSet {

    private final List<String> transactions;
    private BigDecimal totalAmount;

    /**
     * Construtor package-private para ser usado apenas pelos static factories.
     */
    SmallTransactionSet() {
        this.transactions = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
    }

    @Override
    public boolean add(String transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }

        // Verifica se já existe (operação O(n) mas eficiente para poucos elementos)
        if (transactions.contains(transactionId)) {
            return false;
        }

        transactions.add(transactionId);

        // Simula extração do valor da transação do ID (em produção seria um lookup)
        BigDecimal amount = extractAmountFromId(transactionId);
        totalAmount = totalAmount.add(amount);

        return true;
    }

    @Override
    public boolean remove(String transactionId) {
        if (transactionId == null) {
            return false;
        }

        boolean removed = transactions.remove(transactionId);
        if (removed) {
            BigDecimal amount = extractAmountFromId(transactionId);
            totalAmount = totalAmount.subtract(amount);
        }

        return removed;
    }

    @Override
    public boolean contains(String transactionId) {
        return transactionId != null && transactions.contains(transactionId);
    }

    @Override
    public int size() {
        return transactions.size();
    }

    @Override
    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    @Override
    public void clear() {
        transactions.clear();
        totalAmount = BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String getImplementationType() {
        return "SmallTransactionSet";
    }

    @Override
    public Iterator<String> iterator() {
        // Retorna iterator da lista (não thread-safe, mas eficiente)
        return transactions.iterator();
    }

    /**
     * Simula extração do valor da transação a partir do ID.
     * Em um sistema real, isso seria um lookup em banco de dados.
     */
    private BigDecimal extractAmountFromId(String transactionId) {
        // Simula valor baseado no hash do ID
        int hash = Math.abs(transactionId.hashCode());
        return new BigDecimal(hash % 10000 + 1); // Valores entre 1 e 10000
    }
}