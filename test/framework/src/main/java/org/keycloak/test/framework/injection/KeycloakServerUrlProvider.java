package org.keycloak.test.framework.injection;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.keycloak.test.framework.AbstractKeycloakExtension;
import org.keycloak.test.framework.KeycloakServerUrl;

import java.lang.annotation.Annotation;

public class KeycloakServerUrlProvider implements InjectionProvider {

    private final AbstractKeycloakExtension extension;

    public KeycloakServerUrlProvider(AbstractKeycloakExtension extension) {
        this.extension = extension;
    }

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return KeycloakServerUrl.class;
    }

    @Override
    public Object getValue(ExtensionContext context, Annotation annotation) {
        return extension.getServerUrl();
    }
}
