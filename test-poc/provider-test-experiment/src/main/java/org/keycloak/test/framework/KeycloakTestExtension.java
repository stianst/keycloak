package org.keycloak.test.framework;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.keycloak.Keycloak;

public class KeycloakTestExtension implements BeforeEachCallback {

    public KeycloakTestExtension() {
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
            System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
            System.setProperty("picocli.disable.closures", "true");
            System.setProperty("java.util.concurrent.ForkJoinPool.common.threadFactory", "io.quarkus.bootstrap.forkjoin.QuarkusForkJoinWorkerThreadFactory");

        Keycloak.builder().start("start-dev", "--cache=local");
    }
}
