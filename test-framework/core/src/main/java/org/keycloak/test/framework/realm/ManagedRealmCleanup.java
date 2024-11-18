package org.keycloak.test.framework.realm;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.LinkedList;
import java.util.List;

public class ManagedRealmCleanup {

    private final List<RealmCleanup> cleanupTasks = new LinkedList<>();

    public ManagedRealmCleanup add(RealmCleanup realmCleanup) {
        this.cleanupTasks.add(realmCleanup);
        return this;
    }

    public ManagedRealmCleanup deleteUsers() {
        return add(r -> r.users().list().forEach(u -> r.users().delete(u.getId()).close()));
    }

    void resetToOriginalRepresentation(RealmRepresentation originalRepresentation) {
        if (cleanupTasks.stream().noneMatch(c -> c instanceof ResetRealm)) {
            cleanupTasks.add(new ResetRealm(originalRepresentation));
        }
    }

    void runCleanupTasks(RealmResource realm) {
        cleanupTasks.forEach(t -> t.cleanup(realm));
        cleanupTasks.clear();
    }

    public interface RealmCleanup {

        void cleanup(RealmResource realm);

    }

    private record ResetRealm(RealmRepresentation originalRepresentation) implements RealmCleanup {

        private ResetRealm(RealmRepresentation originalRepresentation) {
            this.originalRepresentation = RepresentationUtils.clone(originalRepresentation);
        }

        @Override
        public void cleanup(RealmResource realm) {
            realm.update(originalRepresentation);
        }

    }

}
