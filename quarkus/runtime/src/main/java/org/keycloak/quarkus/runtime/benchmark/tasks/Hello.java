package org.keycloak.quarkus.runtime.benchmark.tasks;

import jakarta.ws.rs.core.MediaType;

public class Hello implements BenchmarkTask {
    @Override
    public Object run() {
        return "Hello";
    }

    @Override
    public MediaType mediaType() {
        return MediaType.TEXT_PLAIN_TYPE;
    }
}
