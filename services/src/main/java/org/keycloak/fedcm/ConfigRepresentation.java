package org.keycloak.fedcm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigRepresentation {

    @JsonProperty("accounts_endpoint")
    private String accountsEndpoint;

    @JsonProperty("id_assertion_endpoint")
    private String idAssertionEndpoint;

    public String getAccountsEndpoint() {
        return accountsEndpoint;
    }

    public void setAccountsEndpoint(String accountsEndpoint) {
        this.accountsEndpoint = accountsEndpoint;
    }

    public String getIdAssertionEndpoint() {
        return idAssertionEndpoint;
    }

    public void setIdAssertionEndpoint(String idAssertionEndpoint) {
        this.idAssertionEndpoint = idAssertionEndpoint;
    }
}
