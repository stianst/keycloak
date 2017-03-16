package org.keycloak.services.resources.account;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.spi.NotFoundException;
import org.keycloak.common.Version;
import org.keycloak.models.*;
import org.keycloak.models.Constants;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.theme.BrowserSecurityHeaderSetup;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.Theme;
import org.keycloak.utils.MediaType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.keycloak.models.ClientModel;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.util.ResolveRelative;
import org.keycloak.services.validation.Validation;

/**
 * Created by st on 29/03/17.
 */
public class AccountConsole {

    @Context
    protected KeycloakSession session;
    @Context
    protected UriInfo uriInfo;
    
    private final RealmModel realm;
    private final ClientModel client;
    private final Theme theme;

    public AccountConsole(RealmModel realm, ClientModel client, Theme theme) {
        this.realm = realm;
        this.client = client;
        this.theme = theme;
    }

    @GET
    @NoCache
    public Response getMainPage() throws URISyntaxException, IOException, FreeMarkerException {
        if (!uriInfo.getRequestUri().getPath().endsWith("/")) {
            return Response.status(302).location(uriInfo.getRequestUriBuilder().path("/").build()).build();
        } else {
            Map<String, Object> map = new HashMap<>();

            URI baseUri = uriInfo.getBaseUri();

            String authUrl = baseUri.toString();
            authUrl = authUrl.substring(0, authUrl.length() - 1);

            map.put("authUrl", authUrl);
            map.put("baseUrl", authUrl + "/realms/" + realm.getName() + "/account");
            map.put("realm", realm.getName());
            map.put("resourceUrl", Urls.themeRoot(baseUri) + "/account/" + theme.getName());
            map.put("resourceVersion", Version.RESOURCES_VERSION);
            
            String[] referrer = getReferrer();
            if (referrer != null) {
                map.put("referrer", referrer[0]);
                map.put("referrer_uri", referrer[1]);
            }
            
            map.put("properties", theme.getProperties());

            FreeMarkerUtil freeMarkerUtil = new FreeMarkerUtil();
            String result = freeMarkerUtil.processTemplate(map, "index.ftl", theme);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK).type(MediaType.TEXT_HTML_UTF_8).language(Locale.ENGLISH).entity(result);
            BrowserSecurityHeaderSetup.headers(builder, realm);
            return builder.build();
        }
    }

    @GET
    @Path("index.html")
    public Response getIndexHtmlRedirect() {
        return Response.status(302).location(session.getContext().getUri().getRequestUriBuilder().path("../").build()).build();
    }

    @GET
    @Path("keycloak.json")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public ClientManager.InstallationAdapterConfig getConfig() {
        ClientModel accountClient = realm.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID);
        if (accountClient == null) {
            throw new javax.ws.rs.NotFoundException("Account console client not found");
        }
        return new ClientManager().toInstallationRepresentation(realm, accountClient, session.getContext().getUri().getBaseUri());
    }
    
    private String[] getReferrer() {
        String referrer = uriInfo.getQueryParameters().getFirst("referrer");
        if (referrer == null) {
            return null;
        }

        String referrerUri = uriInfo.getQueryParameters().getFirst("referrer_uri");

        ClientModel referrerClient = realm.getClientByClientId(referrer);
        if (referrerClient != null) {
            if (referrerUri != null) {
                referrerUri = RedirectUtils.verifyRedirectUri(uriInfo, referrerUri, realm, referrerClient);
            } else {
                referrerUri = ResolveRelative.resolveRelativeUri(uriInfo.getRequestUri(), client.getRootUrl(), referrerClient.getBaseUrl());
            }
            
            if (referrerUri != null) {
                String referrerName = referrerClient.getName();
                if (Validation.isBlank(referrerName)) {
                    referrerName = referrer;
                }
                return new String[]{referrerName, referrerUri};
            }
        } else if (referrerUri != null) {
            referrerClient = realm.getClientByClientId(referrer);
            if (client != null) {
                referrerUri = RedirectUtils.verifyRedirectUri(uriInfo, referrerUri, realm, referrerClient);

                if (referrerUri != null) {
                    return new String[]{referrer, referrerUri};
                }
            }
        }

        return null;
    }

}
