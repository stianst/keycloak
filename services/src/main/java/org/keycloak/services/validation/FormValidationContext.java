package org.keycloak.services.validation;

import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.profile.ValidationContext;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by st on 09/03/17.
 */
public class FormValidationContext implements ValidationContext {

    private KeycloakSession session;
    private UserModel current;
    private UserModel updated;
    private List<FormMessage> errors;

    public FormValidationContext(KeycloakSession session, UserModel current, MultivaluedMap<String, String> formData) {
        this.session = session;
        this.current = current;
        this.updated = new FormUserAdapter(current, formData);
    }

    @Override
    public boolean isRegistration() {
        return false;
    }

    @Override
    public KeycloakSession getSession() {
        return session;
    }

    @Override
    public RealmModel getRealm() {
        return session.getContext().getRealm();
    }

    @Override
    public UserModel getCurrent() {
        return current;
    }

    @Override
    public UserModel getUpdated() {
        return updated;
    }

    @Override
    public void error(String field, String message, Object... parameters) {
        if (errors == null) {
            errors = new LinkedList<>();
        }

        errors.add(new FormMessage(field, message, parameters));
    }

    public List<FormMessage> getErrors() {
        return errors;
    }

    class FormUserAdapter extends ValidationUserAdapter {

        private final MultivaluedMap<String, String> formData;

        private static final String USERNAME = "username";
        private static final String EMAIL = "email";
        private static final String FIRST_NAME = "firstName";
        private static final String LAST_NAME = "lastName";
        private final Map<String, List<String>> attributes;

        public FormUserAdapter(UserModel user, MultivaluedMap<String, String> formData) {
            super(user);
            this.formData = formData;
            this.attributes = user.getAttributes();

            for (String key : formData.keySet()) {
                if (!key.startsWith(Constants.USER_ATTRIBUTES_PREFIX)) continue;
                String attribute = key.substring(Constants.USER_ATTRIBUTES_PREFIX.length());

                // Need to handle case when attribute has multiple values, but in UI was displayed just first value
                List<String> modelVal = user.getAttribute(attribute);
                List<String> modelValue = modelVal==null ? new ArrayList<String>() : new ArrayList<>(modelVal);

                int index = 0;
                for (String value : formData.get(key)) {
                    if (modelValue.size() > index) {
                        modelValue.set(index, value);
                    } else {
                        modelValue.add(value);
                    }
                    index++;
                }

                attributes.put(attribute, modelValue);
            }
        }

        @Override
        public String getUsername() {
            String username = formData.getFirst(USERNAME);
            return username != null && !username.isEmpty() ? username : user.getUsername();
        }

        @Override
        public String getEmail() {
            String email = formData.getFirst(EMAIL);
            return email != null && !email.isEmpty() ? email : user.getEmail();
        }

        @Override
        public String getFirstName() {
            String firstName = formData.getFirst(FIRST_NAME);
            return firstName != null && !firstName.isEmpty() ? firstName : user.getFirstName();
        }

        @Override
        public String getLastName() {
            String lastName = formData.getFirst(LAST_NAME);
            return lastName != null && !lastName.isEmpty() ? lastName : user.getLastName();
        }

        @Override
        public String getFirstAttribute(String name) {
            List<String> list = getAttribute(name);
            return list != null && !list.isEmpty() ? list.get(0) : null;
        }

        @Override
        public List<String> getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public Map<String, List<String>> getAttributes() {
            return attributes;
        }
    }

}
