package org.keycloak.test.framework.injection;

import org.keycloak.test.framework.AbstractKeycloakExtension;
import org.keycloak.test.framework.KeycloakAdminClient;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

public class KeycloakAdminClientProvider implements InjectionProvider {

    private final AbstractKeycloakExtension extension;

    public KeycloakAdminClientProvider(AbstractKeycloakExtension extension) {
        this.extension = extension;
    }

    @Override
    public <T extends Annotation> Class<T> getAnnotation() {
        return (Class<T>) KeycloakAdminClient.class;
    }


    @Override
    public Supplier<Object> getSupplier() {
        return () -> org.keycloak.admin.client.Keycloak.getInstance(extension.getServerUrl(), "master", "admin", "admin", "admin-cli");
    }
}
