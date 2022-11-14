package org.keycloak.test.providers.mocks;

import context.TestKeycloakContext;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientProvider;
import org.keycloak.models.ClientScopeProvider;
import org.keycloak.models.GroupProvider;
import org.keycloak.models.KeyManager;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.RoleProvider;
import org.keycloak.models.ThemeManager;
import org.keycloak.models.TokenManager;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserLoginFailureProvider;
import org.keycloak.models.UserProvider;
import org.keycloak.models.UserSessionProvider;
import org.keycloak.provider.InvalidationHandler;
import org.keycloak.provider.Provider;
import org.keycloak.services.clientpolicy.ClientPolicyManager;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.test.providers.utils.ProviderFactoryWrapper;
import org.keycloak.test.providers.TestCloakException;
import org.keycloak.vault.VaultTranscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class TestCloakSession implements KeycloakSession {

    private Set<ProviderFactoryWrapper> factories;
    private TestKeycloakContext context;

    private Map<Class, Provider> providers;

    public TestCloakSession(Set<ProviderFactoryWrapper> factories, TestKeycloakContext context) {
        this.factories = factories;
        this.context = context;
        this.providers = new HashMap<>();
    }

    @Override
    public KeycloakContext getContext() {
        if (context == null) {
            throw TestCloakException.notConfigured();
        }
        return context;
    }

    @Override
    public KeycloakTransactionManager getTransactionManager() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public <T extends Provider> T getProvider(Class<T> clazz) {
        Optional<ProviderFactoryWrapper> wrapper = factories.stream().filter(f -> f.supports(clazz)).findFirst();
        return wrapper.isPresent() ? (T) wrapper.get().getProvider(this) : null;
    }

    @Override
    public <T extends Provider> T getProvider(Class<T> clazz, String id) {
        Optional<ProviderFactoryWrapper> wrapper = factories.stream().filter(f -> f.supports(clazz) && f.getProviderFactory().getId().equals(id)).findFirst();
        return wrapper.isPresent() ? (T) wrapper.get().getProvider(this) : null;
    }

    @Override
    public <T extends Provider> T getComponentProvider(Class<T> clazz, String componentId) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public <T extends Provider> T getComponentProvider(Class<T> clazz, String componentId, Function<KeycloakSessionFactory, ComponentModel> modelGetter) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public <T extends Provider> T getProvider(Class<T> clazz, ComponentModel componentModel) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public <T extends Provider> Set<String> listProviderIds(Class<T> clazz) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public <T extends Provider> Set<T> getAllProviders(Class<T> clazz) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Class<? extends Provider> getProviderClass(String providerClassName) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Object getAttribute(String attribute) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public <T> T getAttribute(String attribute, Class<T> clazz) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public Object removeAttribute(String attribute) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void setAttribute(String name, Object value) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void invalidate(InvalidationHandler.InvalidableObjectType type, Object... params) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void enlistForClose(Provider provider) {
        throw TestCloakException.notImplemented();
    }

    @Override
    public KeycloakSessionFactory getKeycloakSessionFactory() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RealmProvider realms() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientProvider clients() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientScopeProvider clientScopes() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public GroupProvider groups() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RoleProvider roles() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public UserSessionProvider sessions() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public UserLoginFailureProvider loginFailures() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public AuthenticationSessionProvider authenticationSessions() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public void close() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public UserProvider userCache() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public UserProvider users() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientProvider clientStorageManager() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientScopeProvider clientScopeStorageManager() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RoleProvider roleStorageManager() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public GroupProvider groupStorageManager() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public UserProvider userStorageManager() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public UserCredentialManager userCredentialManager() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public UserProvider userLocalStorage() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RealmProvider realmLocalStorage() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientProvider clientLocalStorage() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientScopeProvider clientScopeLocalStorage() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public GroupProvider groupLocalStorage() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public RoleProvider roleLocalStorage() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public KeyManager keys() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ThemeManager theme() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public TokenManager tokens() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public VaultTranscriber vault() {
        throw TestCloakException.notImplemented();
    }

    @Override
    public ClientPolicyManager clientPolicy() {
        throw TestCloakException.notImplemented();
    }
}
