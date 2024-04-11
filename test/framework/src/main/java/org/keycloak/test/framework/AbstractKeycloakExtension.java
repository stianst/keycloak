package org.keycloak.test.framework;

import java.util.HashMap;
import java.util.Map;

public class AbstractKeycloakExtension {

    Map<String, Object> resources = new HashMap<>();

    public String getServerUrl() {
        return "http://localhost:8080";
    }

    public <T> T getResource(Class<T> clazz) {
        return (T) resources.get(clazz.getName());
    }

    public <T> T getResource(Class<T> key, Class<T> clazz) {
        return (T) resources.get(key.getName());
    }

    public <T> void putResource(Class<T> key, Object object) {
        resources.put(key.getName(), object);
    }

    public <T> boolean hasResource(Class<T> key) {
        return resources.containsKey(key.getName());
    }

}
