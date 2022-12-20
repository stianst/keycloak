package org.keycloak.theme;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class ThemeResourceVersionSpi implements Spi {
    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return ThemeResourceVersionProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ThemeResourceVersionProviderFactory.class;
    }
}
