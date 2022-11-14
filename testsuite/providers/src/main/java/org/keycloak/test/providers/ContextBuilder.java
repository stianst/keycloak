package org.keycloak.test.providers;

import context.TestKeycloakContext;

public class ContextBuilder {

    private TestCloakBuilder parent;

    private RealmBuilder realmBuilder;

    private ClientBuilder clientBuilder;

    ContextBuilder(TestCloakBuilder parent) {
        this.parent = parent;
    }

    public TestCloakBuilder realm(RealmBuilder realm) {
        this.realmBuilder = realm;
        return parent;
    }
    public TestCloakBuilder client(ClientBuilder client) {
        this.clientBuilder = client;
        return parent;
    }

    TestKeycloakContext getContext() {
        TestKeycloakContext context = new TestKeycloakContext();
        context.setRealm(realmBuilder.build());
        context.setClient(clientBuilder.build());
        return context;
    }

}
