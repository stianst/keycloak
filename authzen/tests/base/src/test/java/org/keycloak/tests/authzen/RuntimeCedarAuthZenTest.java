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
package org.keycloak.tests.authzen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.core.Response;

import org.keycloak.admin.client.resource.AuthorizationResource;
import org.keycloak.common.Profile.Feature;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourcePermissionRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.keycloak.testframework.annotations.InjectClient;
import org.keycloak.testframework.annotations.InjectRealm;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.annotations.TestSetup;
import org.keycloak.testframework.authzen.client.AuthZenClient;
import org.keycloak.testframework.authzen.client.AuthZenClient.EvaluationResult;
import org.keycloak.testframework.authzen.client.annotations.InjectAuthZenClient;
import org.keycloak.testframework.oauth.OAuthClient;
import org.keycloak.testframework.oauth.annotations.InjectOAuthClient;
import org.keycloak.testframework.realm.ClientBuilder;
import org.keycloak.testframework.realm.ClientConfig;
import org.keycloak.testframework.realm.ManagedClient;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.realm.RealmBuilder;
import org.keycloak.testframework.realm.RealmConfig;
import org.keycloak.testframework.realm.UserBuilder;
import org.keycloak.testframework.server.KeycloakServerConfig;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;
import org.keycloak.testsuite.util.oauth.AccessTokenResponse;

import org.junit.jupiter.api.Test;

import static org.keycloak.authorization.authzen.AuthZen.SubjectType.USER;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@KeycloakIntegrationTest(config = RuntimeCedarAuthZenTest.RuntimeCedarAuthZenServerConfig.class)
public class RuntimeCedarAuthZenTest {

    private static final String ADMIN_USER = "cedar-admin";
    private static final String REGULAR_USER = "cedar-regular";

    private static final String CEDAR_ROLE_BASED_GRANT = """
            permit(
                principal,
                action == Keycloak::Action::"read",
                resource
            ) when {
                principal has kc_realm_roles && principal.kc_realm_roles == "admin"
            };
            """;

    private static final String CEDAR_GRANT_ALL = """
            permit(
                principal,
                action,
                resource
            );
            """;

    private static final String CEDAR_DENY_ALL = """
            forbid(
                principal,
                action,
                resource
            );
            """;

    @InjectRealm(config = TestRealmConfig.class)
    ManagedRealm realm;

    @InjectClient(ref = "cedar-authzen-client", config = AuthzClientConfig.class)
    ManagedClient client;

    @InjectOAuthClient
    OAuthClient oauth;

    @InjectAuthZenClient
    AuthZenClient authZenClient;

    @TestSetup
    public void setup() {
        configureAuthorizationResources();
    }

    @Test
    public void testRuntimeCedarPolicyGrantsAccessViaAuthZen() throws IOException {
        EvaluationResult result = authzenClient()
                .evaluate(AuthZenClient.evaluationRequest()
                        .subject(USER, ADMIN_USER)
                        .action("read")
                        .resource("endpoint", "/cedar-protected")
                        .build());

        assertEquals(200, result.statusCode());
        assertTrue(result.decision());
    }

    @Test
    public void testRuntimeCedarPolicyDeniesUnauthorizedUser() throws IOException {
        EvaluationResult result = authzenClient()
                .evaluate(AuthZenClient.evaluationRequest()
                        .subject(USER, REGULAR_USER)
                        .action("read")
                        .resource("endpoint", "/cedar-protected")
                        .build());

        assertEquals(200, result.statusCode());
        assertFalse(result.decision());
    }

    @Test
    public void testRuntimeCedarPolicyDeniesWrongAction() throws IOException {
        EvaluationResult result = authzenClient()
                .evaluate(AuthZenClient.evaluationRequest()
                        .subject(USER, ADMIN_USER)
                        .action("write")
                        .resource("endpoint", "/cedar-protected")
                        .build());

        assertEquals(200, result.statusCode());
        assertFalse(result.decision());
    }

    @Test
    public void testRuntimeCedarGrantAllPolicy() throws IOException {
        EvaluationResult result = authzenClient()
                .evaluate(AuthZenClient.evaluationRequest()
                        .subject(USER, REGULAR_USER)
                        .action("read")
                        .resource("endpoint", "/cedar-open")
                        .build());

        assertEquals(200, result.statusCode());
        assertTrue(result.decision());
    }

    @Test
    public void testUpdatedRuntimeCedarPolicyReflectedInAuthZen() throws IOException {
        EvaluationResult permitResult = authzenClient()
                .evaluate(AuthZenClient.evaluationRequest()
                        .subject(USER, REGULAR_USER)
                        .action("read")
                        .resource("endpoint", "/cedar-mutable")
                        .build());

        assertEquals(200, permitResult.statusCode());
        assertTrue(permitResult.decision());

        AuthorizationResource authz = client.admin().authorization();
        PolicyRepresentation mutablePolicy = authz.policies().findByName("Cedar Mutable Policy");

        PolicyRepresentation update = new PolicyRepresentation();
        update.setId(mutablePolicy.getId());
        update.setName("Cedar Mutable Policy");
        update.setType("cedar");
        Map<String, String> config = new HashMap<>();
        config.put("code", CEDAR_DENY_ALL);
        update.setConfig(config);
        authz.policies().policy(mutablePolicy.getId()).update(update);

        EvaluationResult denyResult = authzenClient()
                .evaluate(AuthZenClient.evaluationRequest()
                        .subject(USER, REGULAR_USER)
                        .action("read")
                        .resource("endpoint", "/cedar-mutable")
                        .build());

        assertEquals(200, denyResult.statusCode());
        assertFalse(denyResult.decision());
    }

    @Test
    public void testUnknownResourceDenied() throws IOException {
        EvaluationResult result = authzenClient()
                .evaluate(AuthZenClient.evaluationRequest()
                        .subject(USER, ADMIN_USER)
                        .action("read")
                        .resource("endpoint", "/nonexistent")
                        .build());

        assertEquals(200, result.statusCode());
        assertFalse(result.decision());
    }

    private void configureAuthorizationResources() {
        AuthorizationResource authz = client.admin().authorization();

        createScope(authz, "read");
        createScope(authz, "write");

        String roleBasedPolicyId = createRuntimeCedarPolicy(authz, "Cedar Role Policy", CEDAR_ROLE_BASED_GRANT);
        String grantAllPolicyId = createRuntimeCedarPolicy(authz, "Cedar Open Policy", CEDAR_GRANT_ALL);
        String mutablePolicyId = createRuntimeCedarPolicy(authz, "Cedar Mutable Policy", CEDAR_GRANT_ALL);

        createResource(authz, "/cedar-protected", "endpoint", "read", "write");
        createResourcePermission(authz, "Cedar Protected Permission", "/cedar-protected", roleBasedPolicyId);

        createResource(authz, "/cedar-open", "endpoint", "read");
        createResourcePermission(authz, "Cedar Open Permission", "/cedar-open", grantAllPolicyId);

        createResource(authz, "/cedar-mutable", "endpoint", "read");
        createResourcePermission(authz, "Cedar Mutable Permission", "/cedar-mutable", mutablePolicyId);
    }

    private static String createRuntimeCedarPolicy(AuthorizationResource authz, String name, String code) {
        PolicyRepresentation policy = new PolicyRepresentation();
        policy.setName(name);
        policy.setType("cedar");

        Map<String, String> config = new HashMap<>();
        config.put("code", code);
        policy.setConfig(config);

        try (Response response = authz.policies().create(policy)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
        return authz.policies().findByName(name).getId();
    }

    private static void createScope(AuthorizationResource authz, String name) {
        try (Response response = authz.scopes().create(new ScopeRepresentation(name))) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
    }

    private static void createResource(AuthorizationResource authz, String name, String type, String... scopes) {
        ResourceRepresentation resource = new ResourceRepresentation();
        resource.setName(name);
        resource.setType(type);
        resource.addScope(scopes);
        try (Response response = authz.resources().create(resource)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
    }

    private static void createResourcePermission(AuthorizationResource authz, String name,
                                                  String resourceName, String policyId) {
        ResourcePermissionRepresentation permission = ResourcePermissionRepresentation.create()
                .name(name)
                .resources(Set.of(authz.resources().findByName(resourceName).get(0).getId()))
                .policies(Set.of(policyId))
                .build();
        try (Response response = authz.permissions().resource().create(permission)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
    }

    private AuthZenClient.Authenticated authzenClient() {
        AccessTokenResponse tokenResponse = oauth
                .client(client.getClientId(), client.getSecret())
                .doClientCredentialsGrantAccessTokenRequest();
        return authZenClient.withAccessToken(tokenResponse.getAccessToken());
    }

    public static class RuntimeCedarAuthZenServerConfig implements KeycloakServerConfig {
        @Override
        public KeycloakServerConfigBuilder configure(KeycloakServerConfigBuilder config) {
            return config.features(Feature.AUTHZEN, Feature.CEDAR_POLICIES);
        }
    }

    public static class TestRealmConfig implements RealmConfig {
        @Override
        public RealmBuilder configure(RealmBuilder realm) {
            realm.realmRoles("admin");

            realm.users(UserBuilder.create("cedar-admin")
                    .username(ADMIN_USER)
                    .name("Cedar", "Admin")
                    .email("cedar-admin@localhost")
                    .password("password")
                    .realmRoles("admin"));

            realm.users(UserBuilder.create("cedar-regular")
                    .username(REGULAR_USER)
                    .name("Cedar", "Regular")
                    .email("cedar-regular@localhost")
                    .password("password"));

            return realm;
        }
    }

    public static class AuthzClientConfig implements ClientConfig {
        @Override
        public ClientBuilder configure(ClientBuilder client) {
            return client
                    .secret("secret")
                    .directAccessGrantsEnabled(true)
                    .authorizationServicesEnabled(true);
        }
    }
}
