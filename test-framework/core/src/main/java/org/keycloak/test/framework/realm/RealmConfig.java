package org.keycloak.test.framework.realm;

import org.keycloak.representations.idm.RealmRepresentation;

public interface RealmConfig {

    RealmConfigBuilder configure(RealmConfigBuilder realm);

}
