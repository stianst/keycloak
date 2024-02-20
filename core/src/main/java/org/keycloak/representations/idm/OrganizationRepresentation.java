package org.keycloak.representations.idm;

public class OrganizationRepresentation {

    private String name;

    public OrganizationRepresentation() {
    }

    public OrganizationRepresentation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
