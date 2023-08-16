package org.keycloak;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class BasicParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> aClass, Type type, Annotation[] annotations) {
        if (aClass.isAssignableFrom(Boolean.class)) {
            return (ParamConverter<T>) new BooleanParamConverter();
        }
        if (aClass.isAssignableFrom(Integer.class)) {
            return (ParamConverter<T>) new IntegerParamConverter();
        }
        return null;
    }
}
