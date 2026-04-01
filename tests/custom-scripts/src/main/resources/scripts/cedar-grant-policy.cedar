// Grant view access when the principal has a localhost email address
// and their given name is present (indicating a fully profiled user)
permit(
    principal,
    action == Keycloak::Action::"view",
    resource
)
when {
    principal.email like "*@localhost" &&
    principal.given_name == "First"
};

// Grant delete access only to principals with the admin realm role
permit(
    principal,
    action == Keycloak::Action::"delete",
    resource
)
when {
    principal.kc_realm_roles.contains("admin")
};
