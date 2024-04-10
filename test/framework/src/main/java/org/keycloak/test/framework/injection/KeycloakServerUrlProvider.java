package org.keycloak.test.framework.injection;

import org.keycloak.test.framework.AbstractKeycloakExtension;
import org.keycloak.test.framework.KeycloakServerUrl;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

public class KeycloakServerUrlProvider implements InjectionProvider {

    private final AbstractKeycloakExtension extension;

    public KeycloakServerUrlProvider(AbstractKeycloakExtension extension) {
        this.extension = extension;
    }

    @Override
    public <T extends Annotation> Class<T> getAnnotation() {
        return (Class<T>) KeycloakServerUrl.class;
    }

    @Override
    public Supplier<Object> getSupplier() {
        return extension::getServerUrl;
    }
}
