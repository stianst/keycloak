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

import java.util.EnumSet;

import com.cedarpolicy.AuthorizationEngine;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.policy.PolicySet;

import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.ScriptingPolicyProvider;

/**
 * A {@link PolicyProvider} that evaluates Cedar policies natively using the cedar-java library.
 *
 * <p>The provider maps Keycloak's authorization context to Cedar's authorization model:
 * <ul>
 *   <li>Principal: {@code Keycloak::User::"<user-id>"}</li>
 *   <li>Action: {@code Keycloak::Action::"<scope-name>"} (first scope, or "access" if none)</li>
 *   <li>Resource: {@code Keycloak::Resource::"<resource-name>"}</li>
 * </ul>
 *
 * <p>Identity attributes and resource claims are passed as context to the Cedar policy engine.
 */
class CedarPolicyProvider implements ScriptingPolicyProvider {

    private static final AuthorizationEngine ENGINE = new BasicAuthorizationEngine();

    private final PolicySet policySet;

    CedarPolicyProvider(String policyCode) {
        try {
            this.policySet = PolicySet.parsePolicies(policyCode);
        } catch (com.cedarpolicy.model.exception.InternalException e) {
            throw new RuntimeException("Failed to parse Cedar policy", e);
        }
    }

    @Override
    public void evaluate(Evaluation evaluation) {
        CedarEvaluationHelper.evaluate(ENGINE, policySet, evaluation);
    }

    @Override
    public EnumSet<Capability> supportedCapabilities() {
        return EnumSet.of(Capability.VERIFIABLE);
    }

    @Override
    public void close() {
    }
}
