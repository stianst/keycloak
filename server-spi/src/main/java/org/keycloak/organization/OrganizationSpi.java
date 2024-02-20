package org.keycloak.organization;

import org.keycloak.common.Profile;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class OrganizationSpi implements Spi {


    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "organization";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return OrganizationProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return OrganizationProviderFactory.class;
    }

    @Override
    public boolean isEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION);
    }
}
