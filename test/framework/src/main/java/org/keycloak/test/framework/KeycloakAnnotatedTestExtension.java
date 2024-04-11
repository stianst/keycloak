package org.keycloak.test.framework;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.keycloak.Keycloak;
import org.keycloak.common.Version;
import org.keycloak.test.framework.injection.Injector;

public class KeycloakAnnotatedTestExtension extends AbstractKeycloakExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

    private Keycloak keycloak;

    private final Injector injector;

    public KeycloakAnnotatedTestExtension() {
        injector = new Injector(this);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!context.getRequiredTestClass().isAnnotationPresent(KeycloakTest.class)) {
            return;
        }

        keycloak = Keycloak.builder()
                .setVersion(Version.VERSION)
                .addDependency("org.keycloak.extension", "kc-ext-poc", "1.0-SNAPSHOT")
                .start("start-dev", "--db=dev-mem", "--cache=local");
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (!context.getRequiredTestClass().isAnnotationPresent(KeycloakTest.class)) {
            return;
        }

        keycloak.stop();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (!context.getRequiredTestClass().isAnnotationPresent(KeycloakTest.class)) {
            return;
        }

        Object testInstance = context.getRequiredTestInstance();
        injector.inject(context, testInstance);
    }

}
