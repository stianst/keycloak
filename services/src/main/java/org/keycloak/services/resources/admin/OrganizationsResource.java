package org.keycloak.services.resources.admin;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.organization.Organization;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

import java.util.stream.Stream;

public class OrganizationsResource {

    private final KeycloakSession session;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;

    public OrganizationsResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.auth = auth;
        this.adminEvent = adminEvent;
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createOrganization(OrganizationRepresentation organization) {
        session.getProvider(OrganizationProvider.class).createOrganization(organization.getName());
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Stream<OrganizationRepresentation> listOrganizations() {
        return session.getProvider(OrganizationProvider.class)
                .listOrganizations()
                .map(o -> new OrganizationRepresentation(o.getName()));
    }

    @Path("{organization}")
    public OrganizationResource getOrganization(@PathParam("organization") String organizationName) {
        Organization organization = session.getProvider(OrganizationProvider.class).getOrganization(organizationName);
        return new OrganizationResource(organization, session);
    }

}
