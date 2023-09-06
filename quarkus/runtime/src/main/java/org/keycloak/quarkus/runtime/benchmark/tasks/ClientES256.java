package org.keycloak.quarkus.runtime.benchmark.tasks;

import org.keycloak.crypto.Algorithm;

public class ClientES256 extends AbstractClient {
    @Override
    public String getClientId() {
        return "hello-es256";
    }

    @Override
    public String getAlgorithm() {
        return Algorithm.ES256;
    }
}
