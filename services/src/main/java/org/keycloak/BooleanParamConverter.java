package org.keycloak;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.Provider;

public class BooleanParamConverter implements ParamConverter<Boolean> {
    @Override
    public Boolean fromString(String s) {
        return Boolean.valueOf(s);
    }

    @Override
    public String toString(Boolean aBoolean) {
        return aBoolean.toString();
    }
}
