package org.keycloak.test.provider;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.keycloak.Keycloak;

public class KeycloakTestExtension implements BeforeEachCallback, TestInstanceFactory, TestInstancePreConstructCallback {

    private ClassLoader classLoader;

    public KeycloakTestExtension() {
        classLoader = Keycloak.builder().start("start-dev", "--cache=local").getClassLoader();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
//        System.out.println("here");
//        Keycloak keycloak = Keycloak.builder().start("start-dev", "--cache=local");
//        System.out.println("cl=" + keycloak.getClassLoader());
//        Thread.currentThread().setContextClassLoader(keycloak.getClassLoader());
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) throws TestInstantiationException {
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            Class<?> aClass = classLoader.loadClass(factoryContext.getTestClass().getName());
            return aClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        return ReflectionSupport.newInstance(factoryContext.getTestClass());
    }

    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) throws Exception {
        Thread.currentThread().setContextClassLoader(classLoader);
    }
}
