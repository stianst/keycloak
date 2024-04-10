package org.keycloak.test.base;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.test.framework.KeycloakAdminClient;
import org.keycloak.test.framework.KeycloakTest;

@KeycloakTest
public class UserKeycloakTest {

    @KeycloakAdminClient
    Keycloak adminClient;

    @Test
    public void testCreateUser() {
        UserRepresentation user = new UserRepresentation();
        user.setUsername("my-user");
        Response response = adminClient.realm("master").users().create(user);
        Assertions.assertEquals(201, response.getStatus());
    }

}
