package org.keycloak.organization;

import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

import java.util.stream.Stream;

public interface OrganizationProvider extends Provider {

    Organization createOrganization(String organizationName);

    Organization getOrganization(String organizationName);

    Organization getOrganization(UserModel user);

    Stream<Organization> listOrganizations();

}
