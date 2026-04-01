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

import jakarta.ws.rs.core.Response;

import org.keycloak.admin.client.resource.AuthorizationResource;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.keycloak.testframework.annotations.InjectClient;
import org.keycloak.testframework.annotations.InjectRealm;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.realm.ClientBuilder;
import org.keycloak.testframework.realm.ClientConfig;
import org.keycloak.testframework.realm.ManagedClient;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.tests.authz.services.config.CedarPolicyServerConfig;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that Cedar policies are validated against the generated schema during creation.
 *
 * <p>Verifies that valid policies (including those referencing custom protocol mapper attributes)
 * pass schema validation, and invalid policies (referencing non-existent attributes) are rejected.
 */
@KeycloakIntegrationTest(config = CedarPolicyServerConfig.class)
public class CedarPolicySchemaValidationTest {

    @InjectRealm
    ManagedRealm realm;

    @InjectClient(config = CustomMapperResourceServerConfig.class, ref = "cedar-validation-server")
    ManagedClient resourceServer;

    @Test
    public void testValidPolicyWithCustomMapperAttribute() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createScope(authorization, "view");

        PolicyRepresentation policy = new PolicyRepresentation();
        policy.setName("Valid Custom Attr Policy");
        policy.setType("script-scripts/cedar-custom-attr-policy.cedar");
        policy.getConfig().put("validateSchema", "true");

        try (Response response = authorization.policies().create(policy)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(),
                    "Policy referencing custom 'department' mapper attribute should pass schema validation");
        }
    }

    @Test
    public void testInvalidPolicyRejectedBySchemaValidation() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createScope(authorization, "view");

        PolicyRepresentation policy = new PolicyRepresentation();
        policy.setName("Invalid Attr Policy");
        policy.setType("script-scripts/cedar-invalid-attr-policy.cedar");
        policy.getConfig().put("validateSchema", "true");

        try (Response response = authorization.policies().create(policy)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(),
                    "Policy referencing non-existent attribute should fail schema validation");
        }
    }

    @Test
    public void testInvalidPolicyAcceptedByDefault() {
        AuthorizationResource authorization = resourceServer.admin().authorization();

        createScope(authorization, "view");

        PolicyRepresentation policy = new PolicyRepresentation();
        policy.setName("Invalid Attr Policy No Validation");
        policy.setType("script-scripts/cedar-invalid-attr-policy.cedar");

        try (Response response = authorization.policies().create(policy)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(),
                    "Policy should be accepted by default since schema validation is disabled");
        }
    }

    private void createScope(AuthorizationResource authorization, String name) {
        if (authorization.scopes().findByName(name) != null) {
            return;
        }
        try (Response response = authorization.scopes().create(new ScopeRepresentation(name))) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
    }

    /**
     * Client config that enables authorization services and adds a custom "department"
     * protocol mapper so that the Cedar schema includes the "department" user attribute.
     */
    public static class CustomMapperResourceServerConfig implements ClientConfig {

        @Override
        public ClientBuilder configure(ClientBuilder client) {
            return client
                    .clientId(KeycloakModelUtils.generateId())
                    .secret("secret")
                    .authorizationServicesEnabled(true)
                    .directAccessGrantsEnabled(true)
                    .protocolMappers(createDepartmentMapper());
        }

        private static ProtocolMapperRepresentation createDepartmentMapper() {
            ProtocolMapperRepresentation mapper = new ProtocolMapperRepresentation();
            mapper.setName("department-mapper");
            mapper.setProtocol("openid-connect");
            mapper.setProtocolMapper("oidc-usermodel-attribute-mapper");

            Map<String, String> config = new HashMap<>();
            config.put("user.attribute", "department");
            config.put("claim.name", "department");
            config.put("access.token.claim", "true");
            config.put("id.token.claim", "false");
            config.put("jsonType.label", "String");
            mapper.setConfig(config);

            return mapper;
        }
    }
}
