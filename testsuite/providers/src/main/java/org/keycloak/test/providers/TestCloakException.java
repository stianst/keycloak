package org.keycloak.test.providers;

public class TestCloakException extends RuntimeException {

    public static TestCloakException notImplemented() {
        return new TestCloakException("Method not implemented");
    }

    public static TestCloakException notConfigured() {
        return new TestCloakException("Not configured");
    }

    public TestCloakException(String message) {
        super(message);
    }

    public TestCloakException(Exception e) {
        super(e);
    }

    public TestCloakException(String message, Exception e) {
        super(message, e);
    }

}
