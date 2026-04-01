/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.tests.authz.services;

import java.util.Map;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.testframework.annotations.InjectAdminClient;
import org.keycloak.testframework.annotations.InjectClient;
import org.keycloak.testframework.annotations.InjectKeycloakUrls;
import org.keycloak.testframework.annotations.InjectRealm;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.realm.ManagedClient;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.server.KeycloakUrls;
import org.keycloak.tests.authz.services.config.DefaultResourceServerConfig;
import org.keycloak.tests.authz.services.config.RuntimeCedarPolicyServerConfig;
import org.keycloak.util.JsonSerialization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Cedar policy creation via raw HTTP POST to the generic policy endpoint,
 * mirroring what {@code kcadm.sh create} does:
 * <pre>
 * kcadm.sh create clients/CLIENT_UUID/authz/resource-server/policy \
 *   -r REALM -s type=cedar -s name=my-policy \
 *   -s 'config={"code":"permit(principal, action, resource);"}'
 * </pre>
 */
@KeycloakIntegrationTest(config = RuntimeCedarPolicyServerConfig.class)
public class CedarPolicyUploadEndpointTest {

    private static final String CEDAR_GRANT_ALL = """
            permit(
                principal,
                action,
                resource
            );
            """;

    @InjectRealm
    ManagedRealm realm;

    @InjectClient(config = DefaultResourceServerConfig.class, ref = "upload-test-server")
    ManagedClient resourceServer;

    @InjectKeycloakUrls
    KeycloakUrls keycloakUrls;

    @InjectAdminClient
    Keycloak adminClient;

    @Test
    public void testUploadCedarPolicy() throws Exception {
        try (Response response = createCedarPolicy("Upload Test Policy", CEDAR_GRANT_ALL)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            String body = response.readEntity(String.class);
            PolicyRepresentation result = JsonSerialization.readValue(body, PolicyRepresentation.class);
            assertNotNull(result.getId());
            assertEquals("Upload Test Policy", result.getName());
            assertEquals("cedar", result.getType());
        }

        PolicyRepresentation fetched = resourceServer.admin().authorization()
                .policies().findByName("Upload Test Policy");
        assertNotNull(fetched);
        assertEquals("cedar", fetched.getType());
        assertEquals(CEDAR_GRANT_ALL, fetched.getConfig().get("code"));
        assertEquals("true", fetched.getConfig().get("runtimeDeployed"));
    }

    @Test
    public void testUploadWithInvalidSyntaxRejected() {
        try (Response response = createCedarPolicy("Invalid Syntax Upload", "this is not valid cedar {{{")) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testUploadWithEmptyCodeRejected() {
        try (Response response = createCedarPolicy("Empty Code Upload", "")) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testUploadWithOversizedCodeRejected() {
        String oversizedCode = "permit(principal, action, resource);".repeat(3000);
        assertTrue(oversizedCode.length() > 65_536);

        try (Response response = createCedarPolicy("Oversized Code Upload", oversizedCode)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testUploadDuplicateNameRejected() {
        try (Response first = createCedarPolicy("Duplicate Upload Policy", CEDAR_GRANT_ALL)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), first.getStatus());
        }

        try (Response second = createCedarPolicy("Duplicate Upload Policy", CEDAR_GRANT_ALL)) {
            assertEquals(Response.Status.CONFLICT.getStatusCode(), second.getStatus());
        }
    }

    private Response createCedarPolicy(String name, String code) {
        String policyUrl = keycloakUrls.getBase() + "/admin/realms/" + realm.getName()
                + "/clients/" + resourceServer.getId()
                + "/authz/resource-server/policy";

        Map<String, Object> payload = Map.of(
                "type", "cedar",
                "name", name,
                "config", Map.of("code", code)
        );

        try (Client httpClient = Keycloak.getClientProvider().newRestEasyClient(null, null, true)) {
            return httpClient.target(policyUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminClient.tokenManager().getAccessTokenString())
                    .post(Entity.json(payload));
        }
    }
}
