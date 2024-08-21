package org.keycloak.test.framework;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.keycloak.Keycloak;

public class KeycloakTestExtension implements BeforeEachCallback {

    public KeycloakTestExtension() {
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        Keycloak.builder().start("start-dev", "--cache=local");
    }
}
