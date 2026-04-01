/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.authzen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.attribute.Attributes;
import org.keycloak.authorization.common.ClientModelIdentity;
import org.keycloak.authorization.common.DefaultEvaluationContext;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.identity.UserModelIdentity;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.authorization.store.ScopeStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.common.constants.ServiceAccountConstants;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.authorization.Permission;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.util.JsonSerialization;

public class AuthZenResource {

    private static final AuthZen.EvaluationResponse DECISION_FALSE = new AuthZen.EvaluationResponse(false);
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-8][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");

    private static final String NAMESPACE_ID = "id:";
    private static final String NAMESPACE_USERNAME = "username:";
    private static final String NAMESPACE_EMAIL = "email:";

    private final KeycloakSession session;

    public AuthZenResource(KeycloakSession session) {
        this.session = session;
    }

    @POST
    @Path(AuthZen.EVALUATION_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluate(AuthZen.EvaluationRequest request) {
        AccessToken token = authenticateClient();
        return Response.ok(evaluateSingle(request, token)).build();
    }

    @POST
    @Path(AuthZen.EVALUATIONS_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluations(AuthZen.EvaluationsRequest request) {
        AccessToken token = authenticateClient();

        if (request.evaluations() == null || request.evaluations().isEmpty()) {
            // AuthZen 1.0 Section 7.1
            // "If an evaluations array is NOT present or is empty, the Access Evaluations Request behaves in a
            // backwards-compatible manner with the (single) Access Evaluation API Request (Section 6.1)."
            AuthZen.EvaluationRequest single = new AuthZen.EvaluationRequest(
                  request.subject(), request.resource(), request.action(), request.context());
            return Response.ok(evaluateSingle(single, token)).build();
        }

        AuthZen.EvaluationsSemantic semantic = AuthZen.EvaluationsSemantic.EXECUTE_ALL;
        if (request.options() != null && request.options().evaluationsSemantic() != null) {
            semantic = request.options().evaluationsSemantic();
        }

        List<AuthZen.EvaluationResponse> results = new ArrayList<>(request.evaluations().size());
        for (AuthZen.EvaluationItem item : request.evaluations()) {
            AuthZen.EvaluationRequest merged = mergeDefaults(request, item);
            AuthZen.EvaluationResponse itemResponse = evaluateSingle(merged, token);

            if (semantic == AuthZen.EvaluationsSemantic.DENY_ON_FIRST_DENY && !itemResponse.decision()) {
                results.add(new AuthZen.EvaluationResponse(false, Map.of("reason", "deny_on_first_deny")));
                break;
            }

            results.add(itemResponse);

            if (semantic == AuthZen.EvaluationsSemantic.PERMIT_ON_FIRST_PERMIT && itemResponse.decision()) {
                break;
            }
        }
        return Response.ok(new AuthZen.EvaluationsResponse(results)).build();
    }

    private static AuthZen.EvaluationRequest mergeDefaults(AuthZen.EvaluationsRequest defaults, AuthZen.EvaluationItem item) {
        AuthZen.Subject subject = item.subject() != null ? item.subject() : defaults.subject();
        AuthZen.Resource resource = item.resource() != null ? item.resource() : defaults.resource();
        AuthZen.Action action = item.action() != null ? item.action() : defaults.action();
        Map<String, Object> context = item.context() != null ? item.context() : defaults.context();
        return new AuthZen.EvaluationRequest(subject, resource, action, context);
    }

    private AccessToken authenticateClient() {
        AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
        if (authResult == null) {
            throw new NotAuthorizedException("Bearer");
        }
        // The evaluation of policies must only be called from a confidential client with its service account user.
        // Only with that, we ensure that reveal an authorization result to the appropriate caller.
        if (!authResult.client().getId().equals(authResult.user().getServiceAccountClientLink()) || !Objects.equals(authResult.session().getAuthMethod(), ServiceAccountConstants.CLIENT_AUTH)) {
            throw new NotAuthorizedException("Bearer");
        }
        return authResult.token();
    }

    private AuthZen.EvaluationResponse evaluateSingle(AuthZen.EvaluationRequest request, AccessToken token) {
        if (request.subject() == null || request.resource() == null || request.action() == null) {
            return new AuthZen.EvaluationResponse(false);
        }

        RealmModel realm = session.getContext().getRealm();
        AuthorizationProvider authorization = session.getProvider(AuthorizationProvider.class);
        StoreFactory storeFactory = authorization.getStoreFactory();

        // TODO need a generic solution for Gateways: https://github.com/keycloak/keycloak/issues/49696
        // The resource server is always identified by the bearer token's client (e.g. mcp-gateway).
        // For USER subjects the subject identity is resolved separately.
        // For CLIENT subjects the subject.id names the client being evaluated — it need not be the
        // same as the bearer token's client, allowing a gateway service account to evaluate policies
        // for other agent clients against its own resource server.
        ClientModel resourceServerClient = realm.getClientByClientId(token.getIssuedFor());
        if (resourceServerClient == null || !resourceServerClient.isEnabled()) {
            return DECISION_FALSE;
        }

        ResourceServer resourceServer = storeFactory.getResourceServerStore().findByClient(resourceServerClient);
        if (resourceServer == null) {
            return DECISION_FALSE;
        }

        Identity identity = resolveSubjectIdentity(realm, request);
        if (identity == null) {
            return DECISION_FALSE;
        }

        ResourceStore resourceStore = storeFactory.getResourceStore();
        Resource resource = resourceStore.findByName(resourceServer, request.resource().id());
        if (resource == null) {
            return DECISION_FALSE;
        }

        String keycloakType = resource.getType() != null ? resource.getType() : "";
        if (!keycloakType.equals(request.resource().type())) {
            return DECISION_FALSE;
        }

        ScopeStore scopeStore = storeFactory.getScopeStore();
        Scope scope = scopeStore.findByName(resourceServer, request.action().name());
        if (scope == null) {
            return DECISION_FALSE;
        }

        // Seed claims with the resource's registered Keycloak attributes (e.g. sensitivity, team).
        // This means callers do not need to supply resource.properties — they are always present
        // in Cedar as resource.* attributes regardless of whether the request body includes them.
        Map<String, List<String>> claims = new HashMap<>();
        Map<String, List<String>> resourceAttrs = resource.getAttributes();
        if (resourceAttrs != null) {
            resourceAttrs.forEach((k, v) -> {
                if (v != null && !v.isEmpty()) {
                    claims.put(k, v);
                }
            });
        }
        // Request-supplied resource.properties override registered attributes, allowing the caller
        // to augment or override values (e.g. for testing or dynamic attributes).
        if (request.resource().properties() != null) {
            convertContext(request.resource().properties(), claims);
        }
        if (request.context() != null) {
            convertContext(request.context(), claims);
        }

        DefaultEvaluationContext context = new DefaultEvaluationContext(identity, claims, session);
        ResourcePermission permission = new ResourcePermission(resource, List.of(scope), resourceServer, claims);

        Collection<Permission> granted = authorization.evaluators()
              .from(List.of(permission), context)
              .evaluate(resourceServer, null);

        return new AuthZen.EvaluationResponse(!granted.isEmpty());
    }

    private Identity resolveSubjectIdentity(RealmModel realm, AuthZen.EvaluationRequest request) {
        AuthZen.Subject subject = request.subject();
        Identity identity = switch (subject.type()) {
            case USER -> {
                UserModel user = resolveUserId(realm, subject.id());
                yield user != null ? createUserIdentity(realm, user) : null;
            }
            case CLIENT -> {
                ClientModel subjectClient = realm.getClientByClientId(subject.id());
                yield subjectClient != null ? createClientIdentity(session, subjectClient) : null;
            }
        };
        if (identity != null && subject.properties() != null && !subject.properties().isEmpty()) {
            identity = withSubjectProperties(identity, subject.properties());
        }
        return identity;
    }

    private static Identity createUserIdentity(RealmModel realm, UserModel user) {
        Map<String, Collection<String>> attributes = new HashMap<>();

        Map<String, List<String>> userAttrs = user.getAttributes();
        if (userAttrs != null) {
            attributes.putAll(userAttrs);
        }

        // UserModel.getAttributes() uses Java property names (firstName, lastName).
        // Add OIDC standard claim names so policies can reference either form.
        if (user.getFirstName() != null) {
            attributes.put("given_name", List.of(user.getFirstName()));
        }
        if (user.getLastName() != null) {
            attributes.put("family_name", List.of(user.getLastName()));
        }
        if (user.getUsername() != null) {
            attributes.put("preferred_username", List.of(user.getUsername()));
        }

        Set<String> realmRoles = new HashSet<>();
        user.getRealmRoleMappingsStream().forEach(role -> collectRoleNames(role, realmRoles));
        if (!realmRoles.isEmpty()) {
            attributes.put("kc.realm.roles", realmRoles);
        }

        Attributes enrichedAttributes = Attributes.from(attributes);
        return new UserModelIdentity(realm, user) {
            @Override
            public Attributes getAttributes() {
                return enrichedAttributes;
            }
        };
    }

    /**
     * Creates an enriched identity for a client subject, mirroring what {@link #createUserIdentity}
     * does for users. Collects the service account's realm role mappings into {@code kc.realm.roles}
     * so Cedar policies can inspect them via {@code principal.kc_realm_roles}.
     */
    private Identity createClientIdentity(KeycloakSession session, ClientModel client) {
        UserModel serviceAccount = session.users().getServiceAccount(client);

        Map<String, Collection<String>> attributes = new HashMap<>();

        if (serviceAccount != null) {
            // Include any custom attributes set directly on the service-account user.
            Map<String, List<String>> saAttrs = serviceAccount.getAttributes();
            if (saAttrs != null) {
                attributes.putAll(saAttrs);
            }

            // Expose service-account realm roles under the same key used for human users,
            // so Cedar policies can use principal.kc_realm_roles uniformly.
            Set<String> realmRoles = new HashSet<>();
            serviceAccount.getRealmRoleMappingsStream().forEach(role -> collectRoleNames(role, realmRoles));
            if (!realmRoles.isEmpty()) {
                attributes.put("kc.realm.roles", realmRoles);
            }
        }

        // Expose the OAuth client_id so policies can reference it if needed.
        attributes.put("client_id", List.of(client.getClientId()));

        Attributes enrichedAttributes = Attributes.from(attributes);
        return new ClientModelIdentity(session, client) {
            @Override
            public Attributes getAttributes() {
                return enrichedAttributes;
            }
        };
    }

    private static void collectRoleNames(RoleModel role, Set<String> names) {
        names.add(role.getName());
        if (role.isComposite()) {
            role.getCompositesStream()
                    .filter(child -> !child.isClientRole())
                    .forEach(child -> collectRoleNames(child, names));
        }
    }

    private UserModel resolveUserId(RealmModel realm, String subjectId) {
        UserProvider users = session.users();
        if (subjectId.startsWith(NAMESPACE_ID)) {
            String id = extractNamespaceValue(subjectId, NAMESPACE_ID);
            return users.getUserById(realm, id);
        } else if (subjectId.startsWith(NAMESPACE_USERNAME)) {
            String username = extractNamespaceValue(subjectId, NAMESPACE_USERNAME);
            return users.getUserByUsername(realm, username);
        } else if (subjectId.startsWith(NAMESPACE_EMAIL)) {
            if (realm.isDuplicateEmailsAllowed()) {
                throw new BadRequestException("email namespace cannot be used when duplicate emails are allowed");
            }
            String email = extractNamespaceValue(subjectId, NAMESPACE_EMAIL);
            return users.getUserByEmail(realm, email);
        } else if (UUID_PATTERN.matcher(subjectId).matches()) {
            return users.getUserById(realm, subjectId);
        } else {
            return users.getUserByUsername(realm, subjectId);
        }
    }

    private static String extractNamespaceValue(String subjectId, String namespace) {
        String value = subjectId.substring(namespace.length());
        if (value.isEmpty()) {
            throw new BadRequestException("subject id namespace '" + namespace + "' requires a non-empty value");
        }
        return value;
    }

    private static Identity withSubjectProperties(Identity delegate, Map<String, Object> properties) {
        Map<String, Collection<String>> extra = new HashMap<>();
        convertValues(properties, extra);

        return new Identity() {
            @Override
            public String getId() {
                return delegate.getId();
            }

            @Override
            public Attributes getAttributes() {
                Map<String, Collection<String>> merged = new HashMap<>(delegate.getAttributes().toMap());
                merged.putAll(extra);
                return Attributes.from(merged);
            }

            @Override
            public boolean hasRealmRole(String roleName) {
                return delegate.hasRealmRole(roleName);
            }

            @Override
            public boolean hasClientRole(String clientId, String roleName) {
                return delegate.hasClientRole(clientId, roleName);
            }

            @Override
            public boolean hasOneClientRole(String clientId, String... roleNames) {
                return delegate.hasOneClientRole(clientId, roleNames);
            }
        };
    }

    private static void convertValues(Map<String, Object> source, Map<String, ? extends Collection<String>> result) {
        @SuppressWarnings("unchecked")
        Map<String, Collection<String>> target = (Map<String, Collection<String>>) result;
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map || value instanceof List) {
                try {
                    target.put(entry.getKey(), List.of(JsonSerialization.writeValueAsString(value)));
                } catch (Exception e) {
                    target.put(entry.getKey(), List.of(String.valueOf(value)));
                }
            } else {
                target.put(entry.getKey(), List.of(String.valueOf(value)));
            }
        }
    }

    private static void convertContext(Map<String, Object> context, Map<String, List<String>> result) {
        convertValues(context, result);
    }
}
