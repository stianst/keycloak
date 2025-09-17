package org.keycloak.tests.client.authentication.external;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.broker.spiffe.SpiffeIdentityProviderConfig;
import org.keycloak.broker.spiffe.SpiffeIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.testframework.annotations.InjectAdminClient;
import org.keycloak.testframework.annotations.InjectHttpClient;
import org.keycloak.testframework.annotations.InjectKeycloakUrls;
import org.keycloak.testframework.annotations.InjectRealm;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.server.KeycloakUrls;
import org.keycloak.testsuite.util.IdentityProviderBuilder;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@KeycloakIntegrationTest(config = SpiffeClientAuthTest.SpiffeServerConfig.class)
public class SpiffeIdentityProviderConfigTest {

    @InjectKeycloakUrls
    KeycloakUrls urls;

    @InjectRealm
    protected ManagedRealm realm;

    @InjectAdminClient
    Keycloak adminClient;

    @InjectHttpClient
    HttpClient httpClient;

    @Test
    public void testValidConfig() {
        testConfig("testValidConfig", "spiffe://trust-domain", "https://localhost", 201);
    }

    @Test
    public void testReturnedConfig() throws IOException {
        testConfig("testReturnedConfig", "spiffe://trust-domain", "https://localhost", 201);

        String accessToken = adminClient.tokenManager().getAccessToken().getToken();

        URI uri = urls.getAdminBuilder().path("/realms/{realm}/identity-provider/instances/testReturnedConfig").build(realm.getName());
        HttpGet httpGet = new HttpGet(uri);
        httpGet.addHeader(HttpHeaders.ACCEPT, "application/json");
        httpGet.addHeader(HttpHeaders.AUTHORIZATION, "bearer " + accessToken);

        HttpResponse response = httpClient.execute(httpGet);
        Assertions.assertEquals(200, response.getStatusLine().getStatusCode());

        JsonNode jsonNode = JsonSerialization.readValue(response.getEntity().getContent(), JsonNode.class);

        Assertions.assertFalse(jsonNode.has("trustEmail"));

        Set<String> fields = new HashSet<>();
        jsonNode.fieldNames().forEachRemaining(fields::add);

        Set<String> permitted = Set.of("alias", "internalId", "providerId", "enabled", "updateProfileFirstLoginMode", "hideOnLogin", "config");
        Assertions.assertEquals(permitted, fields);

    }

    @Test
    public void testInvalidTrustDomain() {
        testConfig("testInvalidTrustDomain1", "spiffe://with-port:8080", "https://localhost", 400);
        testConfig("testInvalidTrustDomain2", "spiffe://trust-domain/with-workload", "https://localhost", 400);
    }

    @Test
    public void testBundleEndpoint() {
        testConfig("testBundleEndpoint1", "spiffe://trust-domain", "invalid-url", 400);
        testConfig("testBundleEndpoint2", "spiffe://trust-domain", "spiffe://test", 400);
    }

    private void testConfig(String alias, String trustDomain, String bundleEndpoint, int expectedStatusCode) {
        IdentityProviderRepresentation idp = IdentityProviderBuilder.create().providerId(SpiffeIdentityProviderFactory.PROVIDER_ID)
                .alias(alias)
                .setAttribute(IdentityProviderModel.ISSUER, trustDomain)
                .setAttribute(SpiffeIdentityProviderConfig.BUNDLE_ENDPOINT_KEY, bundleEndpoint).build();

        try (Response r = realm.admin().identityProviders().create(idp)) {
            Assertions.assertEquals(expectedStatusCode, r.getStatus());
        }
    }

}
