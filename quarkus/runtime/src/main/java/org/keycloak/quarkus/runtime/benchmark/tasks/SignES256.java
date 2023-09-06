package org.keycloak.quarkus.runtime.benchmark.tasks;

import org.keycloak.crypto.Algorithm;

public class SignES256 extends AbstractSign {
    @Override
    public String getAlgorithm() {
        return Algorithm.ES256;
    }
}
