package com.mailist.mailist.billing.infrastructure.gateway.invoicing;

import com.mailist.mailist.billing.domain.gateway.InvoicingProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Invoicing Provider Factory (Factory Pattern)
 * Creates and manages invoicing provider instances
 */
@Component
public class InvoicingProviderFactory {

    private final Map<String, InvoicingProvider> providers = new HashMap<>();

    /**
     * Constructor - automatically registers all invoicing providers
     */
    public InvoicingProviderFactory(List<InvoicingProvider> invoicingProviders) {
        for (InvoicingProvider provider : invoicingProviders) {
            register(provider.getProviderName(), provider);
        }
    }

    /**
     * Register an invoicing provider
     *
     * @param providerName Provider name (e.g., "fakturownia", "infakt")
     * @param provider Provider implementation
     */
    public void register(String providerName, InvoicingProvider provider) {
        providers.put(providerName.toLowerCase(), provider);
    }

    /**
     * Get invoicing provider by name
     *
     * @param providerName Provider name
     * @return Invoicing provider instance
     * @throws IllegalArgumentException if provider not found
     */
    public InvoicingProvider getProvider(String providerName) {
        InvoicingProvider provider = providers.get(providerName.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException(
                "Invoicing provider not found: " + providerName +
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
