package org.keycloak.services.resources.admin;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.organization.Organization;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.stream.Stream;

public class OrganizationResource {

    private final Organization organization;
    private final KeycloakSession session;

    public OrganizationResource(Organization organization, KeycloakSession session) {
        this.organization = organization;
        this.session = session;
    }

    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    public Stream<UserRepresentation> listUsers() {
        return organization.listUsers().map(ModelToRepresentation::toBriefRepresentation);
    }

    @PUT
    @Path("users/{user-id}")
    public void addUser(@PathParam("user-id") String userId) {
        UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);
        organization.addUser(user);
    }

    @DELETE
    @Path("users/{user-id}")
    public void removeUser(@PathParam("user-id") String userId) {
        UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);
        organization.removeUser(user);
    }

}
