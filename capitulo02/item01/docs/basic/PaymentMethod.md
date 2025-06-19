# Análise Detalhada da Classe PaymentMethod

Esta classe é um excelente exemplo de implementação do **padrão Static Factory Method**, demonstrando uma das principais vantagens desse padrão: **métodos com nomes descritivos**. Vou explicar cada aspecto da implementação:

## 📋 Estrutura Geral da Classe

```java
public final class PaymentMethod {
    // Campos imutáveis
    private final String type;
    private final String identifier;
    private final BigDecimal limit;
    private final boolean requiresAuthentication;
}
```

### Características Principais:
- **Classe final**: Não pode ser estendida, garantindo integridade do design
- **Campos imutáveis**: Todos os atributos são `final`, tornando a classe thread-safe
- **Encapsulamento**: Construtor privado força o uso dos métodos factory

## 🏗️ Construtor Privado

```java
private PaymentMethod(String type, String identifier, BigDecimal limit, boolean requiresAuthentication) {
    this.type = Objects.requireNonNull(type, "Type cannot be null");
    this.identifier = Objects.requireNonNull(identifier, "Identifier cannot be null");
    this.limit = Objects.requireNonNull(limit, "Limit cannot be null");
    this.requiresAuthentication = requiresAuthentication;
}
```

### Vantagens do Construtor Privado:
- **Controle de criação**: Força o uso dos métodos factory
- **Validação centralizada**: Usa `Objects.requireNonNull()` para validação
- **Mensagens de erro claras**: Cada parâmetro tem sua mensagem específica

## 🏭 Métodos Static Factory

### 1. Cartão de Crédito
```java
public static PaymentMethod creditCard(String cardNumber, BigDecimal limit) {
    validateCreditCardNumber(cardNumber);
    return new PaymentMethod("CREDIT_CARD", cardNumber, limit, true);
}
```

### 2. PIX
```java
public static PaymentMethod pix(String pixKey) {
    validatePixKey(pixKey);
    return new PaymentMethod("PIX", pixKey, new BigDecimal("50000.00"), false);
}
```

### 3. Boleto Bancário
```java
public static PaymentMethod bankSlip(String barcode) {
    validateBankSlipBarcode(barcode);
    return new PaymentMethod("BANK_SLIP", barcode, new BigDecimal("10000.00"), false);
}
```

### 4. Cartão de Débito
```java
public static PaymentMethod debitCard(String cardNumber, BigDecimal limit) {
    validateCreditCardNumber(cardNumber);
    return new PaymentMethod("DEBIT_CARD", cardNumber, limit, true);
}
```

## ✅ Vantagens dos Static Factory Methods

### 1. **Nomes Descritivos**
- `PaymentMethod.creditCard()` é mais claro que `new PaymentMethod("CREDIT_CARD", ...)`
- `PaymentMethod.pix()` deixa explícito o tipo de pagamento sendo criado

### 2. **Múltiplos "Construtores" com Mesma Assinatura**
- `creditCard()` e `debitCard()` ambos recebem `(String, BigDecimal)`
- Impossível com construtores tradicionais

### 3. **Configuração Pré-definida**
- PIX automaticamente define limite de R$ 50.000,00
- Boleto automaticamente define limite de R$ 10.000,00

## 🔍 Métodos de Validação

### Validação de Cartão de Crédito/Débito
```java
private static void validateCreditCardNumber(String cardNumber) {
    if (cardNumber == null || cardNumber.trim().isEmpty()) {
        throw new IllegalArgumentException("Card number cannot be null or empty");
    }
    String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
    if (cleanNumber.length() < 13 || cleanNumber.length() > 19) {
        throw new IllegalArgumentException("Invalid card number length");
    }
}
```

### Validação de Chave PIX
```java
private static void validatePixKey(String pixKey) {
    if (pixKey == null || pixKey.trim().isEmpty()) {
        throw new IllegalArgumentException("PIX key cannot be null or empty");
    }
    // Aceita email, CPF (11 dígitos) ou CNPJ (14 dígitos)
    if (!pixKey.contains("@") && !pixKey.matches("\d{11}") && !pixKey.matches("\d{14}")) {
        throw new IllegalArgumentException("Invalid PIX key format");
    }
}
```

### Validação de Código de Barras
```java
private static void validateBankSlipBarcode(String barcode) {
    if (barcode == null || barcode.length() != 47) {
        throw new IllegalArgumentException("Bank slip barcode must have exactly 47 digits");
    }
    if (!barcode.matches("\d{47}")) {
        throw new IllegalArgumentException("Bank slip barcode must contain only digits");
    }
}
```

## 📊 Métodos de Acesso e Utilitários

### Getters Simples
```java
public String getType() { return type; }
public String getIdentifier() { return identifier; }
public BigDecimal getLimit() { return limit; }
public boolean requiresAuthentication() { return requiresAuthentication; }
```

### Método toString() com Mascaramento
```java
@Override
public String toString() {
return String.format("PaymentMethod{type='%s', identifier='%s', limit=%s, requiresAuth=%s}",
type, maskIdentifier(), limit, requiresAuthentication);
}

private String maskIdentifier() {
if (identifier.length() <= 4) return identifier;
return identifier.substring(0, 4) + "****" + identifier.substring(identifier.length() - 4);
}
```

**Exemplo de saída**: `PaymentMethod{type='CREDIT_CARD', identifier='4111****1111', limit=5000.00, requiresAuth=true}`

## 🔧 Implementação de equals() e hashCode()

```java
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
```

## 💡 Exemplos de Uso

```java
// Criação de diferentes métodos de pagamento
PaymentMethod creditCard = PaymentMethod.creditCard("4111-1111-1111-1111", new BigDecimal("5000.00"));
PaymentMethod pix = PaymentMethod.pix("usuario@email.com");
PaymentMethod boleto = PaymentMethod.bankSlip("12345678901234567890123456789012345678901234567890");
PaymentMethod debitCard = PaymentMethod.debitCard("5555-5555-5555-4444", new BigDecimal("2000.00"));

// Verificação de propriedades
System.out.println(creditCard.getType()); // CREDIT_CARD
System.out.println(pix.requiresAuthentication()); // false
System.out.println(boleto.getLimit()); // 10000.00
```

## 🎯 Benefícios desta Implementação

### ✅ **Clareza e Legibilidade**
- Código autodocumentado através dos nomes dos métodos
- Intenção clara do desenvolvedor ao criar cada tipo de pagamento

### ✅ **Segurança**
- Validações específicas para cada tipo de pagamento
- Classe imutável previne modificações acidentais
- Mascaramento de dados sensíveis no toString()

### ✅ **Flexibilidade**
- Fácil adição de novos tipos de pagamento
- Configurações pré-definidas para cada tipo
- Reutilização de validações quando apropriado

### ✅ **Manutenibilidade**
- Separação clara de responsabilidades
- Métodos de validação isolados e testáveis
- Documentação JavaDoc completa

Esta implementação demonstra perfeitamente como o padrão Static Factory Method pode tornar o código mais expressivo, seguro e fácil de manter, sendo um excelente exemplo das boas práticas recomendadas no livro "Java Efetivo" de Joshua Bloch.