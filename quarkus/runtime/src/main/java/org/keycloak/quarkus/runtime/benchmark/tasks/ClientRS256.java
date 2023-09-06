package org.keycloak.quarkus.runtime.benchmark.tasks;

import org.keycloak.crypto.Algorithm;

public class ClientRS256 extends AbstractClient {
    @Override
    public String getClientId() {
        return "hello-rs256";
    }

    @Override
    public String getAlgorithm() {
        return Algorithm.RS256;
    }
}
