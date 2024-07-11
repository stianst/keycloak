package org.keycloak.test.provider;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.cookie.CookieProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;

import java.util.Map;

@QuarkusTest
@TestProfile(QuarkusProviderTest.KcTestProfile.class)
public class QuarkusProviderTest {

    @Test
    public void testProvider() {
        QuarkusKeycloakSessionFactory sessionFactory = QuarkusKeycloakSessionFactory.getInstance();
        try (KeycloakSession session = sessionFactory.create()) {
            session.getContext().setHttpRequest(new DummyHttpRequest());
            CookieProvider cookieProvider = session.getProvider(CookieProvider.class);
            Assertions.assertNotNull(cookieProvider);
        }
    }

    public static final class KcTestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            System.out.println("there");
            return Map.of(
            "kc.hostname-strict", "false",
            "kc.http-enabled", "true",
            "kc.cache", "local");
        }
    }

}
