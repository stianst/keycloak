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
package org.keycloak.authorization.policy.provider;

import java.util.EnumSet;

/**
 * A {@link PolicyProvider} for script-based policy implementations (e.g., JavaScript, Cedar).
 *
 * <p>Implementations declare which capabilities they support, allowing the UI to expose
 * appropriate controls based on the policy type.
 */
public interface ScriptingPolicyProvider extends PolicyProvider {

    enum Capability {
        /**
         * The policy can be validated against a schema at creation time.
         * When supported, the UI should expose a checkbox that sets {@code validateSchema=true}
         * in the policy config.
         */
        VERIFIABLE,
        /**
         * The policy provider supports runtime upload and removal of policy source files
         * via the admin REST API, without requiring a server restart or JAR redeployment.
         * When supported, the UI exposes upload/remove controls and an editable code editor.
         */
        RUNTIME_DEPLOYMENT
    }

    default EnumSet<Capability> supportedCapabilities() {
        return EnumSet.noneOf(Capability.class);
    }

    default boolean supports(Capability capability) {
        return supportedCapabilities().contains(capability);
    }
}
