package org.keycloak.test.framework.provider;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;

import java.util.Map;

@QuarkusTest
@TestProfile(QuarkusProviderTest.KcTestProfile.class)
public class QuarkusProviderTest {

    @Test
    public void testProvider() {
        QuarkusKeycloakSessionFactory sessionFactory = QuarkusKeycloakSessionFactory.getInstance();
        try (KeycloakSession session = sessionFactory.create()) {
            PasswordHashProvider hashProvider = session.getProvider(PasswordHashProvider.class, "pbkdf2-sha512");
            PasswordCredentialModel mypass = hashProvider.encodedCredential("mypass", 1);
            Assertions.assertNotNull(mypass.getPasswordSecretData().getValue());
        }
    }

    public static final class KcTestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "kc.hostname-strict", "false",
                    "kc.http-enabled", "true",
                    "kc.cache", "local",
                    "kc.db", "dev-mem");
        }
    }

}
