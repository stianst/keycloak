package org.keycloak.test.providers;

import org.keycloak.provider.ProviderFactory;

import java.util.Properties;

public class ConfigBuilder {

    private TestCloakBuilder parent;

    private Properties properties;

    ConfigBuilder(TestCloakBuilder parent) {
        this.parent = parent;
        this.properties = new Properties();
    }

    public TestCloakBuilder set(String key, String value) {
        this.properties.put("keycloak." + key, value);
        return parent;
    }

    public <T extends ProviderFactory> TestCloakBuilder set(Class<T> providerFactoryClass, String key, String value) {
        properties.put("keycloak." + providerFactoryClass.getName() + "." + key, value);
        return parent;
    }

    Properties getProperties() {
        return properties;
    }

}
