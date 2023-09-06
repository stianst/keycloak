package org.keycloak.quarkus.runtime.benchmark.tasks;

import jakarta.ws.rs.core.MediaType;
import org.keycloak.models.KeycloakSession;

public interface BenchmarkTask {

    default void init(KeycloakSession session) {
    }

    Object run();

    MediaType mediaType();

    default void close() {
    }

}
