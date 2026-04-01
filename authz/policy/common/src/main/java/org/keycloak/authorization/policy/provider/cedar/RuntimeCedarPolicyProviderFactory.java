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
package org.keycloak.authorization.policy.provider.cedar;

import java.util.HashMap;
import java.util.Map;

import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.ValidationRequest;
import com.cedarpolicy.model.ValidationResponse;
import com.cedarpolicy.model.exception.AuthException;
import com.cedarpolicy.model.policy.PolicySet;
import com.cedarpolicy.model.schema.Schema;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderAdminService;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ModelValidationException;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

/**
 * Factory for runtime-deployed Cedar policies. Registered with the well-known ID {@code "cedar"}.
 *
 * <p>Unlike {@link CedarPolicyProviderFactory} which is instantiated per JAR-deployed {@code .cedar} file,
 * this factory handles all runtime-deployed Cedar policies. Policy code is stored in the database
 * via the policy's config map and can be updated without a server restart.
 *
 * <p>This factory is gated behind the {@code CEDAR_POLICIES} feature flag ({@link Profile.Feature#CEDAR_POLICIES}).
 */
public class RuntimeCedarPolicyProviderFactory
        implements PolicyProviderFactory<PolicyRepresentation>, EnvironmentDependentProviderFactory {

    private static final Logger logger = Logger.getLogger(RuntimeCedarPolicyProviderFactory.class);

    public static final String PROVIDER_ID = "cedar";

    static final int MAX_POLICY_CODE_SIZE = 65_536;

    private final RuntimeCedarPolicyProvider provider = new RuntimeCedarPolicyProvider();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getName() {
        return "Cedar";
    }

    @Override
    public String getGroup() {
        return "Cedar";
    }

    @Override
    public String getDescription() {
        return "Cedar authorization policy with runtime deployment support";
    }

    @Override
    public PolicyProvider create(AuthorizationProvider authorization) {
        return provider;
    }

    @Override
    public PolicyProvider create(KeycloakSession session) {
        return provider;
    }

    @Override
    public PolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        PolicyRepresentation rep = new PolicyRepresentation();
        rep.setId(policy.getId());
        rep.setName(policy.getName());
        rep.setDescription(policy.getDescription());
        rep.setType(PROVIDER_ID);
        rep.setConfig(new HashMap<>(policy.getConfig()));
        return rep;
    }

    @Override
    public Class<PolicyRepresentation> getRepresentationType() {
        return PolicyRepresentation.class;
    }

    @Override
    public void onCreate(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        String code = representation.getConfig() != null ? representation.getConfig().get("code") : null;
        if (code == null || code.isBlank()) {
            throw new ModelValidationException("Cedar policy code is required");
        }
        validateCodeSize(code);
        validateSyntax(code, policy.getName());

        policy.putConfig("code", code);
        policy.putConfig("runtimeDeployed", "true");

        if (representation.getDescription() != null) {
            policy.setDescription(representation.getDescription());
        }

        if (isSchemaValidationEnabled(representation.getConfig())) {
            validatePolicyAgainstSchema(code, policy, authorization);
        }
    }

    @Override
    public void onUpdate(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        String code = representation.getConfig() != null ? representation.getConfig().get("code") : null;
        if (code != null && !code.isBlank()) {
            validateCodeSize(code);
            validateSyntax(code, policy.getName());
            policy.putConfig("code", code);
        }

        if (isSchemaValidationEnabled(representation.getConfig())) {
            String effectiveCode = code != null ? code : policy.getConfig().get("code");
            if (effectiveCode != null) {
                validatePolicyAgainstSchema(effectiveCode, policy, authorization);
            }
        }
    }

    @Override
    public void onRemove(Policy policy, AuthorizationProvider authorization) {
    }

    @Override
    public void onImport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        String code = representation.getConfig() != null ? representation.getConfig().get("code") : null;
        if (code == null || code.isBlank()) {
            throw new ModelValidationException("Cedar policy code is required");
        }
        validateCodeSize(code);
        validateSyntax(code, policy.getName());

        policy.putConfig("runtimeDeployed", "true");

        if (isSchemaValidationEnabled(representation.getConfig())) {
            validatePolicyAgainstSchema(code, policy, authorization);
        }
    }

    @Override
    public PolicyProviderAdminService getAdminResource(ResourceServer resourceServer, AuthorizationProvider authorization) {
        return new CedarPolicyAdminResource(resourceServer, authorization);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.CEDAR_POLICIES);
    }

    private static void validateCodeSize(String code) {
        if (code.length() > MAX_POLICY_CODE_SIZE) {
            throw new ModelValidationException(
                    "Cedar policy code exceeds maximum size of " + MAX_POLICY_CODE_SIZE + " bytes");
        }
    }

    private static void validateSyntax(String code, String policyName) {
        try {
            PolicySet.parsePolicies(code);
        } catch (Exception e) {
            throw new ModelValidationException(
                    "Invalid Cedar policy syntax for '" + policyName + "': " + e.getMessage());
        }
    }

    private static boolean isSchemaValidationEnabled(Map<String, String> config) {
        return Boolean.parseBoolean(config != null ? config.get("validateSchema") : null);
    }

    private static void validatePolicyAgainstSchema(String code, Policy policy, AuthorizationProvider authorization) {
        String schemaJson = new CedarSchemaGenerator(authorization, policy.getResourceServer()).generate();
        Schema schema = new Schema(Schema.JsonOrCedar.Json,
                java.util.Optional.of(schemaJson),
                java.util.Optional.empty()
        );

        ValidationResponse response;
        try {
            PolicySet policySet = PolicySet.parsePolicies(code);
            response = new BasicAuthorizationEngine().validate(new ValidationRequest(schema, policySet));
        } catch (AuthException e) {
            throw new RuntimeException("Unable to communicate with Cedar authorization engine", e);
        }

        if (response.type != ValidationResponse.SuccessOrFailure.Success) {
            throw new ModelValidationException("Cedar policy validation failed for policy '"
                    + policy.getName() + "': unable to validate against schema");
        }

        if (!response.validationPassed()) {
            ValidationResponse.ValidationSuccessResponse success = response.success.get();
            StringBuilder sb = new StringBuilder();
            for (ValidationResponse.ValidationError err : success.validationErrors) {
                logger.debugf("Cedar policy '%s' validation error: %s", policy.getName(), err.getError().message);
                if (!sb.isEmpty()) sb.append("; ");
                sb.append(err.getError());
            }
            if (!sb.isEmpty()) {
                throw new ModelValidationException(
                        "Cedar policy '" + policy.getName() + "' does not conform to schema: " + sb);
            }
        }
        logger.debugf("Cedar policy '%s' passed schema validation", policy.getName());
    }
}
