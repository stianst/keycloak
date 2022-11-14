package org.keycloak.test.providers.mocks;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.test.providers.TestCloakException;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class TestClientModel implements ClientModel {

    public String clientId;
    public Map<String, String> attributes;

    @Override
    public void updateClient() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getId() {
        return clientId;
    }

    @Override
    public RoleModel getRole(String name) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RoleModel addRole(String name) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RoleModel addRole(String id, String name) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean removeRole(RoleModel role) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<RoleModel> getRolesStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<RoleModel> getRolesStream(Integer firstResult, Integer maxResults) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<RoleModel> searchForRolesStream(String search, Integer first, Integer max) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<String> getDefaultRolesStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void addDefaultRole(String name) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeDefaultRoles(String... defaultRoles) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getName() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getDescription() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setDescription(String description) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isEnabled() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setEnabled(boolean enabled) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isAlwaysDisplayInConsole() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAlwaysDisplayInConsole(boolean alwaysDisplayInConsole) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isSurrogateAuthRequired() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setSurrogateAuthRequired(boolean surrogateAuthRequired) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Set<String> getWebOrigins() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setWebOrigins(Set<String> webOrigins) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void addWebOrigin(String webOrigin) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeWebOrigin(String webOrigin) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Set<String> getRedirectUris() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setRedirectUris(Set<String> redirectUris) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void addRedirectUri(String redirectUri) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeRedirectUri(String redirectUri) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getManagementUrl() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setManagementUrl(String url) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getRootUrl() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setRootUrl(String url) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getBaseUrl() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setBaseUrl(String url) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isBearerOnly() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setBearerOnly(boolean only) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getNodeReRegistrationTimeout() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setNodeReRegistrationTimeout(int timeout) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getClientAuthenticatorType() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setClientAuthenticatorType(String clientAuthenticatorType) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean validateSecret(String secret) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getSecret() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setSecret(String secret) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getRegistrationToken() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setRegistrationToken(String registrationToken) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getProtocol() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setProtocol(String protocol) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public String getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public String getAuthenticationFlowBindingOverride(String binding) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Map<String, String> getAuthenticationFlowBindingOverrides() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeAuthenticationFlowBindingOverride(String binding) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAuthenticationFlowBindingOverride(String binding, String flowId) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isFrontchannelLogout() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setFrontchannelLogout(boolean flag) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isFullScopeAllowed() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setFullScopeAllowed(boolean value) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isPublicClient() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setPublicClient(boolean flag) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isConsentRequired() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setConsentRequired(boolean consentRequired) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isStandardFlowEnabled() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setStandardFlowEnabled(boolean standardFlowEnabled) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isImplicitFlowEnabled() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setImplicitFlowEnabled(boolean implicitFlowEnabled) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isDirectAccessGrantsEnabled() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setDirectAccessGrantsEnabled(boolean directAccessGrantsEnabled) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isServiceAccountsEnabled() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setServiceAccountsEnabled(boolean serviceAccountsEnabled) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RealmModel getRealm() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void addClientScope(ClientScopeModel clientScope, boolean defaultScope) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void addClientScopes(Set<ClientScopeModel> clientScopes, boolean defaultScope) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeClientScope(ClientScopeModel clientScope) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Map<String, ClientScopeModel> getClientScopes(boolean defaultScope) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getNotBefore() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setNotBefore(int notBefore) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Map<String, Integer> getRegisteredNodes() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void registerNode(String nodeHost, int registrationTime) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void unregisterNode(String nodeHost) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ProtocolMapperModel> getProtocolMappersStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ProtocolMapperModel addProtocolMapper(ProtocolMapperModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeProtocolMapper(ProtocolMapperModel mapping) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void updateProtocolMapper(ProtocolMapperModel mapping) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ProtocolMapperModel getProtocolMapperById(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ProtocolMapperModel getProtocolMapperByName(String protocol, String name) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<RoleModel> getScopeMappingsStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<RoleModel> getRealmScopeMappingsStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void addScopeMapping(RoleModel role) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void deleteScopeMapping(RoleModel role) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean hasScope(RoleModel role) {
        throw TestCloakException.notImplemented();
    }

}
