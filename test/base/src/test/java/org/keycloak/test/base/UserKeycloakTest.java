package org.keycloak.test.base;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.test.framework.KeycloakAdminClient;
import org.keycloak.test.framework.KeycloakServerUrl;
import org.keycloak.test.framework.KeycloakTest;
import org.keycloak.test.framework.KeycloakTestRealm;

@KeycloakTest
public class UserKeycloakTest {

    @KeycloakAdminClient
    Keycloak adminClient;

    // The realm is automatically created, and deleted
    @KeycloakTestRealm
    RealmResource realm;

// Not implemented, but we could perhaps let a test decide if they want to create a dedicated realm for the test, or
// use a globally configured realm through a json file
//    @KeycloakTestRealm(global = true)
//    RealmResource realm;

// Another option to the above would be to just use a shared class as below, and allow controlling cleanup
//    @KeycloakTestRealm(config = MyGlobalTestRealm.class, resetAfterAll = false|true, resetAfterEach = false|true)
//    RealmResource realm;

// Not implemented, but the idea is to be able to supply a class that can create a custom realm rather than just
// an empty realm
//    @KeycloakTestRealm(config = UserKeycloakTest.MyRealmConfig.class)
//    RealmResource realm;

    // Have the server url injected, we could extend on this and provide a class instead of string, to provide some
    // additional things like UriBuilder, account URL, etc.
    @KeycloakServerUrl
    String keycloakServerUrl;

    // Ability to inject pages, with some common pages from the framework itself
    // @Page
    // UsernamePasswordLoginPage loginPage;

    // Need some way to do easy registering and cleanup of resource, like a user, logout sessions, etc.


    @Test
    public void testCreateUser() {
        UserRepresentation user = new UserRepresentation();
        user.setUsername("my-user");
        Response response = realm.users().create(user);
        Assertions.assertEquals(201, response.getStatus());
    }

}
