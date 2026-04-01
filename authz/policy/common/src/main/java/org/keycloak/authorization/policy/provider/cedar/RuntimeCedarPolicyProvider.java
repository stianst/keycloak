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

import org.jboss.logging.Logger;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.ScriptingPolicyProvider;

/**
 * A Cedar policy provider for runtime-deployed policies whose code is stored in the database.
 *
 * <p>Unlike {@link CedarPolicyProvider} which parses code once at construction time,
 * this provider reads the policy code from the policy's config map at evaluation time,
 * allowing the code to be updated at runtime without restart.
 */
class RuntimeCedarPolicyProvider implements ScriptingPolicyProvider {

    private static final Logger logger = Logger.getLogger(RuntimeCedarPolicyProvider.class);

    // The BasicAuthorizationEngine is stateless — isAuthorized() takes all state as parameters.
    private static final AuthorizationEngine ENGINE = new BasicAuthorizationEngine();

    @Override
    public void evaluate(Evaluation evaluation) {
        String policyCode = evaluation.getPolicy().getConfig().get("code");

        if (policyCode == null || policyCode.isBlank()) {
            logger.warnf("Cedar policy '%s' has no code configured, denying", evaluation.getPolicy().getName());
            evaluation.deny();
            return;
        }

        PolicySet policySet;
        try {
            policySet = PolicySet.parsePolicies(policyCode);
        } catch (com.cedarpolicy.model.exception.InternalException e) {
            logger.warnf("Cedar policy '%s' failed to parse, denying", evaluation.getPolicy().getName());
            evaluation.deny();
            return;
        }
        CedarEvaluationHelper.evaluate(ENGINE, policySet, evaluation);
    }

    @Override
    public EnumSet<Capability> supportedCapabilities() {
        return EnumSet.of(Capability.VERIFIABLE, Capability.RUNTIME_DEPLOYMENT);
    }

    @Override
    public void close() {
    }
}
