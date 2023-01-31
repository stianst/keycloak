package org.keycloak.testsuite.account;

import org.keycloak.testsuite.util.TokenUtil;

public class AccountClientBuilder {

    private String realm = "test";
    private String username = "test-user@localhost";
    private String password = "password";

    public AccountClientBuilder realm(String realm) {
        this.realm = realm;
        return this;
    }

    public AccountClientBuilder username(String username) {
        this.username = username;
        return this;
    }

    public AccountClientBuilder password(String password) {
        this.password = password;
        return this;
    }

    public AccountClient build() {
        TokenUtil tokenUtil = new TokenUtil(username, password);
        tokenUtil.
        return new AccountClient(realm, username, password);
    }

}
