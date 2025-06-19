package br.com.bianeck.javaefetivo.capitulo02.item01.variation;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Implementação otimizada para grandes conjuntos de transações (> 64 elementos).
 *
 * Usa um HashSet para armazenamento, que oferece operações O(1) para
 * adicionar, remover e verificar existência.
 *
 * @author Thiago Bianeck
 */
class LargeTransactionSet extends TransactionSet {

    private final Set<String> transactions;
    private BigDecimal totalAmount;

    /**
     * Construtor package-private com capacidade inicial.
     */
    LargeTransactionSet(int initialCapacity) {
        // Usa HashSet com capacidade inicial para evitar redimensionamentos
        this.transactions = new HashSet<>(initialCapacity);
        this.totalAmount = BigDecimal.ZERO;
    }

    @Override
    public boolean add(String transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }

        // HashSet.add() retorna false se o elemento já existir
        boolean added = transactions.add(transactionId);

        if (added) {
            BigDecimal amount = extractAmountFromId(transactionId);
            totalAmount = totalAmount.add(amount);
        }

        return added;
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
        return "LargeTransactionSet";
    }

    @Override
    public Iterator<String> iterator() {
        return transactions.iterator();
    }

    /**
     * Sobrescreve addAll para melhor performance com HashSet.
     */
    @Override
    public boolean addAll(java.util.Collection<String> transactions) {
        boolean modified = false;

        // Otimização: redimensiona o HashSet se necessário
        if (this.transactions instanceof HashSet) {
            int newSize = this.transactions.size() + transactions.size();
            // HashSet se redimensiona automaticamente, mas podemos dar uma dica
        }

        for (String transaction : transactions) {
            if (add(transaction)) {
                modified = true;
            }
        }

        return modified;
    }

    /**
     * Simula extração do valor da transação a partir do ID.
     */
    private BigDecimal extractAmountFromId(String transactionId) {
        int hash = Math.abs(transactionId.hashCode());
        return new BigDecimal(hash % 10000 + 1);
    }
}