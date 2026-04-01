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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;

import org.keycloak.admin.client.resource.AuthorizationResource;
import org.keycloak.representations.idm.authorization.DecisionEffect;
import org.keycloak.representations.idm.authorization.PolicyEvaluationRequest;
import org.keycloak.representations.idm.authorization.PolicyEvaluationResponse;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourcePermissionRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.keycloak.testframework.annotations.InjectClient;
import org.keycloak.testframework.annotations.InjectRealm;
import org.keycloak.testframework.annotations.InjectUser;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.realm.ManagedClient;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.realm.ManagedUser;
import org.keycloak.tests.authz.services.config.DefaultResourceServerConfig;
import org.keycloak.tests.authz.services.config.RuntimeCedarPolicyServerConfig;
import org.keycloak.tests.common.BasicUserConfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@KeycloakIntegrationTest(config = RuntimeCedarPolicyServerConfig.class)
public class RuntimeCedarPolicyCrudTest {

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

    private static final String CEDAR_GRANT_VIEW = """
            permit(
                principal,
                action == Keycloak::Action::"view",
                resource
            );
            """;

    @InjectRealm
    ManagedRealm realm;

    @InjectUser(config = BasicUserConfig.class)
    ManagedUser user;

    @InjectClient(config = DefaultResourceServerConfig.class, ref = "runtime-cedar-server")
    ManagedClient resourceServer;

    @Test
    public void testCreateRuntimeCedarPolicy() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        PolicyRepresentation policy = createRuntimeCedarPolicy(authorization, "Create Test Policy", CEDAR_GRANT_ALL);
        assertNotNull(policy.getId());
        assertEquals("Create Test Policy", policy.getName());
        assertEquals("cedar", policy.getType());
    }

    @Test
    public void testReadRuntimeCedarPolicy() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        PolicyRepresentation created = createRuntimeCedarPolicy(authorization, "Read Test Policy", CEDAR_GRANT_ALL);

        PolicyRepresentation fetched = authorization.policies().policy(created.getId()).toRepresentation();
        assertEquals(created.getId(), fetched.getId());
        assertEquals("Read Test Policy", fetched.getName());
        assertEquals("cedar", fetched.getType());
        assertEquals(CEDAR_GRANT_ALL, fetched.getConfig().get("code"));
        assertEquals("true", fetched.getConfig().get("runtimeDeployed"));
    }

    @Test
    public void testUpdateRuntimeCedarPolicy() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        PolicyRepresentation created = createRuntimeCedarPolicy(authorization, "Update Test Policy", CEDAR_GRANT_ALL);

        PolicyRepresentation update = new PolicyRepresentation();
        update.setId(created.getId());
        update.setName("Update Test Policy");
        update.setType("cedar");
        Map<String, String> config = new HashMap<>();
        config.put("code", CEDAR_DENY_ALL);
        update.setConfig(config);

        authorization.policies().policy(created.getId()).update(update);

        PolicyRepresentation fetched = authorization.policies().policy(created.getId()).toRepresentation();
        assertEquals(CEDAR_DENY_ALL, fetched.getConfig().get("code"));
    }

    @Test
    public void testDeleteRuntimeCedarPolicy() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        PolicyRepresentation created = createRuntimeCedarPolicy(authorization, "Delete Test Policy", CEDAR_GRANT_ALL);
        String policyId = created.getId();

        PolicyRepresentation fetched = authorization.policies().policy(policyId).toRepresentation();
        assertNotNull(fetched);

        authorization.policies().policy(policyId).remove();

        PolicyRepresentation afterDelete = authorization.policies().findByName("Delete Test Policy");
        assertNull(afterDelete);
    }

    @Test
    public void testCreatePolicyWithInvalidSyntaxRejected() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        PolicyRepresentation policy = new PolicyRepresentation();
        policy.setName("Invalid Syntax Policy");
        policy.setType("cedar");

        Map<String, String> config = new HashMap<>();
        config.put("code", "this is not valid cedar syntax {{{");
        policy.setConfig(config);

        try (Response response = authorization.policies().create(policy)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testCreatePolicyWithEmptyCodeRejected() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        PolicyRepresentation policy = new PolicyRepresentation();
        policy.setName("Empty Code Policy");
        policy.setType("cedar");

        Map<String, String> config = new HashMap<>();
        config.put("code", "");
        policy.setConfig(config);

        try (Response response = authorization.policies().create(policy)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testCreatePolicyWithOversizedCodeRejected() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        String oversizedCode = "permit(principal, action, resource);".repeat(3000);
        assertTrue(oversizedCode.length() > 65_536);

        PolicyRepresentation policy = new PolicyRepresentation();
        policy.setName("Oversized Code Policy");
        policy.setType("cedar");

        Map<String, String> config = new HashMap<>();
        config.put("code", oversizedCode);
        policy.setConfig(config);

        try (Response response = authorization.policies().create(policy)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testCreateDuplicateNameRejected() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createRuntimeCedarPolicy(authorization, "Duplicate Name Policy", CEDAR_GRANT_ALL);

        PolicyRepresentation duplicate = new PolicyRepresentation();
        duplicate.setName("Duplicate Name Policy");
        duplicate.setType("cedar");

        Map<String, String> config = new HashMap<>();
        config.put("code", CEDAR_DENY_ALL);
        duplicate.setConfig(config);

        try (Response response = authorization.policies().create(duplicate)) {
            assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testRuntimePolicyEvaluatesCorrectly() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createScope(authorization, "view");
        PolicyRepresentation grantPolicy = createRuntimeCedarPolicy(authorization, "Eval Grant Policy", CEDAR_GRANT_VIEW);
        createResource(authorization, "Eval Resource", "view");

        ResourcePermissionRepresentation permission = new ResourcePermissionRepresentation();
        permission.setName("Eval Permission");
        permission.addResource("Eval Resource");
        permission.addPolicy(grantPolicy.getName());

        try (Response response = authorization.permissions().resource().create(permission)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }

        PolicyEvaluationResponse result = evaluate(authorization, user, "Eval Resource", "view");
        assertEquals(DecisionEffect.PERMIT, result.getStatus());
    }

    @Test
    public void testUpdatedPolicyChangesDecision() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createScope(authorization, "access");
        PolicyRepresentation policy = createRuntimeCedarPolicy(authorization, "Flip Policy", CEDAR_GRANT_ALL);
        createResource(authorization, "Flip Resource", "access");

        ResourcePermissionRepresentation permission = new ResourcePermissionRepresentation();
        permission.setName("Flip Permission");
        permission.addResource("Flip Resource");
        permission.addPolicy(policy.getName());

        try (Response response = authorization.permissions().resource().create(permission)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }

        PolicyEvaluationResponse permitResult = evaluate(authorization, user, "Flip Resource", "access");
        assertEquals(DecisionEffect.PERMIT, permitResult.getStatus());

        PolicyRepresentation update = new PolicyRepresentation();
        update.setId(policy.getId());
        update.setName("Flip Policy");
        update.setType("cedar");
        Map<String, String> config = new HashMap<>();
        config.put("code", CEDAR_DENY_ALL);
        update.setConfig(config);
        authorization.policies().policy(policy.getId()).update(update);

        PolicyEvaluationResponse denyResult = evaluate(authorization, user, "Flip Resource", "access");
        assertEquals(DecisionEffect.DENY, denyResult.getStatus());
    }

    @Test
    public void testListPoliciesByType() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createRuntimeCedarPolicy(authorization, "List Policy A", CEDAR_GRANT_ALL);
        createRuntimeCedarPolicy(authorization, "List Policy B", CEDAR_DENY_ALL);

        List<PolicyRepresentation> cedarPolicies = authorization.policies()
                .policies(null, null, "cedar", null, null, null, null, null, null, null);

        assertTrue(cedarPolicies.size() >= 2);
        assertTrue(cedarPolicies.stream().anyMatch(p -> "List Policy A".equals(p.getName())));
        assertTrue(cedarPolicies.stream().anyMatch(p -> "List Policy B".equals(p.getName())));
    }

    private PolicyRepresentation createRuntimeCedarPolicy(AuthorizationResource authorization, String name, String code) {
        PolicyRepresentation policy = new PolicyRepresentation();
        policy.setName(name);
        policy.setType("cedar");

        Map<String, String> config = new HashMap<>();
        config.put("code", code);
        policy.setConfig(config);

        try (Response response = authorization.policies().create(policy)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }

        PolicyRepresentation created = authorization.policies().findByName(name);
        assertNotNull(created);
        return created;
    }

    private PolicyEvaluationResponse evaluate(AuthorizationResource authorization, ManagedUser evaluatedUser,
                                              String resourceName, String... scopes) {
        PolicyEvaluationRequest request = new PolicyEvaluationRequest();
        request.setUserId(evaluatedUser.admin().toRepresentation().getId());
        request.addResource(resourceName, scopes);
        return authorization.policies().evaluate(request);
    }

    private void createScope(AuthorizationResource authorization, String name) {
        if (authorization.scopes().findByName(name) != null) {
            return;
        }
        try (Response response = authorization.scopes().create(new ScopeRepresentation(name))) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
    }

    private void createResource(AuthorizationResource authorization, String name, String... scopes) {
        ResourceRepresentation resource = new ResourceRepresentation(name, scopes);
        try (Response response = authorization.resources().create(resource)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
    }
}
