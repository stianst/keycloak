package org.keycloak.quarkus.runtime;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.timer.TimerProvider;
import org.keycloak.timer.TimerProviderFactory;

public class DummyTimerProviderFactory implements TimerProviderFactory {
    @Override
    public TimerProvider create(KeycloakSession session) {
        return new DummyTimerProvider();
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
        return "dummy";
    }
}
