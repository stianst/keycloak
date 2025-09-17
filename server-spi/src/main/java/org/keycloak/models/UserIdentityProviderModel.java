package org.keycloak.models;

public class UserIdentityProviderModel extends IdentityProviderModel {

    public UserIdentityProviderModel() {
    }

    public UserIdentityProviderModel(IdentityProviderModel model) {
        super(model);
    }

    @Override
    public Boolean isTrustEmail() {
        return Boolean.TRUE.equals(super.isTrustEmail());
    }

    @Override
    public Boolean isStoreToken() {
        return Boolean.TRUE.equals(super.isStoreToken());
    }

    @Override
    public Boolean isAddReadTokenRoleOnCreate() {
        return Boolean.TRUE.equals(super.isAddReadTokenRoleOnCreate());
    }

    @Override
    public Boolean isLinkOnly() {
        return Boolean.TRUE.equals(super.isLinkOnly());
    }

    @Override
    public Boolean isAuthenticateByDefault() {
        return Boolean.TRUE.equals(super.isAuthenticateByDefault());
    }

    @Override
    public Boolean isHideOnLogin() {
        return Boolean.TRUE.equals(super.isHideOnLogin());
    }

}
