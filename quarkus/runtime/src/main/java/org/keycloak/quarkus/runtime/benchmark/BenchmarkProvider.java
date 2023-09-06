package org.keycloak.quarkus.runtime.benchmark;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class BenchmarkProvider implements RealmResourceProvider {

    private KeycloakSession session;

    public BenchmarkProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new BenchmarkResource(session);
    }

    @Override
    public void close() {

    }
}
