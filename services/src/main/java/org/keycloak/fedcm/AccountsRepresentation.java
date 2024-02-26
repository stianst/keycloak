package org.keycloak.fedcm;

import java.util.List;

public class AccountsRepresentation {

    private List<AccountRepresentation> accounts;

    public List<AccountRepresentation> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountRepresentation> accounts) {
        this.accounts = accounts;
    }
}
