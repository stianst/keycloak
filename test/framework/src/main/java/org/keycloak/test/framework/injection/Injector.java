package org.keycloak.test.framework.injection;

import org.keycloak.test.framework.AbstractKeycloakExtension;
import org.keycloak.test.framework.KeycloakExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class Injector {

    private final List<InjectionProvider> injectors;

    public Injector(AbstractKeycloakExtension extension) {
        injectors = new LinkedList<>();
        injectors.add(new KeycloakAdminClientProvider(extension));
        injectors.add(new KeycloakServerUrlProvider(extension));
    }

    public void inject(Object o) throws IllegalAccessException {
        for (Field f : o.getClass().getDeclaredFields()) {
            for (InjectionProvider i : injectors) {
                Annotation annotation = f.getAnnotation(i.getAnnotation());
                if (annotation != null) {
                    f.setAccessible(true);
                    f.set(o, i.getSupplier().get());
                }
            }
        }
    }

}
