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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builds a Cedar schema by manually specifying entity types, attributes, and actions.
 *
 * @see CedarSchemaGenerator
 */
public class CedarSchemaBuilder {

    private static final String NAMESPACE = "Keycloak";
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final Set<String> actions = new LinkedHashSet<>();
    private final Map<String, String> userAttributes = new LinkedHashMap<>();
    private final Map<String, String> resourceAttributes = new LinkedHashMap<>();

    public CedarSchemaBuilder() {
        // Built-in resource attributes from CedarPolicyProvider.buildResourceAttributes
        resourceAttributes.put("name", "String");
        resourceAttributes.put("type", "String");
    }

    /**
     * Add an action (scope) to the schema.
     */
    public CedarSchemaBuilder addAction(String actionName) {
        actions.add(actionName);
        return this;
    }

    /**
     * Add a user attribute with its Cedar type (e.g., "String", "Long", "Boolean", "Set<String>").
     */
    public CedarSchemaBuilder addUserAttribute(String name, String cedarType) {
        userAttributes.put(sanitizeKey(name), cedarType);
        return this;
    }

    /**
     * Generate the schema as a JSON string.
     */
    public String generateJson() {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode namespace = root.putObject(NAMESPACE);

        namespace.set("entityTypes", buildEntityTypes());
        namespace.set("actions", buildActions());

        try {
            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Cedar schema", e);
        }
    }

    private ObjectNode buildEntityTypes() {
        ObjectNode entityTypes = MAPPER.createObjectNode();

        // User entity type
        ObjectNode userType = MAPPER.createObjectNode();
        userType.set("shape", buildShape(userAttributes));
        entityTypes.set("User", userType);

        // Resource entity type
        ObjectNode resourceType = MAPPER.createObjectNode();
        resourceType.set("shape", buildShape(resourceAttributes));
        entityTypes.set("Resource", resourceType);

        return entityTypes;
    }

    private ObjectNode buildShape(Map<String, String> attributes) {
        ObjectNode shape = MAPPER.createObjectNode();
        shape.put("type", "Record");

        ObjectNode attrs = MAPPER.createObjectNode();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            attrs.set(entry.getKey(), buildAttributeType(entry.getValue()));
        }
        shape.set("attributes", attrs);

        return shape;
    }

    private ObjectNode buildAttributeType(String cedarType) {
        ObjectNode typeNode = MAPPER.createObjectNode();

        if (cedarType.startsWith("Set<") && cedarType.endsWith(">")) {
            typeNode.put("type", "Set");
            ObjectNode elementType = MAPPER.createObjectNode();
            elementType.put("type", cedarType.substring(4, cedarType.length() - 1));
            typeNode.set("element", elementType);
        } else {
            typeNode.put("type", cedarType);
        }

        return typeNode;
    }

    private ObjectNode buildActions() {
        ObjectNode actionsNode = MAPPER.createObjectNode();

        for (String action : actions) {
            ObjectNode actionNode = MAPPER.createObjectNode();
            ObjectNode appliesTo = MAPPER.createObjectNode();

            ArrayNode principalTypes = MAPPER.createArrayNode();
            principalTypes.add("User");
            appliesTo.set("principalTypes", principalTypes);

            ArrayNode resourceTypes = MAPPER.createArrayNode();
            resourceTypes.add("Resource");
            appliesTo.set("resourceTypes", resourceTypes);

            actionNode.set("appliesTo", appliesTo);
            actionsNode.set(action, actionNode);
        }

        return actionsNode;
    }

    private static String sanitizeKey(String key) {
        return key.replace('.', '_').replace('-', '_');
    }
}
