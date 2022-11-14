package org.keycloak.test.providers.mocks;

import org.keycloak.common.enums.SslRequired;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.CibaConfig;
import org.keycloak.models.ClientInitialAccessModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.OAuth2DeviceConfig;
import org.keycloak.models.OTPPolicy;
import org.keycloak.models.ParConfig;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.RequiredCredentialModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.WebAuthnPolicy;
import org.keycloak.test.providers.TestCloakException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class TestRealmModel implements RealmModel {
    public String name;
    public Map<String, String> attributes = new HashMap<>();
    public String loginTheme;

    @Override
    public String getId() {
        return name;
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
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setDisplayName(String displayName) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getDisplayNameHtml() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setDisplayNameHtml(String displayNameHtml) {
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
    public SslRequired getSslRequired() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setSslRequired(SslRequired sslRequired) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isRegistrationAllowed() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setRegistrationAllowed(boolean registrationAllowed) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isRegistrationEmailAsUsername() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setRegistrationEmailAsUsername(boolean registrationEmailAsUsername) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isRememberMe() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setRememberMe(boolean rememberMe) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isEditUsernameAllowed() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setEditUsernameAllowed(boolean editUsernameAllowed) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isUserManagedAccessAllowed() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setUserManagedAccessAllowed(boolean userManagedAccessAllowed) {
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
    public boolean isBruteForceProtected() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setBruteForceProtected(boolean value) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isPermanentLockout() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setPermanentLockout(boolean val) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getMaxFailureWaitSeconds() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setMaxFailureWaitSeconds(int val) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getWaitIncrementSeconds() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setWaitIncrementSeconds(int val) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getMinimumQuickLoginWaitSeconds() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setMinimumQuickLoginWaitSeconds(int val) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public long getQuickLoginCheckMilliSeconds() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setQuickLoginCheckMilliSeconds(long val) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getMaxDeltaTimeSeconds() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setMaxDeltaTimeSeconds(int val) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getFailureFactor() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setFailureFactor(int failureFactor) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isVerifyEmail() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setVerifyEmail(boolean verifyEmail) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isLoginWithEmailAllowed() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setLoginWithEmailAllowed(boolean loginWithEmailAllowed) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isDuplicateEmailsAllowed() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setDuplicateEmailsAllowed(boolean duplicateEmailsAllowed) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isResetPasswordAllowed() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setResetPasswordAllowed(boolean resetPasswordAllowed) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getDefaultSignatureAlgorithm() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setDefaultSignatureAlgorithm(String defaultSignatureAlgorithm) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isRevokeRefreshToken() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setRevokeRefreshToken(boolean revokeRefreshToken) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getRefreshTokenMaxReuse() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setRefreshTokenMaxReuse(int revokeRefreshTokenCount) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getSsoSessionIdleTimeout() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setSsoSessionIdleTimeout(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getSsoSessionMaxLifespan() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setSsoSessionMaxLifespan(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getSsoSessionIdleTimeoutRememberMe() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setSsoSessionIdleTimeoutRememberMe(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getSsoSessionMaxLifespanRememberMe() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setSsoSessionMaxLifespanRememberMe(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getOfflineSessionIdleTimeout() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setOfflineSessionIdleTimeout(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getAccessTokenLifespan() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isOfflineSessionMaxLifespanEnabled() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setOfflineSessionMaxLifespanEnabled(boolean offlineSessionMaxLifespanEnabled) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getOfflineSessionMaxLifespan() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setOfflineSessionMaxLifespan(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getClientSessionIdleTimeout() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setClientSessionIdleTimeout(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getClientSessionMaxLifespan() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setClientSessionMaxLifespan(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getClientOfflineSessionIdleTimeout() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setClientOfflineSessionIdleTimeout(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getClientOfflineSessionMaxLifespan() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setClientOfflineSessionMaxLifespan(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAccessTokenLifespan(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getAccessTokenLifespanForImplicitFlow() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAccessTokenLifespanForImplicitFlow(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getAccessCodeLifespan() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAccessCodeLifespan(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getAccessCodeLifespanUserAction() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAccessCodeLifespanUserAction(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public OAuth2DeviceConfig getOAuth2DeviceConfig() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public CibaConfig getCibaPolicy() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ParConfig getParPolicy() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Map<String, Integer> getUserActionTokenLifespans() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getAccessCodeLifespanLogin() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAccessCodeLifespanLogin(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getActionTokenGeneratedByAdminLifespan() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setActionTokenGeneratedByAdminLifespan(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getActionTokenGeneratedByUserLifespan() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setActionTokenGeneratedByUserLifespan(int seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public int getActionTokenGeneratedByUserLifespan(String actionTokenType) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setActionTokenGeneratedByUserLifespan(String actionTokenType, Integer seconds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<RequiredCredentialModel> getRequiredCredentialsStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void addRequiredCredential(String cred) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public PasswordPolicy getPasswordPolicy() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setPasswordPolicy(PasswordPolicy policy) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public OTPPolicy getOTPPolicy() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setOTPPolicy(OTPPolicy policy) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public WebAuthnPolicy getWebAuthnPolicy() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setWebAuthnPolicy(WebAuthnPolicy policy) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public WebAuthnPolicy getWebAuthnPolicyPasswordless() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setWebAuthnPolicyPasswordless(WebAuthnPolicy policy) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RoleModel getRoleById(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<GroupModel> getDefaultGroupsStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void addDefaultGroup(GroupModel group) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeDefaultGroup(GroupModel group) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ClientModel> getClientsStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ClientModel> getClientsStream(Integer firstResult, Integer maxResults) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Long getClientsCount() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ClientModel> getAlwaysDisplayInConsoleClientsStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientModel addClient(String name) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientModel addClient(String id, String clientId) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean removeClient(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientModel getClientById(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientModel getClientByClientId(String clientId) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ClientModel> searchClientByClientIdStream(String clientId, Integer firstResult, Integer maxResults) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ClientModel> searchClientByAttributes(Map<String, String> attributes, Integer firstResult, Integer maxResults) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void updateRequiredCredentials(Set<String> creds) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Map<String, String> getBrowserSecurityHeaders() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setBrowserSecurityHeaders(Map<String, String> headers) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Map<String, String> getSmtpConfig() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setSmtpConfig(Map<String, String> smtpConfig) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationFlowModel getBrowserFlow() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setBrowserFlow(AuthenticationFlowModel flow) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationFlowModel getRegistrationFlow() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setRegistrationFlow(AuthenticationFlowModel flow) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationFlowModel getDirectGrantFlow() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setDirectGrantFlow(AuthenticationFlowModel flow) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationFlowModel getResetCredentialsFlow() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setResetCredentialsFlow(AuthenticationFlowModel flow) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationFlowModel getClientAuthenticationFlow() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setClientAuthenticationFlow(AuthenticationFlowModel flow) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationFlowModel getDockerAuthenticationFlow() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setDockerAuthenticationFlow(AuthenticationFlowModel flow) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<AuthenticationFlowModel> getAuthenticationFlowsStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationFlowModel getFlowByAlias(String alias) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationFlowModel addAuthenticationFlow(AuthenticationFlowModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationFlowModel getAuthenticationFlowById(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeAuthenticationFlow(AuthenticationFlowModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void updateAuthenticationFlow(AuthenticationFlowModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<AuthenticationExecutionModel> getAuthenticationExecutionsStream(String flowId) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationExecutionModel getAuthenticationExecutionById(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationExecutionModel getAuthenticationExecutionByFlowId(String flowId) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationExecutionModel addAuthenticatorExecution(AuthenticationExecutionModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void updateAuthenticatorExecution(AuthenticationExecutionModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeAuthenticatorExecution(AuthenticationExecutionModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<AuthenticatorConfigModel> getAuthenticatorConfigsStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticatorConfigModel addAuthenticatorConfig(AuthenticatorConfigModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void updateAuthenticatorConfig(AuthenticatorConfigModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeAuthenticatorConfig(AuthenticatorConfigModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticatorConfigModel getAuthenticatorConfigById(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticatorConfigModel getAuthenticatorConfigByAlias(String alias) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<RequiredActionProviderModel> getRequiredActionProvidersStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RequiredActionProviderModel addRequiredActionProvider(RequiredActionProviderModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void updateRequiredActionProvider(RequiredActionProviderModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeRequiredActionProvider(RequiredActionProviderModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RequiredActionProviderModel getRequiredActionProviderById(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RequiredActionProviderModel getRequiredActionProviderByAlias(String alias) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<IdentityProviderModel> getIdentityProvidersStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public IdentityProviderModel getIdentityProviderByAlias(String alias) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void addIdentityProvider(IdentityProviderModel identityProvider) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeIdentityProviderByAlias(String alias) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void updateIdentityProvider(IdentityProviderModel identityProvider) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<IdentityProviderMapperModel> getIdentityProviderMappersStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<IdentityProviderMapperModel> getIdentityProviderMappersByAliasStream(String brokerAlias) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public IdentityProviderMapperModel addIdentityProviderMapper(IdentityProviderMapperModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeIdentityProviderMapper(IdentityProviderMapperModel mapping) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void updateIdentityProviderMapper(IdentityProviderMapperModel mapping) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public IdentityProviderMapperModel getIdentityProviderMapperById(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public IdentityProviderMapperModel getIdentityProviderMapperByName(String brokerAlias, String name) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ComponentModel addComponentModel(ComponentModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ComponentModel importComponentModel(ComponentModel model) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void updateComponent(ComponentModel component) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeComponent(ComponentModel component) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeComponents(String parentId) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ComponentModel> getComponentsStream(String parentId, String providerType) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ComponentModel> getComponentsStream(String parentId) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ComponentModel> getComponentsStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ComponentModel getComponent(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getLoginTheme() {
        return loginTheme;
    }

    @Override
    public void setLoginTheme(String name) {
        this.loginTheme = name;
    }

    @Override
    public String getAccountTheme() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAccountTheme(String name) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getAdminTheme() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAdminTheme(String name) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getEmailTheme() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setEmailTheme(String name) {
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
    public boolean isEventsEnabled() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setEventsEnabled(boolean enabled) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public long getEventsExpiration() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setEventsExpiration(long expiration) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<String> getEventsListenersStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setEventsListeners(Set<String> listeners) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<String> getEnabledEventTypesStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setEnabledEventTypes(Set<String> enabledEventTypes) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isAdminEventsEnabled() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAdminEventsEnabled(boolean enabled) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isAdminEventsDetailsEnabled() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAdminEventsDetailsEnabled(boolean enabled) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientModel getMasterAdminClient() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setMasterAdminClient(ClientModel client) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RoleModel getDefaultRole() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setDefaultRole(RoleModel role) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isIdentityFederationEnabled() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean isInternationalizationEnabled() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setInternationalizationEnabled(boolean enabled) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<String> getSupportedLocalesStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setSupportedLocales(Set<String> locales) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public String getDefaultLocale() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setDefaultLocale(String locale) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public GroupModel createGroup(String id, String name, GroupModel toParent) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public GroupModel getGroupById(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<GroupModel> getGroupsStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Long getGroupsCount(Boolean onlyTopGroups) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Long getGroupsCountByNameContaining(String search) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<GroupModel> getTopLevelGroupsStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<GroupModel> getTopLevelGroupsStream(Integer first, Integer max) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<GroupModel> searchForGroupByNameStream(String search, Integer first, Integer max) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean removeGroup(GroupModel group) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void moveGroup(GroupModel group, GroupModel toParent) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ClientScopeModel> getClientScopesStream() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientScopeModel addClientScope(String name) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientScopeModel addClientScope(String id, String name) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean removeClientScope(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientScopeModel getClientScopeById(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void addDefaultClientScope(ClientScopeModel clientScope, boolean defaultScope) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeDefaultClientScope(ClientScopeModel clientScope) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void createOrUpdateRealmLocalizationTexts(String locale, Map<String, String> localizationTexts) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public boolean removeRealmLocalizationTexts(String locale) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Map<String, Map<String, String>> getRealmLocalizationTexts() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Map<String, String> getRealmLocalizationTextsByLocale(String locale) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ClientScopeModel> getDefaultClientScopesStream(boolean defaultScope) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientInitialAccessModel createClientInitialAccessModel(int expiration, int count) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientInitialAccessModel getClientInitialAccessModel(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void removeClientInitialAccessModel(String id) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Stream<ClientInitialAccessModel> getClientInitialAccesses() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void decreaseRemainingCount(ClientInitialAccessModel clientInitialAccess) {
        throw TestCloakException.notImplemented();
    }
}
