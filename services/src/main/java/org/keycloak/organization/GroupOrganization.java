package org.keycloak.organization;

import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.util.stream.Stream;

public class GroupOrganization implements Organization {

    private final KeycloakSession session;
    private final GroupModel group;

    public GroupOrganization(KeycloakSession session, GroupModel group) {
        this.session = session;
        this.group = group;
    }

    public GroupModel getGroup() {
        return group;
    }

    @Override
    public String getName() {
        return group.getName();
    }

    @Override
    public void addUser(UserModel user) {
        group.setSingleAttribute("groupType", "organization");
        user.joinGroup(group);
    }

    @Override
    public void removeUser(UserModel user) {
        user.leaveGroup(group);
    }

    @Override
    public Stream<UserModel> listUsers() {
        return session.users().getGroupMembersStream(session.getContext().getRealm(), group);
    }

}
