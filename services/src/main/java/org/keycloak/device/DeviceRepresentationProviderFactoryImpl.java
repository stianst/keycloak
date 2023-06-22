package org.keycloak.device;

import org.keycloak.models.KeycloakSession;

public class DeviceRepresentationProviderFactoryImpl implements DeviceRepresentationProviderFactory {

    public static final String PROVIDER_ID = "deviceRepresentation";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public DeviceRepresentationProvider create(KeycloakSession session) {
        lazyInit(session);
        return new DeviceRepresentationProviderImpl(session);
    }

    private void lazyInit(KeycloakSession session) {

    }
}
