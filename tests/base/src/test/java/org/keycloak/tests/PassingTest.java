package org.keycloak.tests;

import org.keycloak.testframework.annotations.KeycloakIntegrationTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@KeycloakIntegrationTest
public class PassingTest {

    @Test
    public void failingTest() {
        Assertions.assertEquals(1, 1);
    }

}
