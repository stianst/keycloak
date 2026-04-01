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

import java.util.Map;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ModelValidationException;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.provider.ScriptProviderMetadata;

import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.ValidationRequest;
import com.cedarpolicy.model.ValidationResponse;
import com.cedarpolicy.model.exception.AuthException;
import com.cedarpolicy.model.policy.PolicySet;
import com.cedarpolicy.model.schema.Schema;
import org.jboss.logging.Logger;

/**
 * Factory for creating {@link CedarPolicyProvider} instances from deployed Cedar policy files.
 *
 * <p>Each deployed Cedar policy file (.cedar) in a provider JAR gets its own factory instance,
 * registered with a unique provider ID based on the file name.
 */
public class CedarPolicyProviderFactory implements PolicyProviderFactory<PolicyRepresentation> {

    private static final Logger logger = Logger.getLogger(CedarPolicyProviderFactory.class);

    public static final String POLICY_TYPE = "cedar";

    private ScriptProviderMetadata metadata;
    private CedarPolicyProvider provider;

    public CedarPolicyProviderFactory(ScriptProviderMetadata metadata) {
        this.metadata = metadata;
    }

    @SuppressWarnings("unused")
    public CedarPolicyProviderFactory() {
        // for reflection
    }

    @Override
    public String getId() {
        return metadata.getId();
    }

    @Override
    public String getName() {
        return metadata.getName();
    }

    @Override
    public String getGroup() {
        return "Cedar";
    }

    @Override
    public String getDescription() {
        return metadata.getDescription();
    }

    @Override
    public String getCode() {
        return metadata.getCode();
    }

    @Override
    public PolicyProvider create(AuthorizationProvider authorization) {
        if (provider == null) {
            provider = new CedarPolicyProvider(metadata.getCode());
        }
        return provider;
    }

    @Override
    public PolicyProvider create(KeycloakSession session) {
        if (provider == null) {
            provider = new CedarPolicyProvider(metadata.getCode());
        }
        return provider;
    }

    @Override
    public PolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        PolicyRepresentation representation = new PolicyRepresentation();
        representation.setId(policy.getId());
        representation.setName(policy.getName());
        if (policy.getDescription() == null) {
            representation.setDescription(metadata.getDescription());
        }
        representation.setType(getId());
        representation.getConfig().put("code", metadata.getCode());
        return representation;
    }

    @Override
    public Class<PolicyRepresentation> getRepresentationType() {
        return PolicyRepresentation.class;
    }

    @Override
    public void onCreate(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        if (representation.getDescription() == null && metadata.getDescription() != null) {
            representation.setDescription(metadata.getDescription());
            policy.setDescription(metadata.getDescription());
        }
        if (!representation.getConfig().containsKey("code")) {
            representation.getConfig().put("code", metadata.getCode());
        }

        if (isSchemaValidationEnabled(representation.getConfig())) {
            validatePolicyAgainstSchema(policy, authorization);
        }
    }

    private boolean isSchemaValidationEnabled(Map<String, String> config) {
        return Boolean.parseBoolean(config != null ? config.get("validateSchema") : null);
    }

    private void validatePolicyAgainstSchema(Policy policy, AuthorizationProvider authorization) {
        String schemaJson = new CedarSchemaGenerator(authorization, policy.getResourceServer()).generate();
        Schema schema = new Schema(Schema.JsonOrCedar.Json,
              java.util.Optional.of(schemaJson),
              java.util.Optional.empty()
        );

        ValidationResponse response;
        try {
            PolicySet policySet = PolicySet.parsePolicies(metadata.getCode());
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
                throw new ModelValidationException("Cedar policy '" + policy.getName() + "' does not conform to schema: " + sb);
            }
        }
        logger.debugf("Cedar policy '%s' passed schema validation", policy.getName());
    }

    @Override
    public void onImport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        if (policy.getDescription() == null && metadata.getDescription() != null) {
            policy.setDescription(metadata.getDescription());
        }

        if (isSchemaValidationEnabled(representation.getConfig())) {
            validatePolicyAgainstSchema(policy, authorization);
        }
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

    public ScriptProviderMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ScriptProviderMetadata metadata) {
        this.metadata = metadata;
    }
}
