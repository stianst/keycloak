package org.keycloak.test.framework;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.keycloak.Keycloak;
import org.keycloak.common.Version;
import org.keycloak.test.framework.injection.Injector;

import java.util.concurrent.TimeoutException;

public class KeycloakSharedExtension extends AbstractKeycloakExtension implements BeforeAllCallback, BeforeEachCallback {

    private static Keycloak KEYCLOAK;

    private final Injector injector;

    public KeycloakSharedExtension() {
        injector = new Injector(this);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (KEYCLOAK == null) {
            KEYCLOAK = Keycloak.builder()
                    .setVersion(Version.VERSION)
                    .addDependency("org.keycloak.extension", "kc-ext-poc", "1.0-SNAPSHOT")
                    .start("start-dev", "--db=dev-mem", "--cache=local");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    KEYCLOAK.stop();
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        injector.inject(context, testInstance);
    }

    public String getServerUrl() {
        return "http://localhost:8080";
    }

}
