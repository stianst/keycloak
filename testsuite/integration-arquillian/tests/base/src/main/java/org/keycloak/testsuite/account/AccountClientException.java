package org.keycloak.testsuite.account;

public class AccountClientException extends RuntimeException {

    public AccountClientException(String message) {
        super(message);
    }

    public AccountClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountClientException(Throwable cause) {
        super(cause);
    }
}
