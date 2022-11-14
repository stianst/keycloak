package org.keycloak.test.providers.utils;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.test.providers.TestCloakException;
import org.keycloak.test.providers.mocks.TestCloakSession;

import java.lang.reflect.Method;

public class ProviderFactoryWrapper {

    private Class<? extends ProviderFactory> providerFactoryClass;

    private Class<? extends Provider> providerClass;

    private ProviderFactory providerFactory;

    private Provider provider;

    private Config.Scope scope;

    public <T extends ProviderFactory> ProviderFactoryWrapper(Class<T> providerFactoryClass) {
        this.providerFactoryClass = providerFactoryClass;
    }

    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return providerFactoryClass;
    }

    public ProviderFactory getProviderFactory() {
        if (this.providerFactory == null) {
            try {
                this.providerFactory = providerFactoryClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new TestCloakException(e);
            }
        }
        return this.providerFactory;
    }

    public Provider getProvider(TestCloakSession session) {
        if (provider == null) {
            ProviderFactory factory = getProviderFactory();
            factory.init(scope);
            this.provider = factory.create(session);
        }
        return this.provider;
    }

    public void setConfig(Config.Scope scope) {
        this.scope = scope;
    }

    public <T extends Provider> boolean supports(Class<T> providerClass) {
        if (this.providerClass == null) {
            try {
                Method method = providerFactoryClass.getMethod("create", KeycloakSession.class);
                this.providerClass = (Class<T>) method.getReturnType();
            } catch (NoSuchMethodException e) {
                throw new TestCloakException(e);
            }
        }
        return providerClass.isAssignableFrom(this.providerClass);
    }

}
