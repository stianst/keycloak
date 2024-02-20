package org.keycloak.admin.client.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface OrganizationResource {

    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> listUsers();

    @PUT
    @Path("users/{user-id}")
    void addUser(@PathParam("user-id") String userId);

    @DELETE
    @Path("users/{user-id}")
    void removeUser(@PathParam("user-id") String userId);

}
