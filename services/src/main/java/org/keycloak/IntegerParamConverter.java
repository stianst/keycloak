package org.keycloak;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IntegerParamConverter implements ParamConverter<Integer> {
    @Override
    public Integer fromString(String s) {
        return Integer.valueOf(s);
    }

    @Override
    public String toString(Integer integer) {
        return integer.toString();
    }
}
