package org.keycloak.services.validation;

import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import java.util.List;
import java.util.Set;

/**
 * Created by st on 09/03/17.
 */
public abstract class ValidationUserAdapter implements UserModel {

    protected UserModel user;

    public ValidationUserAdapter(UserModel user) {
        this.user = user;
    }

    @Override
    public Set<RoleModel> getRealmRoleMappings() {
        return user.getRealmRoleMappings();
    }

    @Override
    public Set<RoleModel> getClientRoleMappings(ClientModel app) {
        return user.getClientRoleMappings(app);
    }

    @Override
    public String getId() {
        return user.getId();
    }

    @Override
    public void setUsername(String username) {
        user.setUsername(username);
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        user.setSingleAttribute(name, value);
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        user.setAttribute(name, values);
    }

    @Override
    public void removeAttribute(String name) {
        user.removeAttribute(name);
    }

    @Override
    public void setFirstName(String firstName) {
        user.setFirstName(firstName);
    }

    @Override
    public void setLastName(String lastName) {
        user.setLastName(lastName);
    }

    @Override
    public void setEmail(String email) {
        user.setEmail(email);
    }

    @Override
    public boolean isEmailVerified() {
        return user.isEmailVerified();
    }

    @Override
    public void setEmailVerified(boolean verified) {
        user.setEmailVerified(verified);
    }

    @Override
    public Long getCreatedTimestamp() {
        return user.getCreatedTimestamp();
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        user.setCreatedTimestamp(timestamp);
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        user.setEnabled(enabled);
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return user.hasRole(role);
    }

    @Override
    public void grantRole(RoleModel role) {
        user.grantRole(role);
    }

    @Override
    public Set<RoleModel> getRoleMappings() {
        return user.getRoleMappings();
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {
        user.deleteRoleMapping(role);
    }

    @Override
    public Set<String> getRequiredActions() {
        return user.getRequiredActions();
    }

    @Override
    public void addRequiredAction(String action) {
        user.addRequiredAction(action);
    }

    @Override
    public void removeRequiredAction(String action) {
        user.removeRequiredAction(action);
    }

    @Override
    public void addRequiredAction(RequiredAction action) {
        user.addRequiredAction(action);
    }

    @Override
    public void removeRequiredAction(RequiredAction action) {
        user.removeRequiredAction(action);
    }

    @Override
    public Set<GroupModel> getGroups() {
        return user.getGroups();
    }

    @Override
    public void joinGroup(GroupModel group) {
        user.joinGroup(group);
    }

    @Override
    public void leaveGroup(GroupModel group) {
        user.leaveGroup(group);
    }

    @Override
    public boolean isMemberOf(GroupModel group) {
        return user.isMemberOf(group);
    }

    @Override
    public String getFederationLink() {
        return user.getFederationLink();
    }

    @Override
    public void setFederationLink(String link) {
        user.setFederationLink(link);
    }

    @Override
    public String getServiceAccountClientLink() {
        return user.getServiceAccountClientLink();
    }

    @Override
    public void setServiceAccountClientLink(String clientInternalId) {
        user.setServiceAccountClientLink(clientInternalId);
    }
}
