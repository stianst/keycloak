package org.keycloak.profile;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.util.regex.Pattern;

/**
 * Created by st on 09/03/17.
 */
public class DummyProfileProviderFactory implements ProfileProviderFactory {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9!#$%&'*+/=?^_`{|}~.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*");

    @Override
    public ProfileProvider create(KeycloakSession session) {
        return new DummyProfileProvider(session);
    }

    @Override
    public String getId() {
        return "dummy";
    }

    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    private static boolean isEmailValid(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

}
