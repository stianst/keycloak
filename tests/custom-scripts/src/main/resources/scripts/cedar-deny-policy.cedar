// Deny delete actions on restricted resources unless the principal has the admin role
forbid(
    principal,
    action == Keycloak::Action::"delete",
    resource
)
when {
    resource.name like "*Restricted*"
} unless {
    principal.kc_realm_roles.contains("admin")
};
