# Item 1: Static Factory Methods - Java Efetivo 3ª Edição

Este projeto demonstra todas as vantagens dos métodos static factory apresentadas no Item 1 do livro "Java Efetivo" de Joshua Bloch, através de um exemplo prático de sistema de processamento de pagamentos empresarial.

## 📋 Visão Geral

O projeto implementa um sistema completo de pagamentos que exemplifica as cinco principais vantagens dos métodos static factory:

1. **Nomes Descritivos** - Métodos com nomes claros que indicam exatamente o que fazem
2. **Controle de Instâncias** - Reutilização de objetos e implementação de cache
3. **Flexibilidade de Tipos** - Retorno de diferentes subtipos baseado em parâmetros
4. **Variação de Classes** - Diferentes implementações baseadas em critérios de entrada
5. **Service Provider Framework** - Sistema flexível de provedores de serviços

## 🏗️ Arquitetura do Projeto

```text
src/main/java/com/javaefetico/item01/
├── basic/                  # Demonstra vantagem dos nomes descritivos
│   ├── PaymentMethod.java
│   └── PaymentProcessor.java
├── cache/                  # Demonstra controle de instâncias e cache
│   ├── ValidationResult.java
│   └── CachedPaymentValidator.java
├── flexibility/            # Demonstra flexibilidade de tipos de retorno
│   ├── PaymentGateway.java
│   ├── CreditCardGateway.java
│   ├── PixGateway.java
│   └── BoletoGateway.java
├── variation/              # Demonstra variação de classes retornadas
│   ├── TransactionSet.java
│   ├── SmallTransactionSet.java
│   └── LargeTransactionSet.java
├── serviceloader/          # Demonstra Service Provider Framework
│   ├── PaymentService.java
│   ├── PaymentServiceProvider.java
│   ├── PaymentServiceRegistry.java
│   └── impl/
│       ├── CreditCardPaymentService.java
│       └── PixPaymentService.java
└── examples/
    └── PaymentSystemDemo.java
```

## 🚀 Como Executar

### Pré-requisitos
- Java 11 ou superior
- Gradle 7.0 ou superior

### Executando a Demonstração
```bash
# Compilar o projeto
./gradlew build

# Executar a demonstração principal
./gradlew run --main-class="com.javaefetico.item01.examples.PaymentSystemDemo"

# Executar testes
./gradlew test

# Gerar documentação
./gradlew javadoc
```

## 📚 Exemplos de Uso

### 1. Vantagem dos Nomes Descritivos

```java
// Em vez de construtores confusos
PaymentMethod creditCard = PaymentMethod.creditCard("4111-1111-1111-1111", new BigDecimal("5000.00"));
PaymentMethod pix = PaymentMethod.pix("usuario@email.com");
PaymentMethod boleto = PaymentMethod.bankSlip("12345...");

// Processadores com comportamentos claros
PaymentProcessor immediate = PaymentProcessor.processImmediatePayment(creditCard, amount, merchantId);
PaymentProcessor scheduled = PaymentProcessor.schedulePayment(pix, amount, merchantId);
PaymentProcessor recurring = PaymentProcessor.setupRecurringPayment(boleto, amount, merchantId);
```

### 2. Controle de Instâncias e Cache

```java
// Instâncias reutilizadas (padrão Flyweight)
ValidationResult success1 = ValidationResult.success();
ValidationResult success2 = ValidationResult.success();
assert success1 == success2; // Mesma instância!

// Cache transparente para melhorar performance
ValidationResult result1 = CachedPaymentValidator.validatePayment(paymentMethod, amount);
ValidationResult result2 = CachedPaymentValidator.validatePayment(paymentMethod, amount);
assert result1 == result2; // Resultado do cache
```

### 3. Flexibilidade de Tipos de Retorno

```java
// Cliente não sabe qual implementação específica está recebendo
PaymentGateway gateway1 = PaymentGateway.forPaymentType("CREDIT_CARD"); // Retorna CreditCardGateway
PaymentGateway gateway2 = PaymentGateway.forPaymentType("PIX");         // Retorna PixGateway
PaymentGateway gateway3 = PaymentGateway.withLowestFee();               // Retorna o com menor taxa
PaymentGateway gateway4 = PaymentGateway.optimizedForAmount(amount);    // Retorna o otimizado para o valor
```

### 4. Variação de Classes Baseada em Parâmetros

```java
// Diferentes implementações baseadas no tamanho esperado
TransactionSet small = TransactionSet.withCapacity(10);   // Retorna SmallTransactionSet
TransactionSet large = TransactionSet.withCapacity(100);  // Retorna LargeTransactionSet

// Cliente não sabe qual implementação está usando
System.out.println(small.getImplementationType()); // "SmallTransactionSet"
System.out.println(large.getImplementationType()); // "LargeTransactionSet"
```

### 5. Service Provider Framework

```java
// Registrar provedores
PaymentServiceRegistry.registerProvider("CreditCard", new CreditCardPaymentService.Provider());
PaymentServiceRegistry.registerProvider("PIX", new PixPaymentService.Provider());

// Usar serviços de diferentes formas
PaymentService defaultService = PaymentServiceRegistry.newInstance();
PaymentService specificService = PaymentServiceRegistry.newInstance("CreditCard");
PaymentService optimizedService = PaymentServiceRegistry.newInstanceForPaymentType("PIX");
```

## 🧪 Testes

O projeto inclui testes abrangentes que verificam:

- ✅ Funcionamento correto de todos os métodos static factory
- ✅ Reutilização de instâncias e cache
- ✅ Seleção correta de implementações
- ✅ Comportamento do service provider framework
- ✅ Cenários de integração completos
- ✅ Tratamento de erros e casos extremos

Execute os testes com:
```bash
./gradlew test --info
```

## 📖 Conceitos Demonstrados

### Padrões de Design Utilizados
- **Flyweight**: Reutilização de instâncias em `ValidationResult`
- **Strategy**: Diferentes implementações de `PaymentGateway`
- **Service Provider**: Framework flexível de provedores
- **Factory Method**: Métodos static factory em todas as classes

### Convenções de Nomenclatura
- `from` - Conversão de tipo com um parâmetro
- `of` - Agregação com múltiplos parâmetros
- `valueOf` - Alternativa verbosa para `from` e `of`
- `getInstance` - Retorna instância (pode ser reutilizada)
- `newInstance` - Garante nova instância
- `create` - Alias para `newInstance`

## 🎯 Benefícios Demonstrados

1. **Clareza de Código**: Nomes descritivos tornam o código auto-documentado
2. **Performance**: Cache e reutilização de instâncias melhoram a performance
3. **Flexibilidade**: Diferentes implementações podem ser retornadas transparentemente
4. **Manutenibilidade**: Mudanças internas não afetam o código cliente
5. **Extensibilidade**: Novos provedores podem ser adicionados facilmente

## 📝 Limitações Abordadas

O projeto também demonstra as limitações dos métodos static factory:

1. **Herança**: Classes sem construtores públicos não podem ser estendidas
2. **Descoberta**: Métodos static factory são menos óbvios na documentação

### Soluções Implementadas
- Documentação rica com Javadoc
- Convenções de nomenclatura consistentes
- Exemplos de uso em todas as classes

## 🔗 Referências

- **Java Efetivo, 3ª Edição** - Joshua Bloch, Item 1
- **Design Patterns** - Gang of Four
- **Oracle Java Documentation**

## 👨‍💻 Autor

**Thiago Bianeck**
- Engenheiro de Software
- Francisco Beltrão, PR
- Especialista em Java e arquitetura de software

---

Este projeto serve como referência prática para implementação dos conceitos apresentados no Item 1 do Java Efetivo, demonstrando como aplicar métodos static factory em cenários reais de desenvolvimento empresarial.