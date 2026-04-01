// This policy references a non-existent attribute and should fail schema validation
permit(
    principal,
    action == Keycloak::Action::"view",
    resource
)
when {
    principal.nonexistent_attribute == "value"
};
