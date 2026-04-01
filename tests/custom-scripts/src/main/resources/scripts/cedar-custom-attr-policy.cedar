// Grant view access only when the principal has a specific department
// This policy relies on a custom "department" protocol mapper being configured
permit(
    principal,
    action == Keycloak::Action::"view",
    resource
)
when {
    principal.department == "engineering"
};
