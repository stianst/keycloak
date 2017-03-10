package org.keycloak.profile;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;

import java.util.regex.Pattern;

/**
 * Created by st on 09/03/17.
 */
public class DummyProfileProvider implements ProfileProvider {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9!#$%&'*+/=?^_`{|}~.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*");

    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_LAST_NAME = "lastName";
    private static final String FIELD_FIRST_NAME = "firstName";
    private static final String FIELD_USERNAME = "username";

    @Override
    public void validate(ValidationContext context) {
        RealmModel realm = context.getRealm();
        UserModel user = context.getUpdated();

        if (!realm.isRegistrationEmailAsUsername() && isBlank(user.getUsername())) {
            context.error(FIELD_USERNAME, Messages.MISSING_USERNAME);
        }

        if (isBlank(user.getFirstName())) {
            context.error(FIELD_FIRST_NAME, Messages.MISSING_FIRST_NAME);
        }

        if (isBlank(user.getLastName())) {
            context.error(FIELD_LAST_NAME, Messages.MISSING_LAST_NAME);
        }

        if (isBlank(user.getEmail())) {
            context.error(FIELD_EMAIL, Messages.MISSING_EMAIL);
        } else if (!isEmailValid(user.getEmail())) {
            context.error(FIELD_EMAIL, Messages.INVALID_EMAIL);
        }
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
