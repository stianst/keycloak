package org.keycloak.test.framework.injection;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.Annotation;

public interface InjectionProvider {

    Class<? extends Annotation> getAnnotation();

    Object getValue(ExtensionContext context, Annotation annotation);

    default void afterEach() {}

    default void afterAll() {};
}
