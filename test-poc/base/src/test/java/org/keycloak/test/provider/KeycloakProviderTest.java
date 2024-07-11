package org.keycloak.test.provider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.cookie.CookieProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;

@ExtendWith(KeycloakTestExtension.class)
public class KeycloakProviderTest {

    @Test
    public void test() {
        QuarkusKeycloakSessionFactory sessionFactory = QuarkusKeycloakSessionFactory.getInstance();
        try (KeycloakSession session = sessionFactory.create()) {
            session.getContext().setHttpRequest(new DummyHttpRequest());
            CookieProvider cookieProvider = session.getProvider(CookieProvider.class);
            Assertions.assertNotNull(cookieProvider);
        }
    }

}
