package org.keycloak.test.migration;

public class AddKeycloakIntegrationTest extends Migrator {

    @Override
    public void rewrite() {
        int classDeclaration = findClassDeclaration();
        content.add(classDeclaration, "@KeycloakIntegrationTest");

        info(classDeclaration,"Added @KeycloakIntegrationTest");
    }

}
