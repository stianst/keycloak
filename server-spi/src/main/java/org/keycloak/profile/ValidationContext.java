package org.keycloak.profile;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * Created by st on 09/03/17.
 */
public interface ValidationContext {

    boolean isRegistration();

    KeycloakSession getSession();

    RealmModel getRealm();

    UserModel getCurrent();

    UserModel getUpdated();

    void error(String field, String message, Object... parameters);

}
