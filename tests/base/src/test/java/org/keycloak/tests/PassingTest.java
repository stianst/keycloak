package org.keycloak.tests;

import org.keycloak.testframework.annotations.KeycloakIntegrationTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@KeycloakIntegrationTest
public class PassingTest {

    @Test
    public void failingTest() {
        Assertions.assertEquals(1, 1);
    }

    @Test
    @Disabled
    public void disabledTest() {
        Assertions.assertEquals(1, 2);
    }

}
