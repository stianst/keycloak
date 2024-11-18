package org.keycloak.test.framework.realm;

import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;

import java.util.Arrays;

public class RealmConfigBuilder {

    private final RealmRepresentation representation;

    private RealmConfigBuilder(RealmRepresentation rep) {
        this.representation = rep;
    }

    public static RealmConfigBuilder create() {
        RealmRepresentation rep = new RealmRepresentation();
        rep.setEnabled(true);
        return new RealmConfigBuilder(rep);
    }

    public static RealmConfigBuilder update(RealmRepresentation rep) {

        return new RealmConfigBuilder(rep);
    }

    public RealmConfigBuilder name(String name) {
        representation.setRealm(name);
        return this;
    }

    public RealmConfigBuilder registrationEmailAsUsername(boolean registrationEmailAsUsername) {
        representation.setRegistrationEmailAsUsername(registrationEmailAsUsername);
        return this;
    }

    public RealmConfigBuilder roles(String... roleNames) {
        if (representation.getRoles() == null) {
            representation.setRoles(new RolesRepresentation());
        }
        representation.getRoles().setRealm(Collections.combine(representation.getRoles().getRealm(), Arrays.stream(roleNames).map(Representations::toRole)));
        return this;
    }

    public RealmConfigBuilder groups(String... groupsNames) {
        representation.setGroups(Collections.combine(representation.getGroups(), Arrays.stream(groupsNames).map(Representations::toGroup)));
        return this;
    }

    public RealmRepresentation build() {
        return representation;
    }

}
