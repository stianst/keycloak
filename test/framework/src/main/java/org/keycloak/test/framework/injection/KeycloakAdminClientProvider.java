package org.keycloak.test.framework.injection;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.test.framework.AbstractKeycloakExtension;
import org.keycloak.test.framework.KeycloakAdminClient;

import java.lang.annotation.Annotation;

public class KeycloakAdminClientProvider implements InjectionProvider {

    private final AbstractKeycloakExtension extension;

    public KeycloakAdminClientProvider(AbstractKeycloakExtension extension) {
        this.extension = extension;
    }

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return KeycloakAdminClient.class;
    }


    @Override
    public Object getValue(ExtensionContext context, Annotation annotation) {
        if (!extension.hasResource(Keycloak.class)) {
            Keycloak instance = Keycloak.getInstance(extension.getServerUrl(), "master", "admin", "admin", "admin-cli");
            extension.putResource(Keycloak.class, instance);
        }
        return extension.getResource(Keycloak.class, Keycloak.class);
    }
}
