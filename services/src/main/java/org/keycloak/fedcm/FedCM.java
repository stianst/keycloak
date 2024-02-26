package org.keycloak.fedcm;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.urls.UrlType;

import java.util.Collections;

@Path("fedcm")
public class FedCM {

    private final KeycloakSession session;

    public FedCM(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("config")
    public ConfigRepresentation config() {
        ConfigRepresentation configRepresentation = new ConfigRepresentation();

        KeycloakUriInfo uri = session.getContext().getUri(UrlType.FRONTEND);

        String accountsUrl = Urls.realmBase(session.getContext().getUri().getBaseUri()).path("{realm}/fedcm/accounts").build(session.getContext().getRealm().getName()).toString();
        String idAssertionEndpoint = Urls.realmBase(session.getContext().getUri().getBaseUri()).path("{realm}/fedcm/assertion").build(session.getContext().getRealm().getName()).toString();

        configRepresentation.setAccountsEndpoint(accountsUrl);
        configRepresentation.setIdAssertionEndpoint(idAssertionEndpoint);

        return configRepresentation;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("accounts")
    public AccountsRepresentation accounts() {
        // TODO Verify that the request contains a Sec-Fetch-Dest: webidentity HTTP header. DOESN'T MATTER FOR POC
        // TODO Match the session cookies with the IDs of the already signed-in accounts. HUH? What?!

        AuthenticationManager.AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(session, session.getContext().getRealm(), true);
        if (authResult != null) {
            AccountRepresentation account = new AccountRepresentation();
            account.setId(authResult.getUser().getId());
            account.setName(authResult.getUser().getFirstName() + " " + authResult.getUser().getLastName());
            account.setEmail(authResult.getUser().getEmail());

            AccountsRepresentation accounts = new AccountsRepresentation();
            accounts.setAccounts(Collections.singletonList(account));
            return accounts;
        } else {
            throw new WebApplicationException(401);
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("assertion")
    public AssertionResponse assertion() {
        MultivaluedMap<String, String> decodedFormParameters = session.getContext().getHttpRequest().getDecodedFormParameters();

        String clientId = decodedFormParameters.getFirst("client_id");
        String accountId = decodedFormParameters.getFirst("account_id");

        AssertionResponse assertion = new AssertionResponse();
        assertion.setToken(clientId + "--" + accountId);
        return assertion;
    }

}
