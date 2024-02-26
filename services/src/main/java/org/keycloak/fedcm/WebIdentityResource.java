package org.keycloak.fedcm;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.Urls;

@Path(".well-known")
public class WebIdentityResource {

    @Context
    protected KeycloakSession session;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("web-identity")
    public WebIdentityRepresentation webIdentity() {
        WebIdentityRepresentation representation = new WebIdentityRepresentation();
        String[] providerUrls = session.realms().getRealmsStream().map(r -> Urls.realmBase(session.getContext().getUri().getBaseUri()).path("{realm}/fedcm/config").build(r.getName()).toString()).toArray(String[]::new);
        representation.setProviderUrls(providerUrls);
        return representation;
    }

}
