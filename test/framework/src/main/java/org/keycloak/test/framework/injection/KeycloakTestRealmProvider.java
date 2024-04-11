package org.keycloak.test.framework.injection;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.test.framework.AbstractKeycloakExtension;
import org.keycloak.test.framework.KeycloakAdminClient;
import org.keycloak.test.framework.KeycloakTestRealm;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

public class KeycloakTestRealmProvider implements InjectionProvider {

    private final AbstractKeycloakExtension extension;

    public KeycloakTestRealmProvider(AbstractKeycloakExtension extension) {
        this.extension = extension;
    }

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return KeycloakTestRealm.class;
    }

    @Override
    public Object getValue(ExtensionContext context, Annotation annotation) {
        KeycloakTestRealm keycloakTestRealm = (KeycloakTestRealm) annotation;
        if (!extension.hasResource(KeycloakTestRealm.class)) {
            String realmName = ((KeycloakTestRealm) annotation).name();
            if (realmName == null || realmName.isEmpty()) {
                realmName = context.getRequiredTestClass().getName();
            }

            Keycloak keycloak = extension.getResource(Keycloak.class);
            RealmRepresentation realm = new RealmRepresentation();
            realm.setRealm(realmName);
            keycloak.realms().create(realm);

            RealmResource realmResource = keycloak.realm(realmName);
            extension.putResource(RealmResource.class, realmResource);

            System.out.println("Created realm " + realmName);
        }
        return extension.getResource(RealmResource.class);
    }

    @Override
    public void afterAll() {
        RealmResource resource = extension.getResource(RealmResource.class);
        if (resource != null) {
            System.out.println("Deleted realm");
            resource.remove();
        }
    }
}
