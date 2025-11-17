package com.mailist.mailist.billing.infrastructure.gateway.payment;

import com.mailist.mailist.billing.domain.gateway.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Payment Provider Factory (Factory Pattern)
 * Creates and manages payment provider instances
 */
@Component
public class PaymentProviderFactory {

    private final Map<String, PaymentProvider> providers = new HashMap<>();

    /**
     * Constructor - automatically registers all payment providers
     */
    public PaymentProviderFactory(List<PaymentProvider> paymentProviders) {
        for (PaymentProvider provider : paymentProviders) {
            register(provider.getProviderName(), provider);
        }
    }

    /**
     * Register a payment provider
     *
     * @param providerName Provider name (e.g., "stripe", "paypal")
     * @param provider Provider implementation
     */
    public void register(String providerName, PaymentProvider provider) {
        providers.put(providerName.toLowerCase(), provider);
    }

    /**
     * Get payment provider by name
     *
     * @param providerName Provider name
     * @return Payment provider instance
     * @throws IllegalArgumentException if provider not found
     */
    public PaymentProvider getProvider(String providerName) {
        PaymentProvider provider = providers.get(providerName.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException(
                "Payment provider not found: " + providerName +
                ". Available providers: " + providers.keySet()
            );
        }
        return provider;
    }

    /**
     * Check if provider is supported
     *
     * @param providerName Provider name
     * @return true if provider is registered
     */
    public boolean isProviderSupported(String providerName) {
        return providers.containsKey(providerName.toLowerCase());
    }

    /**
     * Get all supported provider names
     *
     * @return Set of provider names
     */
    public java.util.Set<String> getSupportedProviders() {
        return providers.keySet();
    }
}
