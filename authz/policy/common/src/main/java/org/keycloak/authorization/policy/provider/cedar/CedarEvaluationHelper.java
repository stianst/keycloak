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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.cedarpolicy.AuthorizationEngine;
import com.cedarpolicy.model.AuthorizationRequest;
import com.cedarpolicy.model.AuthorizationResponse;
import com.cedarpolicy.model.AuthorizationSuccessResponse;
import com.cedarpolicy.model.entity.Entity;
import com.cedarpolicy.model.policy.PolicySet;
import com.cedarpolicy.value.CedarList;
import com.cedarpolicy.value.EntityTypeName;
import com.cedarpolicy.value.EntityUID;
import com.cedarpolicy.value.PrimString;
import com.cedarpolicy.value.Value;

import org.jboss.logging.Logger;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.Evaluation;

/**
 * Shared Cedar evaluation logic used by both JAR-deployed ({@link CedarPolicyProvider}) and
 * runtime-deployed ({@link RuntimeCedarPolicyProvider}) providers.
 *
 * <p>All evaluations run on a bounded thread pool with a configurable timeout to prevent
 * resource exhaustion from complex or malicious policies.
 */
final class CedarEvaluationHelper {

    private static final Logger logger = Logger.getLogger(CedarEvaluationHelper.class);

    static final long EVAL_TIMEOUT_MS = 5000;

    private static final ExecutorService CEDAR_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            r -> {
                Thread t = new Thread(r, "cedar-eval");
                t.setDaemon(true);
                return t;
            }
    );

    private CedarEvaluationHelper() {
    }

    static void evaluate(AuthorizationEngine engine, PolicySet policySet, Evaluation evaluation) {
        Policy policy = evaluation.getPolicy();

        try {
            Identity identity = evaluation.getContext().getIdentity();
            ResourcePermission permission = evaluation.getPermission();
            Resource resource = permission.getResource();

            EntityUID principalEUID = createEntityUID("Keycloak::User", identity.getId());
            EntityUID actionEUID = createActionEUID(permission.getScopes());
            EntityUID resourceEUID = createEntityUID("Keycloak::Resource",
                    resource != null ? resource.getName() : "default");

            Set<Entity> entities = new HashSet<>();
            entities.add(new Entity(principalEUID, buildIdentityAttributes(identity), new HashSet<>()));
            entities.add(new Entity(resourceEUID, buildResourceAttributes(resource, permission), new HashSet<>()));
            entities.add(new Entity(actionEUID));

            Map<String, Value> context = buildContext(evaluation);

            AuthorizationRequest request = new AuthorizationRequest(
                    principalEUID, actionEUID, resourceEUID, context);

            AuthorizationResponse response = evaluateWithTimeout(engine, request, policySet, entities, policy.getName());

            if (response == null) {
                evaluation.deny();
                return;
            }

            if (response.type == AuthorizationResponse.SuccessOrFailure.Success) {
                AuthorizationSuccessResponse success = response.success.get();
                if (success.isAllowed()) {
                    logger.debugf("Cedar policy '%s' granted access", policy.getName());
                    evaluation.grant();
                } else {
                    logger.debugf("Cedar policy '%s' denied access", policy.getName());
                    evaluation.deny();
                }
            } else {
                logger.warnf("Cedar policy '%s' evaluation failed: %s", policy.getName(), response);
                evaluation.deny();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating Cedar policy [" + policy.getName() + "].", e);
        }
    }

    private static AuthorizationResponse evaluateWithTimeout(
            AuthorizationEngine engine,
            AuthorizationRequest request,
            PolicySet policySet,
            Set<Entity> entities,
            String policyName) {

        CompletableFuture<AuthorizationResponse> future = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return engine.isAuthorized(request, policySet, entities);
                    } catch (com.cedarpolicy.model.exception.AuthException e) {
                        throw new RuntimeException(e);
                    }
                },
                CEDAR_EXECUTOR
        ).orTimeout(EVAL_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof TimeoutException) {
                logger.warnf("Cedar policy '%s' evaluation timed out after %dms", policyName, EVAL_TIMEOUT_MS);
                return null;
            }
            throw e;
        }
    }

    static EntityUID createEntityUID(String type, String id) {
        EntityTypeName typeName = EntityTypeName.parse(type).orElseThrow(
                () -> new RuntimeException("Invalid Cedar entity type: " + type));
        return new EntityUID(typeName, id);
    }

    static EntityUID createActionEUID(Collection<Scope> scopes) {
        String actionName = scopes.stream()
                .findFirst()
                .map(Scope::getName)
                .orElse("access");
        return createEntityUID("Keycloak::Action", actionName);
    }

    static Map<String, Value> buildIdentityAttributes(Identity identity) {
        Map<String, Value> attrs = new HashMap<>();
        if (identity.getAttributes() != null) {
            identity.getAttributes().toMap().forEach((key, values) -> {
                if (values != null && !values.isEmpty()) {
                    List<String> nonNull = values.stream()
                            .filter(v -> v != null)
                            .collect(Collectors.toList());
                    if (!nonNull.isEmpty()) {
                        attrs.put(sanitizeKey(key), toValue(nonNull));
                    }
                }
            });
        }
        return attrs;
    }

    static Map<String, Value> buildResourceAttributes(Resource resource, ResourcePermission permission) {
        Map<String, Value> attrs = new HashMap<>();
        if (resource != null) {
            if (resource.getType() != null) {
                attrs.put("type", new PrimString(resource.getType()));
            }
            if (resource.getName() != null) {
                attrs.put("name", new PrimString(resource.getName()));
            }
        }
        Map<String, Set<String>> claims = permission.getClaims();
        if (claims != null) {
            claims.forEach((key, values) -> {
                if (values != null && !values.isEmpty()) {
                    attrs.put(sanitizeKey(key), toValue(values));
                }
            });
        }
        return attrs;
    }

    static Map<String, Value> buildContext(Evaluation evaluation) {
        Map<String, Value> context = new HashMap<>();
        if (evaluation.getContext().getAttributes() != null) {
            evaluation.getContext().getAttributes().toMap().forEach((key, values) -> {
                if (values != null && !values.isEmpty()) {
                    context.put(sanitizeKey(key), new PrimString(values.iterator().next()));
                }
            });
        }
        return context;
    }

    static Value toValue(Collection<String> values) {
        if (values.size() == 1) {
            return new PrimString(values.iterator().next());
        }
        List<Value> list = new ArrayList<>();
        values.forEach(v -> list.add(new PrimString(v)));
        return new CedarList(list);
    }

    static String sanitizeKey(String key) {
        return key.replace('.', '_').replace('-', '_');
    }
}
