/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.services.resources.account;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventStoreProvider;
import org.keycloak.events.EventType;
import org.keycloak.models.AccountRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.account.ClientRepresentation;
import org.keycloak.representations.account.SessionRepresentation;
import org.keycloak.representations.account.UserRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.managers.Auth;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resources.Cors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.services.ErrorResponse;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AccountService {

    private static final Logger logger = Logger.getLogger(AccountService.class);

    @Context
    private HttpRequest request;

    private final KeycloakSession session;
    private final ClientModel client;
    private final EventBuilder event;
    private EventStoreProvider eventStore;
    private Auth auth;

    public AccountService(KeycloakSession session, Auth auth, ClientModel client, EventBuilder event) {
        this.session = session;
        this.auth = auth;
        this.client = client;
        this.event = event;
    }

    public void init() {
        eventStore = session.getProvider(EventStoreProvider.class);
    }

    /**
     * CORS preflight
     *
     * @return
     */
    @Path("/")
    @OPTIONS
    public Response preflight() {
        return Cors.add(request, Response.ok()).auth().preflight().build();
    }

    /**
     * Get account information.
     *
     * @return
     */
    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response account() {
        auth.requireOneOf(AccountRoles.MANAGE_ACCOUNT, AccountRoles.VIEW_PROFILE);

        UserModel user = auth.getUser();

        UserRepresentation rep = new UserRepresentation();
        rep.setUsername(user.getUsername());
        rep.setFirstName(user.getFirstName());
        rep.setLastName(user.getLastName());
        rep.setEmail(user.getEmail());
        rep.setEmailVerified(user.isEmailVerified());
        rep.setAttributes(user.getAttributes());

        return Cors.add(request, Response.ok(rep)).auth().allowedOrigins(auth.getToken()).build();
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAccount(UserRepresentation userRep) {
        auth.require(AccountRoles.MANAGE_ACCOUNT);

        UserModel user = auth.getUser();

        event.event(EventType.UPDATE_PROFILE).client(auth.getClient()).user(auth.getUser());

        AccountUtils.updateUsername(userRep.getUsername(), user, session);
        
        try {
            AccountUtils.updateEmail(userRep.getEmail(), user, session, event);
        } catch (ModelDuplicateException e) {
            return ErrorResponse.exists("User with email " + userRep.getEmail() + " already exists.");
        }

        user.setFirstName(userRep.getFirstName());
        user.setLastName(userRep.getLastName());

        if (userRep.getAttributes() != null) {
            for (String k : user.getAttributes().keySet()) {
                if (!userRep.getAttributes().containsKey(k)) {
                    user.removeAttribute(k);
                }
            }

            for (Map.Entry<String, List<String>> e : userRep.getAttributes().entrySet()) {
                user.setAttribute(e.getKey(), e.getValue());
            }
        }

        event.success();

        return Cors.add(request, Response.ok()).auth().allowedOrigins(auth.getToken()).build();
    }

    /**
     * Get session information.
     *
     * @return
     */
    @Path("/sessions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response sessions() {
        RealmModel realm = auth.getRealm();
        UserModel user = auth.getUser();

        List<SessionRepresentation> reps = new LinkedList<>();

        List<UserSessionModel> sessions = session.sessions().getUserSessions(realm, user);
        for (UserSessionModel s : sessions) {
            SessionRepresentation rep = new SessionRepresentation();
            rep.setId(s.getId());
            rep.setIpAddress(s.getIpAddress());
            rep.setStarted(s.getStarted());
            rep.setLastAccess(s.getLastSessionRefresh());
            rep.setExpires(s.getStarted() + realm.getSsoSessionMaxLifespan());
            rep.setClients(new LinkedList());

            for (String clientUUID : s.getAuthenticatedClientSessions().keySet()) {
                ClientModel client = realm.getClientById(clientUUID);
                ClientRepresentation clientRep = new ClientRepresentation();
                clientRep.setClientId(client.getClientId());
                clientRep.setClientName(client.getName());
                rep.getClients().add(clientRep);
            }

            reps.add(rep);
        }

        return Cors.add(request, Response.ok(reps)).auth().allowedOrigins(auth.getToken()).build();
    }

    /**
     * Remove sessions
     *
     * @param removeCurrent remove current session (default is false)
     * @return
     */
    @Path("/sessions")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response sessionsLogout(@QueryParam("current") boolean removeCurrent) {
        RealmModel realm = auth.getRealm();
        UserModel user = auth.getUser();
        UserSessionModel userSession = auth.getSession();

        List<UserSessionModel> userSessions = session.sessions().getUserSessions(realm, user);
        for (UserSessionModel s : userSessions) {
            if (removeCurrent || !s.getId().equals(userSession.getId())) {
                AuthenticationManager.backchannelLogout(session, s, true);
            }
        }

        return Cors.add(request, Response.ok()).auth().allowedOrigins(auth.getToken()).build();
    }


    // TODO Federated identities
    // TODO Update credentials
    // TODO Applications
    // TODO Logs

}
