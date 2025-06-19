package br.com.bianeck.javaefetivo.capitulo02.item01.serviceloader;

/**
 * Interface do provedor de serviços que cria instâncias de PaymentService.
 *
 * Esta é a "service provider interface" no padrão Service Provider Framework.
 * Permite que diferentes implementações sejam registradas e instanciadas
 * dinamicamente.
 *
 * @author Thiago Bianeck
 */
public interface PaymentServiceProvider {

    /**
     * Cria uma nova instância do serviço de pagamento.
     *
     * @return nova instância de PaymentService
     */
    PaymentService newService();

    /**
     * Retorna o nome do provedor.
     *
     * @return nome do provedor
     */
    String getProviderName();

    /**
     * Retorna os tipos de pagamento suportados por este provedor.
     *
     * @return array com os tipos suportados
     */
    String[] getSupportedPaymentTypes();
}