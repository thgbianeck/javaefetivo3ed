package br.com.bianeck.javaefetivo.capitulo02.item01.variation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Testes unitários para TransactionSet.
 *
 * Verifica se as diferentes implementações são retornadas baseadas
 * no tamanho esperado e se funcionam corretamente.
 *
 * @author Thiago Bianeck
 */
class TransactionSetTest {

    @Test
    @DisplayName("Deve retornar SmallTransactionSet para capacidade pequena")
    void shouldReturnSmallTransactionSetForSmallCapacity() {
        // When
        TransactionSet smallSet = TransactionSet.withCapacity(10);

        // Then
        assertNotNull(smallSet);
        assertEquals("SmallTransactionSet", smallSet.getImplementationType());
        assertTrue(smallSet.isEmpty());
        assertEquals(0, smallSet.size());
    }

    @Test
    @DisplayName("Deve retornar LargeTransactionSet para capacidade grande")
    void shouldReturnLargeTransactionSetForLargeCapacity() {
        // When
        TransactionSet largeSet = TransactionSet.withCapacity(100);

        // Then
        assertNotNull(largeSet);
        assertEquals("LargeTransactionSet", largeSet.getImplementationType());
        assertTrue(largeSet.isEmpty());
        assertEquals(0, largeSet.size());
    }

    @Test
    @DisplayName("Deve retornar SmallTransactionSet no limite")
    void shouldReturnSmallTransactionSetAtThreshold() {
        // When
        TransactionSet setAt64 = TransactionSet.withCapacity(64);
        TransactionSet setAt65 = TransactionSet.withCapacity(65);

        // Then
        assertEquals("SmallTransactionSet", setAt64.getImplementationType());
        assertEquals("LargeTransactionSet", setAt65.getImplementationType());
    }

    @Test
    @DisplayName("Deve falhar para capacidade inválida")
    void shouldFailForInvalidCapacity() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                TransactionSet.withCapacity(0));
        assertThrows(IllegalArgumentException.class, () ->
                TransactionSet.withCapacity(-1));
    }

    @Test
    @DisplayName("Deve criar conjunto vazio")
    void shouldCreateEmptySet() {
        // When
        TransactionSet emptySet = TransactionSet.empty();

        // Then
        assertNotNull(emptySet);
        assertTrue(emptySet.isEmpty());
        assertEquals(0, emptySet.size());
        assertEquals("SmallTransactionSet", emptySet.getImplementationType());
    }

    @Test
    @DisplayName("Deve criar conjunto a partir de coleção")
    void shouldCreateSetFromCollection() {
        // Given
        java.util.List<String> transactions = Arrays.asList("TX1", "TX2", "TX3");

        // When
        TransactionSet set = TransactionSet.of(transactions);

        // Then
        assertNotNull(set);
        assertEquals(3, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.contains("TX1"));
        assertTrue(set.contains("TX2"));
        assertTrue(set.contains("TX3"));
    }

    @Test
    @DisplayName("Deve criar conjunto a partir de varargs")
    void shouldCreateSetFromVarargs() {
        // When
        TransactionSet set = TransactionSet.of("TXA", "TXB", "TXC", "TXD");

        // Then
        assertNotNull(set);
        assertEquals(4, set.size());
        assertTrue(set.contains("TXA"));
        assertTrue(set.contains("TXB"));
        assertTrue(set.contains("TXC"));
        assertTrue(set.contains("TXD"));
    }

    @Test
    @DisplayName("Deve escolher implementação baseada no tamanho da coleção")
    void shouldChooseImplementationBasedOnCollectionSize() {
        // Given
        java.util.List<String> smallList = Arrays.asList("TX1", "TX2", "TX3");
        java.util.List<String> largeList = new java.util.ArrayList<>();
        for (int i = 1; i <= 70; i++) {
            largeList.add("TX" + i);
        }

        // When
        TransactionSet smallSet = TransactionSet.of(smallList);
        TransactionSet largeSet = TransactionSet.of(largeList);

        // Then
        assertEquals("SmallTransactionSet", smallSet.getImplementationType());
        assertEquals("LargeTransactionSet", largeSet.getImplementationType());
    }

    @Test
    @DisplayName("Deve adicionar transações corretamente")
    void shouldAddTransactionsCorrectly() {
        // Given
        TransactionSet set = TransactionSet.empty();

        // When
        boolean added1 = set.add("TX1");
        boolean added2 = set.add("TX2");
        boolean added3 = set.add("TX1"); // Duplicata

        // Then
        assertTrue(added1);
        assertTrue(added2);
        assertFalse(added3); // Não deve adicionar duplicata

        assertEquals(2, set.size());
        assertTrue(set.contains("TX1"));
        assertTrue(set.contains("TX2"));
    }

    @Test
    @DisplayName("Deve remover transações corretamente")
    void shouldRemoveTransactionsCorrectly() {
        // Given
        TransactionSet set = TransactionSet.of("TX1", "TX2", "TX3");

        // When
        boolean removed1 = set.remove("TX2");
        boolean removed2 = set.remove("TX4"); // Não existe

        // Then
        assertTrue(removed1);
        assertFalse(removed2);

        assertEquals(2, set.size());
        assertTrue(set.contains("TX1"));
        assertFalse(set.contains("TX2"));
        assertTrue(set.contains("TX3"));
    }

    @Test
    @DisplayName("Deve limpar conjunto corretamente")
    void shouldClearSetCorrectly() {
        // Given
        TransactionSet set = TransactionSet.of("TX1", "TX2", "TX3");

        // When
        set.clear();

        // Then
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        assertFalse(set.contains("TX1"));
    }

    @Test
    @DisplayName("Deve calcular valor total corretamente")
    void shouldCalculateTotalAmountCorrectly() {
        // Given
        TransactionSet set = TransactionSet.empty();

        // When
        set.add("TX1");
        set.add("TX2");

        // Then
        assertNotNull(set.getTotalAmount());
        assertTrue(set.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Deve iterar sobre transações")
    void shouldIterateOverTransactions() {
        // Given
        TransactionSet set = TransactionSet.of("TX1", "TX2", "TX3");

        // When
        Iterator<String> iterator = set.iterator();

        // Then
        assertNotNull(iterator);

        int count = 0;
        while (iterator.hasNext()) {
            String transaction = iterator.next();
            assertNotNull(transaction);
            assertTrue(transaction.startsWith("TX"));
            count++;
        }

        assertEquals(3, count);
    }

    @Test
    @DisplayName("Deve adicionar todas as transações de uma coleção")
    void shouldAddAllTransactionsFromCollection() {
        // Given
        TransactionSet set = TransactionSet.empty();
        java.util.List<String> transactions = Arrays.asList("TX1", "TX2", "TX3");

        // When
        boolean modified = set.addAll(transactions);

        // Then
        assertTrue(modified);
        assertEquals(3, set.size());
        assertTrue(set.contains("TX1"));
        assertTrue(set.contains("TX2"));
        assertTrue(set.contains("TX3"));
    }

    @Test
    @DisplayName("Deve ter toString informativo")
    void shouldHaveInformativeToString() {
        // Given
        TransactionSet set = TransactionSet.of("TX1", "TX2");

        // When
        String toString = set.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("TransactionSet"));
        assertTrue(toString.contains("size=2"));
        assertTrue(toString.contains("TX1"));
        assertTrue(toString.contains("TX2"));
    }

    @Test
    @DisplayName("Deve falhar ao adicionar transação nula")
    void shouldFailToAddNullTransaction() {
        // Given
        TransactionSet set = TransactionSet.empty();

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                set.add(null));
    }

    @Test
    @DisplayName("Deve falhar ao criar conjunto com coleção nula")
    void shouldFailToCreateSetWithNullCollection() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                TransactionSet.of((java.util.Collection<String>) null));

        assertThrows(IllegalArgumentException.class, () ->
                TransactionSet.of((String[]) null));
    }
}