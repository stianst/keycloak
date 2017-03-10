package org.keycloak.profile;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * Created by st on 09/03/17.
 */
public class ProfileSpi implements Spi {
    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return ProfileProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ProfileProviderFactory.class;
    }
}
