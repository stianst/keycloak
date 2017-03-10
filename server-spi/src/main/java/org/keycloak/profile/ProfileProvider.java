package org.keycloak.profile;

import org.keycloak.provider.Provider;

/**
 * Created by st on 09/03/17.
 */
public interface ProfileProvider extends Provider {

    void validate(ValidationContext context);

}
