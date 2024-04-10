package org.keycloak.test.framework.injection;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

public interface InjectionProvider {

    <T extends Annotation> Class<T> getAnnotation();

    Supplier<Object> getSupplier();

}
