package org.keycloak.quarkus.runtime.benchmark.tasks;

import jakarta.ws.rs.core.MediaType;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmProvider;

public class Sessions implements BenchmarkTask {

    private KeycloakSessionFactory sessionFactory;

    public Sessions(KeycloakSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Object run() {
        KeycloakSession session = sessionFactory.create();
        session.getProvider(RealmProvider.class);
        session.getProvider(SignatureProvider.class);
        session.close();
        return null;
    }

    @Override
    public MediaType mediaType() {
        return null;
    }

    @Override
    public void close() {
    }
}
