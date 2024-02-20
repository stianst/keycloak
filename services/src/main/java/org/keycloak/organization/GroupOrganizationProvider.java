package org.keycloak.organization;

import jakarta.ws.rs.NotFoundException;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.stream.Stream;

public class GroupOrganizationProvider implements OrganizationProvider {

    private final KeycloakSession session;
    private final RealmModel realm;
    private GroupModel parent;

    public GroupOrganizationProvider(KeycloakSession session) {
        this.session = session;
        this.realm = session.getContext().getRealm();

        parent = session.groups().getGroupByName(realm, null, "organizations");
        if (parent == null) {
            parent = session.groups().createGroup(realm, "organizations");
        }
    }

    @Override
    public Organization createOrganization(String organizationName) {
        GroupModel group = session.groups().createGroup(realm, organizationName, parent);
        return new GroupOrganization(session, group);
    }

    @Override
    public Organization getOrganization(String organizationName) {
        GroupModel group = session.groups().getGroupByName(realm, parent, organizationName);
        if (group == null) {
            throw new NotFoundException();
        }
        return new GroupOrganization(session, group);
    }

    public Organization getOrganization(UserModel user) {
        return user.getGroupsStream()
                .filter(g -> g.getParentId().equals(parent.getId()))
                .findFirst()
                .map(groupModel -> new GroupOrganization(session, groupModel))
                .orElse(null);
    }

    @Override
    public Stream<Organization> listOrganizations() {
        return parent.getSubGroupsStream()
                .map(g -> new GroupOrganization(session, g));
    }

    @Override
    public void close() {
    }
}
