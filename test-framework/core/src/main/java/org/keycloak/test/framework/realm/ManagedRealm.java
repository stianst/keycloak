package org.keycloak.test.framework.realm;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.test.framework.injection.ManagedTestResource;

public class ManagedRealm extends ManagedTestResource {

    private final String baseUrl;
    private final RealmRepresentation createdRepresentation;
    private final RealmResource realmResource;

    private ManagedRealmCleanup realmCleanup;

    public ManagedRealm(String baseUrl, RealmRepresentation createdRepresentation, RealmResource realmResource) {
        this.baseUrl = baseUrl;
        this.createdRepresentation = createdRepresentation;
        this.realmResource = realmResource;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getName() {
        return createdRepresentation.getRealm();
    }

    public RealmResource admin() {
        return realmResource;
    }

    public void updateWithCleanup(RealmUpdate... updates) {
        RealmRepresentation realm = admin().toRepresentation();
        cleanup().resetToOriginalRepresentation(realm);

        RealmConfigBuilder configBuilder = RealmConfigBuilder.update(realm);
        for (RealmUpdate update : updates) {
            configBuilder = update.update(configBuilder);
        }

        admin().update(configBuilder.build());
    }

    public ManagedRealmCleanup cleanup() {
        if (realmCleanup == null) {
            realmCleanup = new ManagedRealmCleanup();
        }
        return realmCleanup;
    }

    @Override
    public void runCleanupTasks() {
        if (realmCleanup != null) {
            realmCleanup.runCleanupTasks(realmResource);
            realmCleanup = null;
        }
    }

    public interface RealmUpdate {

        RealmConfigBuilder update(RealmConfigBuilder realm);

    }

}
