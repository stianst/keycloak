package org.keycloak.organization;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

public class GroupOrganizationProviderFactory implements OrganizationProviderFactory, EnvironmentDependentProviderFactory {
    @Override
    public OrganizationProvider create(KeycloakSession session) {
        return new GroupOrganizationProvider(session);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "groups";
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return true;
    }
}
