package org.keycloak.tests;

import org.keycloak.testframework.annotations.KeycloakIntegrationTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@KeycloakIntegrationTest
public class ErrorTest {

    @BeforeAll
    public static void all() {
        throw new RuntimeException("hello");
    }

    @Test
    public void failingTest() {
        throw new RuntimeException("Hello");
    }

}
