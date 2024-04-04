package org.keycloak.managerpoc;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

public class MyCustomManagerImpl implements MyCustomManager {

    private final KeycloakSession session;

    public MyCustomManagerImpl(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void doSomething() {

    }
}
