package org.keycloak.test.providers;

import context.TestKeycloakContext;
import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.test.providers.mocks.TestCloakSession;
import org.keycloak.test.providers.utils.ProviderFactoryWrapper;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TestCloakBuilder {

    private ProviderBuilder providerBuilder;
    private FeatureBuilder featureBuilder;

    private ConfigBuilder configBuilder;
    private ContextBuilder contextBuilder;

    public static TestCloakBuilder create() {
        return new TestCloakBuilder();
    }

    public ProviderBuilder providers() {
        if (providerBuilder == null) {
            providerBuilder = new ProviderBuilder(this);
        }
        return providerBuilder;
    }

    public FeatureBuilder features() {
        if (featureBuilder == null) {
            featureBuilder = new FeatureBuilder(this);
        }
        return featureBuilder;
    }

    public ConfigBuilder config() {
        if (configBuilder == null) {
            configBuilder = new ConfigBuilder(this);
        }
        return configBuilder;
    }

    public ContextBuilder context() {
        if (contextBuilder == null) {
            contextBuilder = new ContextBuilder(this);
        }
        return contextBuilder;
    }

    public TestCloakSession build() {
        if (featureBuilder != null) {
            Map<Profile.Feature, Boolean> features = featureBuilder.getFeatures();
            Profile.init(Profile.ProfileName.DEFAULT, features);
        } else {
            Profile.defaults();
        }

        if (configBuilder != null) {
            Properties properties = configBuilder.getProperties();
            Config.init(new Config.PropertiesConfigProvider(properties));
        } else {
            Config.init(new Config.SystemPropertiesConfigProvider());
        }

        TestKeycloakContext context = null;
        if (contextBuilder != null) {
            context = contextBuilder.getContext();
        }

        Set<ProviderFactoryWrapper> factories = providerBuilder != null ? providerBuilder.getFactories() : Collections.emptySet();
        factories.stream().forEach(f -> f.setConfig(Config.scope(f.getProviderFactoryClass().getName())));

        return new TestCloakSession(factories, context);
    }

}
