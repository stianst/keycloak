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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.cedarpolicy.model.policy.PolicySet;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.provider.PolicyProviderAdminService;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.models.ModelValidationException;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

/**
 * Admin REST resource for Cedar-specific policy operations, accessible at
 * {@code .../policy/cedar/provider/...}.
 *
 * <p>Provides endpoints for uploading, listing, and validating Cedar policies at runtime.
 * Authorization checks are inherited from the parent {@code PolicyTypeService}.
 */
public class CedarPolicyAdminResource implements PolicyProviderAdminService<PolicyRepresentation> {

    record CedarPolicyUploadRequest(String name, String description, String fileName, String code, boolean validateSchema) {}

    private final ResourceServer resourceServer;
    private final AuthorizationProvider authorization;

    public CedarPolicyAdminResource(ResourceServer resourceServer, AuthorizationProvider authorization) {
        this.resourceServer = resourceServer;
        this.authorization = authorization;
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadPolicy(CedarPolicyUploadRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Policy name is required");
        }
        if (request.code() == null || request.code().isBlank()) {
            throw new BadRequestException("Policy code is required");
        }
        if (request.code().length() > RuntimeCedarPolicyProviderFactory.MAX_POLICY_CODE_SIZE) {
            throw new BadRequestException(
                    "Cedar policy code exceeds maximum size of " + RuntimeCedarPolicyProviderFactory.MAX_POLICY_CODE_SIZE + " bytes");
        }

        try {
            PolicySet.parsePolicies(request.code());
        } catch (Exception e) {
            throw new BadRequestException("Invalid Cedar policy syntax: " + e.getMessage());
        }

        PolicyRepresentation rep = new PolicyRepresentation();
        rep.setName(request.name());
        rep.setDescription(request.description());
        rep.setType(RuntimeCedarPolicyProviderFactory.PROVIDER_ID);

        Map<String, String> config = new HashMap<>();
        config.put("code", request.code());
        config.put("runtimeDeployed", "true");
        if (request.fileName() != null) {
            config.put("fileName", request.fileName());
        }
        if (request.validateSchema()) {
            config.put("validateSchema", "true");
        }
        rep.setConfig(config);

        PolicyStore policyStore = authorization.getStoreFactory().getPolicyStore();

        Policy existing = policyStore.findByName(resourceServer, request.name());
        if (existing != null) {
            throw new BadRequestException("Policy with name '" + request.name() + "' already exists");
        }

        Policy policy = policyStore.create(resourceServer, rep);

        try {
            authorization.getProviderFactory(RuntimeCedarPolicyProviderFactory.PROVIDER_ID)
                    .onCreate(policy, rep, authorization);
        } catch (ModelValidationException e) {
            policyStore.delete(policy.getId());
            throw new BadRequestException(e.getMessage());
        }

        return Response.status(Response.Status.CREATED)
                .entity(Map.of("id", policy.getId(), "name", policy.getName(), "type", RuntimeCedarPolicyProviderFactory.PROVIDER_ID))
                .build();
    }

    @GET
    @Path("/deployed")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listDeployedPolicies() {
        PolicyStore policyStore = authorization.getStoreFactory().getPolicyStore();

        Map<Policy.FilterOption, String[]> filters = new java.util.EnumMap<>(Policy.FilterOption.class);
        filters.put(Policy.FilterOption.TYPE, new String[]{RuntimeCedarPolicyProviderFactory.PROVIDER_ID});

        List<Map<String, Object>> result = policyStore.find(resourceServer, filters, -1, -1).stream()
                .filter(p -> "true".equals(p.getConfig().get("runtimeDeployed")))
                .map(p -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("id", p.getId());
                    entry.put("name", p.getName());
                    entry.put("description", p.getDescription() != null ? p.getDescription() : "");
                    entry.put("fileName", p.getConfig().getOrDefault("fileName", ""));
                    return entry;
                })
                .collect(Collectors.toList());

        return Response.ok(result).build();
    }

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validatePolicy(Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Policy code is required");
        }
        if (code.length() > RuntimeCedarPolicyProviderFactory.MAX_POLICY_CODE_SIZE) {
            return Response.ok(Map.of("valid", false,
                    "error", "Policy code exceeds maximum size of " + RuntimeCedarPolicyProviderFactory.MAX_POLICY_CODE_SIZE + " bytes"))
                    .build();
        }
        try {
            PolicySet.parsePolicies(code);
        } catch (Exception e) {
            return Response.ok(Map.of("valid", false, "error", e.getMessage())).build();
        }
        return Response.ok(Map.of("valid", true)).build();
    }
}
