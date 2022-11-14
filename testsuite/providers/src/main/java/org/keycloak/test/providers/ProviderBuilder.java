package org.keycloak.test.providers;

import org.keycloak.provider.ProviderFactory;
import org.keycloak.test.providers.utils.ProviderFactoryWrapper;

import java.util.HashSet;
import java.util.Set;

public class ProviderBuilder {

    private HashSet<ProviderFactoryWrapper> factories = new HashSet<>();
    private TestCloakBuilder sessionBuilder;

    ProviderBuilder(TestCloakBuilder sessionBuilder) {
        this.sessionBuilder = sessionBuilder;
    }

    public <T extends ProviderFactory> TestCloakBuilder add(Class<T>... providerFactoryClasses) {
        for (Class<T> c : providerFactoryClasses) {
            factories.add(new ProviderFactoryWrapper(c));
        }
        return sessionBuilder;
    }

    Set<ProviderFactoryWrapper> getFactories() {
        return factories;
    }

}
