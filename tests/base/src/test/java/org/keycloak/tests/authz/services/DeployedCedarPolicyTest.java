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

import java.util.Collections;

import jakarta.ws.rs.core.Response;

import org.keycloak.admin.client.resource.AuthorizationResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.authorization.DecisionEffect;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
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
import org.keycloak.testframework.remote.runonserver.InjectRunOnServer;
import org.keycloak.testframework.remote.runonserver.RunOnServerClient;
import org.keycloak.tests.authz.services.config.AdminUserConfig;
import org.keycloak.tests.authz.services.config.CedarPolicyServerConfig;
import org.keycloak.tests.authz.services.config.DefaultResourceServerConfig;
import org.keycloak.tests.common.BasicUserConfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for deployed Cedar policies using attribute-based access control (ABAC).
 *
 * <p>The grant policy permits "view" for principals with a localhost email and given_name "First",
 * and permits "delete" only for principals with the "admin" realm role.
 * The deny policy forbids "delete" on resources with "Restricted" in the name unless the principal has the "admin" role.
 */
@KeycloakIntegrationTest(config = CedarPolicyServerConfig.class)
public class DeployedCedarPolicyTest {

    private static final String ADMIN_ROLE = "admin";

    @InjectRealm
    ManagedRealm realm;

    @InjectUser(config = BasicUserConfig.class)
    ManagedUser user;

    @InjectUser(config = AdminUserConfig.class, ref = "admin-user")
    ManagedUser adminUser;

    @InjectClient(config = DefaultResourceServerConfig.class, ref = "cedar-resource-server")
    ManagedClient resourceServer;

    @InjectRunOnServer
    RunOnServerClient runOnServer;

    private boolean adminRoleAssigned = false;

    private void ensureAdminRole() {
        if (adminRoleAssigned) {
            return;
        }
        try {
            realm.admin().roles().get(ADMIN_ROLE).toRepresentation();
        } catch (Exception e) {
            realm.admin().roles().create(new RoleRepresentation(ADMIN_ROLE, "Administrator role", false));
        }
        RoleRepresentation adminRole = realm.admin().roles().get(ADMIN_ROLE).toRepresentation();
        adminUser.admin().roles().realmLevel().add(Collections.singletonList(adminRole));
        adminRoleAssigned = true;
    }

    @Test
    public void testGrantPolicyPermitsViewAction() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createScope(authorization, "view");
        PolicyRepresentation grantPolicy = findOrCreatePolicy(authorization, "Cedar Grant Policy", "script-scripts/cedar-grant-policy.cedar");
        createResource(authorization, "Public Resource", "view");

        ResourcePermissionRepresentation permission = new ResourcePermissionRepresentation();
        permission.setName("View Public Permission");
        permission.addResource("Public Resource");
        permission.addPolicy(grantPolicy.getName());

        try (Response response = authorization.permissions().resource().create(permission)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }

        // basic-user has email basic@localhost and given_name "First" → PERMIT
        PolicyEvaluationResponse result = evaluate(authorization, user, "Public Resource", "view");
        assertEquals(DecisionEffect.PERMIT, result.getStatus());
    }

    @Test
    public void testGrantPolicyDeniesDeleteForNonAdminUser() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createScope(authorization, "delete");
        PolicyRepresentation grantPolicy = findOrCreatePolicy(authorization, "Cedar Grant Policy", "script-scripts/cedar-grant-policy.cedar");
        createResource(authorization, "Delete Target", "delete");

        ResourcePermissionRepresentation permission = new ResourcePermissionRepresentation();
        permission.setName("Delete Target Permission");
        permission.addResource("Delete Target");
        permission.addPolicy(grantPolicy.getName());

        try (Response response = authorization.permissions().resource().create(permission)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }

        // basic-user does not have the "admin" role → DENY
        PolicyEvaluationResponse result = evaluate(authorization, user, "Delete Target", "delete");
        assertEquals(DecisionEffect.DENY, result.getStatus());
    }

    @Test
    public void testGrantPolicyPermitsDeleteForAdminUser() {
        ensureAdminRole();
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createScope(authorization, "delete");
        PolicyRepresentation grantPolicy = findOrCreatePolicy(authorization, "Cedar Grant Policy", "script-scripts/cedar-grant-policy.cedar");
        createResource(authorization, "Admin Delete Target", "delete");

        ResourcePermissionRepresentation permission = new ResourcePermissionRepresentation();
        permission.setName("Admin Delete Permission");
        permission.addResource("Admin Delete Target");
        permission.addPolicy(grantPolicy.getName());

        try (Response response = authorization.permissions().resource().create(permission)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }

        // admin-user has the "admin" realm role → PERMIT
        PolicyEvaluationResponse result = evaluate(authorization, adminUser, "Admin Delete Target", "delete");
        assertEquals(DecisionEffect.PERMIT, result.getStatus());
    }

    @Test
    public void testDenyPolicyForbidsDeleteOnRestrictedResourceForNonAdmin() {
        ensureAdminRole();
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createScope(authorization, "delete");
        PolicyRepresentation grantPolicy = findOrCreatePolicy(authorization, "Cedar Grant Policy", "script-scripts/cedar-grant-policy.cedar");
        PolicyRepresentation denyPolicy = findOrCreatePolicy(authorization, "Cedar Deny Policy", "script-scripts/cedar-deny-policy.cedar");
        createResource(authorization, "Restricted Document", "delete");

        ResourcePermissionRepresentation permission = new ResourcePermissionRepresentation();
        permission.setName("Restricted Delete Permission");
        permission.setDecisionStrategy(DecisionStrategy.AFFIRMATIVE);
        permission.addResource("Restricted Document");
        permission.addPolicy(grantPolicy.getName());
        permission.addPolicy(denyPolicy.getName());

        try (Response response = authorization.permissions().resource().create(permission)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }

        // basic-user: grant policy denies (no admin role), deny policy forbids (Restricted + no admin role) → DENY
        PolicyEvaluationResponse result = evaluate(authorization, user, "Restricted Document", "delete");
        assertEquals(DecisionEffect.DENY, result.getStatus());
    }

    @Test
    public void testDenyPolicyAllowsDeleteOnRestrictedResourceForAdmin() {
        ensureAdminRole();
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createScope(authorization, "delete");
        PolicyRepresentation grantPolicy = findOrCreatePolicy(authorization, "Cedar Grant Policy", "script-scripts/cedar-grant-policy.cedar");
        PolicyRepresentation denyPolicy = findOrCreatePolicy(authorization, "Cedar Deny Policy", "script-scripts/cedar-deny-policy.cedar");
        createResource(authorization, "Restricted Report", "delete");

        // AFFIRMATIVE: the deny policy acts as a veto; if it doesn't forbid, a single permit is enough
        ResourcePermissionRepresentation permission = new ResourcePermissionRepresentation();
        permission.setName("Admin Restricted Delete Permission");
        permission.setDecisionStrategy(DecisionStrategy.AFFIRMATIVE);
        permission.addResource("Restricted Report");
        permission.addPolicy(grantPolicy.getName());
        permission.addPolicy(denyPolicy.getName());

        try (Response response = authorization.permissions().resource().create(permission)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }

        // admin-user: grant policy permits (admin role), deny policy's unless clause matches (admin role) → PERMIT
        PolicyEvaluationResponse result = evaluate(authorization, adminUser, "Restricted Report", "delete");
        assertEquals(DecisionEffect.PERMIT, result.getStatus());
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

    private PolicyRepresentation findOrCreatePolicy(AuthorizationResource authorization, String name, String type) {
        PolicyRepresentation existing = authorization.policies().findByName(name);
        if (existing != null) {
            return existing;
        }
        return createPolicy(authorization, name, type);
    }

    private PolicyRepresentation createPolicy(AuthorizationResource authorization, String name, String type) {
        PolicyRepresentation policy = new PolicyRepresentation();
        policy.setName(name);
        policy.setType(type);
        try (Response response = authorization.policies().create(policy)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }

        PolicyRepresentation created = authorization.policies().findByName(name);
        assertEquals(name, created.getName());
        return created;
    }
}
