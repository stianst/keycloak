package org.keycloak.test.providers;

import org.keycloak.models.ClientModel;
import org.keycloak.test.providers.mocks.TestClientModel;

import java.util.HashMap;

public class ClientBuilder {

    private TestClientModel client;

    public static ClientBuilder standard() {
        return create("myclient");
    }

    public static ClientBuilder create(String clientId) {
        return new ClientBuilder(clientId);
    }

    ClientBuilder(String clientId) {
        client = new TestClientModel();
        client.clientId = clientId;
        client.attributes = new HashMap<>();
    }

    public ClientBuilder attribute(String name, String value) {
        client.attributes.put(name, value);
        return this;
    }

    public ClientModel build() {
        return client;
    }

}
