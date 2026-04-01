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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ProtocolMapperModel;

/**
 * Generates a Cedar schema by inspecting a Keycloak client's authorization configuration.
 *
 * <p>Discovers user attributes from the client's protocol mappers (default and optional client scopes),
 * actions from the resource server's authorization scopes, and produces a schema via {@link CedarSchemaBuilder}.
 */
public class CedarSchemaGenerator {

    private static final String CLAIM_NAME_CONFIG = "claim.name";
    private static final String ACCESS_TOKEN_CLAIM_CONFIG = "access.token.claim";
    private static final String MULTIVALUED_CONFIG = "multivalued";

    private static final Set<String> STRUCTURAL_CLAIMS = Set.of(
          "sub", "iss", "aud", "exp", "iat", "nbf", "jti", "typ", "azp", "nonce",
          "auth_time", "at_hash", "c_hash", "acr", "sid", "session_state",
          "scope", "allowed-origins"
    );

    private final AuthorizationProvider authorization;
    private final ResourceServer resourceServer;

    public CedarSchemaGenerator(AuthorizationProvider authorization, ResourceServer resourceServer) {
        this.authorization = authorization;
        this.resourceServer = resourceServer;
    }

    public String generate() {
        CedarSchemaBuilder builder = new CedarSchemaBuilder();

        addActionsFromScopes(builder);
        addUserAttributesFromMappers(builder);
        addRoleAttributes(builder);

        return builder.generateJson();
    }

    private void addActionsFromScopes(CedarSchemaBuilder builder) {
        authorization.getStoreFactory().getScopeStore()
              .findByResourceServer(resourceServer)
              .stream()
              .map(Scope::getName)
              .forEach(builder::addAction);
    }

    private void addUserAttributesFromMappers(CedarSchemaBuilder builder) {
        ClientModel client = authorization.getKeycloakSession()
              .getContext().getRealm()
              .getClientById(resourceServer.getClientId());

        getAccessTokenMappers(client).forEach(mapper -> {
            String claimName = mapper.getConfig().get(CLAIM_NAME_CONFIG);
            if (claimName == null || STRUCTURAL_CLAIMS.contains(claimName)) {
                return;
            }
            // Skip nested claims like realm_access.roles — these are handled separately as kc.realm.roles
            if (claimName.contains(".")) {
                return;
            }
            String cedarType = mapToCedarType(mapper);
            builder.addUserAttribute(claimName, cedarType);
        });
    }

    private void addRoleAttributes(CedarSchemaBuilder builder) {
        builder.addUserAttribute("kc.realm.roles", "Set<String>");

        ClientModel client = authorization.getKeycloakSession()
              .getContext().getRealm()
              .getClientById(resourceServer.getClientId());

        builder.addUserAttribute("kc.client." + client.getClientId() + ".roles", "Set<String>");
    }

    private Stream<ProtocolMapperModel> getAccessTokenMappers(ClientModel client) {
        Stream<ProtocolMapperModel> clientMappers = client.getProtocolMappersStream();

        // Default client scopes are always included
        Stream<ProtocolMapperModel> defaultScopeMappers = client.getClientScopes(true).values().stream()
              .flatMap(ClientScopeModel::getProtocolMappersStream);

        // Optional client scopes may be included depending on the request
        Stream<ProtocolMapperModel> optionalScopeMappers = client.getClientScopes(false).values().stream()
              .flatMap(ClientScopeModel::getProtocolMappersStream);

        return Stream.of(clientMappers, defaultScopeMappers, optionalScopeMappers)
              .flatMap(s -> s)
              .filter(this::isIncludedInAccessToken)
              .collect(Collectors.toMap(
                    m -> m.getConfig().getOrDefault(CLAIM_NAME_CONFIG, m.getName()),
                    m -> m,
                    (a, b) -> a))
              .values()
              .stream();
    }

    private boolean isIncludedInAccessToken(ProtocolMapperModel mapper) {
        Map<String, String> config = mapper.getConfig();
        return config != null && "true".equals(config.get(ACCESS_TOKEN_CLAIM_CONFIG));
    }

    private String mapToCedarType(ProtocolMapperModel mapper) {
        Map<String, String> config = mapper.getConfig();
        boolean multivalued = "true".equals(config.get(MULTIVALUED_CONFIG));
        // KeycloakIdentity converts all claim values to strings via JsonNode.asText()
        // so even boolean/numeric types become "String" in Cedar
        return multivalued ? "Set<String>" : "String";
    }
}
