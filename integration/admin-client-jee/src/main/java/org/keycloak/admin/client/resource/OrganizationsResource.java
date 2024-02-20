package org.keycloak.admin.client.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.keycloak.representations.idm.OrganizationRepresentation;

import java.util.List;

public interface OrganizationsResource {

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    void createOrganization(OrganizationRepresentation organization);

//    @GET
//    @Path("")
//    @Produces(MediaType.APPLICATION_JSON)
//    List<OrganizationRepresentation> listOrganizations();

    @Path("{organization}")
    OrganizationResource getOrganization(@PathParam("organization") String organizationName);
}
