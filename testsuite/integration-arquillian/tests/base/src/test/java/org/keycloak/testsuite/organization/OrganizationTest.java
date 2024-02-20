package org.keycloak.testsuite.organization;

import org.junit.Test;
import org.keycloak.admin.client.resource.OrganizationResource;
import org.keycloak.admin.client.resource.OrganizationsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.common.Profile;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.testsuite.AbstractKeycloakTest;
import org.keycloak.testsuite.Assert;
import org.keycloak.testsuite.arquillian.annotation.EnableFeature;
import org.keycloak.testsuite.util.RealmBuilder;
import org.keycloak.testsuite.util.UserBuilder;

import java.util.List;

@EnableFeature(Profile.Feature.ORGANIZATION)
public class OrganizationTest extends AbstractKeycloakTest {

    public static final String TEST_REALM = "org-test";

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {
        testRealms.add(RealmBuilder.create().name(TEST_REALM).user(UserBuilder.create().username("myuser")).build());
    }

    @Test
    public void addUserToGroup() {
        RealmResource realm = adminClient.realm(TEST_REALM);
        OrganizationsResource organizations = realm.organizations();

        OrganizationRepresentation orgRep = new OrganizationRepresentation("myorg");
        organizations.createOrganization(orgRep);

        List<OrganizationRepresentation> organizationList = organizations.listOrganizations();
        Assert.assertEquals(1, organizationList.size());
        Assert.assertEquals("myorg", organizationList.get(0).getName());

        String userId = realm.users().search("myuser").get(0).getId();

        OrganizationResource organization = organizations.getOrganization("myorg");

        organization.addUser(userId);

        List<UserRepresentation> users = organization.listUsers();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals("myuser", users.get(0).getUsername());

        organization.removeUser(userId);

        Assert.assertEquals(0, organization.listUsers().size());
    }

}
