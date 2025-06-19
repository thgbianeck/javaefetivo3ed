package br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Registry que implementa o padrão Service Provider Framework.
 *
 * Esta classe demonstra como métodos static factory podem formar a base
 * de um service provider framework, permitindo que implementações sejam
 * registradas dinamicamente e acessadas através de uma API unificada.
 *
 * @author Thiago Bianeck
 */
public final class PaymentServiceRegistry {

    // Mapa thread-safe para armazenar provedores registrados
    private static final ConcurrentMap<String, PaymentServiceProvider> providers =
            new ConcurrentHashMap<>();

    // Provedor padrão
    private static volatile PaymentServiceProvider defaultProvider;

    /**
     * Construtor privado para classe utilitária.
     */
    private PaymentServiceRegistry() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * API de registro de provedores.
     *
     * Permite que provedores se registrem no sistema dinamicamente.
     *
     * @param providerName nome único do provedor
     * @param provider implementação do provedor
     * @throws IllegalArgumentException se o provedor já estiver registrado
     */
    public static void registerProvider(String providerName, PaymentServiceProvider provider) {
        Objects.requireNonNull(providerName, "Provider name cannot be null");
        Objects.requireNonNull(provider, "Provider cannot be null");

        PaymentServiceProvider existing = providers.putIfAbsent(providerName, provider);
        if (existing != null) {
            throw new IllegalArgumentException("Provider already registered: " + providerName);
        }
    }

    /**
     * Remove um provedor do registro.
     *
     * @param providerName nome do provedor a ser removido
     * @return true se o provedor foi removido
     */
    public static boolean unregisterProvider(String providerName) {
        return providers.remove(providerName) != null;
    }

    /**
     * Define o provedor padrão.
     *
     * @param providerName nome do provedor padrão
     * @throws IllegalArgumentException se o provedor não estiver registrado
     */
    public static void setDefaultProvider(String providerName) {
        PaymentServiceProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new IllegalArgumentException("Provider not registered: " + providerName);
        }
        defaultProvider = provider;
    }

    /**
     * API de acesso ao serviço - método static factory principal.
     *
     * Retorna uma instância de PaymentService usando o provedor padrão.
     * Esta é a "service access API" do padrão.
     *
     * @return instância de PaymentService
     * @throws IllegalStateException se nenhum provedor padrão estiver definido
     */
    public static PaymentService newInstance() {
        PaymentServiceProvider provider = defaultProvider;
        if (provider == null) {
            throw new IllegalStateException("No default provider set");
        }
        return provider.newService();
    }

    /**
     * API de acesso ao serviço com provedor específico.
     *
     * Permite ao cliente especificar qual provedor usar.
     *
     * @param providerName nome do provedor desejado
     * @return instância de PaymentService
     * @throws IllegalArgumentException se o provedor não estiver registrado
     */
    public static PaymentService newInstance(String providerName) {
        PaymentServiceProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new IllegalArgumentException("Provider not registered: " + providerName);
        }
        return provider.newService();
    }

    /**
     * API de acesso ao serviço otimizada para tipo de pagamento.
     *
     * Retorna o melhor provedor para um tipo específico de pagamento,
     * baseado na prioridade e suporte ao tipo.
     *
     * @param paymentType tipo de pagamento
     * @return instância de PaymentService otimizada
     * @throws IllegalArgumentException se nenhum provedor suportar o tipo
     */
    public static PaymentService newInstanceForPaymentType(String paymentType) {
        Objects.requireNonNull(paymentType, "Payment type cannot be null");

        List<PaymentServiceProvider> supportedProviders = new ArrayList<>();

        // Encontra provedores que suportam o tipo de pagamento
        for (PaymentServiceProvider provider : providers.values()) {
            String[] supportedTypes = provider.getSupportedPaymentTypes();
            if (Arrays.asList(supportedTypes).contains(paymentType.toUpperCase())) {
                supportedProviders.add(provider);
            }
        }

        if (supportedProviders.isEmpty()) {
            throw new IllegalArgumentException("No provider supports payment type: " + paymentType);
        }

        // Ordena por prioridade e retorna o melhor
        PaymentServiceProvider bestProvider = supportedProviders.stream()
                .min(Comparator.comparingInt(p -> p.newService().getPriority()))
                .orElse(supportedProviders.get(0));

        return bestProvider.newService();
    }

    /**
     * Retorna todos os provedores disponíveis.
     *
     * @return conjunto com nomes dos provedores registrados
     */
    public static Set<String> getAvailableProviders() {
        return new HashSet<>(providers.keySet());
    }

    /**
     * Verifica se um provedor está registrado.
     *
     * @param providerName nome do provedor
     * @return true se estiver registrado
     */
    public static boolean isProviderRegistered(String providerName) {
        return providers.containsKey(providerName);
    }

    /**
     * Retorna informações sobre todos os provedores registrados.
     *
     * @return mapa com informações dos provedores
     */
    public static Map<String, String[]> getProviderInfo() {
        Map<String, String[]> info = new HashMap<>();
        providers.forEach((name, provider) ->
                info.put(name, provider.getSupportedPaymentTypes()));
        return info;
    }

    /**
     * Limpa todos os provedores registrados.
     * Útil para testes.
     */
    public static void clearProviders() {
        providers.clear();
        defaultProvider = null;
    }
}