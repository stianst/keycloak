package org.keycloak.test.base;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.test.framework.KeycloakAdminClient;
import org.keycloak.test.framework.KeycloakExtension;
import org.keycloak.test.framework.KeycloakTestRealm;

@ExtendWith(KeycloakExtension.class)
public class UserTest {

    @KeycloakAdminClient
    Keycloak adminClient;

    @KeycloakTestRealm
    RealmResource realm;

    @Test
    public void testCreateUser() {
        UserRepresentation user = new UserRepresentation();
        user.setUsername("testCreateUser");
        Response response = realm.users().create(user);
        Assertions.assertEquals(201, response.getStatus());
    }

}
