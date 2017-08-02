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
import org.jboss.resteasy.annotations.cache.NoCache;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import org.keycloak.common.ClientConnection;
import org.keycloak.credential.CredentialModel;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ModelException;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.theme.beans.MessageType;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AccountService {

    private static final Logger logger = Logger.getLogger(AccountService.class);

    @Context
    private HttpRequest request;
    @Context
    protected UriInfo uriInfo;
    @Context
    protected HttpHeaders headers;
    @Context
    protected ClientConnection clientConnection;

    private final KeycloakSession session;
    private final ClientModel client;
    private final EventBuilder event;
    private EventStoreProvider eventStore;
    private Auth auth;
    
    private final RealmModel realm;
    private final UserModel user;
    
    private List<FormMessage> messages = null;
    private MessageType messageType = MessageType.ERROR;
    
    private UIMessages uiMessages;

    public AccountService(KeycloakSession session, Auth auth, ClientModel client, EventBuilder event) {
        this.session = session;
        this.auth = auth;
        this.realm = auth.getRealm();
        this.user = auth.getUser();
        this.client = client;
        this.event = event;
        
        this.uiMessages = new UIMessages(session, auth);
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
    @NoCache
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
    @NoCache
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
    @NoCache
    public Response updateAccount(UserRepresentation userRep) {
        auth.require(AccountRoles.MANAGE_ACCOUNT);

        event.event(EventType.UPDATE_PROFILE).client(auth.getClient()).user(user);

        try {
            RealmModel realm = session.getContext().getRealm();

            boolean usernameChanged = userRep.getUsername() != null && !userRep.getUsername().equals(user.getUsername());
            if (realm.isEditUsernameAllowed()) {
                if (usernameChanged) {
                    UserModel existing = session.users().getUserByUsername(userRep.getUsername(), realm);
                    if (existing != null) {
                        return ErrorResponse.exists(uiMessages.localize(Messages.USERNAME_EXISTS));
                    }

                    user.setUsername(userRep.getUsername());
                }
            } else if (usernameChanged) {
                return ErrorResponse.error(uiMessages.localize(Messages.READ_ONLY_USERNAME), Response.Status.BAD_REQUEST);
            }

            boolean emailChanged = userRep.getEmail() != null && !userRep.getEmail().equals(user.getEmail());
            if (emailChanged && !realm.isDuplicateEmailsAllowed()) {
                UserModel existing = session.users().getUserByEmail(userRep.getEmail(), realm);
                if (existing != null) {
                    return ErrorResponse.exists(uiMessages.localize(Messages.EMAIL_EXISTS));
                }
            }

            if (realm.isRegistrationEmailAsUsername() && !realm.isDuplicateEmailsAllowed()) {
                UserModel existing = session.users().getUserByUsername(userRep.getEmail(), realm);
                if (existing != null) {
                    return ErrorResponse.exists(uiMessages.localize(Messages.USERNAME_EXISTS));
                }
            }

            if (emailChanged) {
                String oldEmail = user.getEmail();
                user.setEmail(userRep.getEmail());
                user.setEmailVerified(false);
                event.clone().event(EventType.UPDATE_EMAIL).detail(Details.PREVIOUS_EMAIL, oldEmail).detail(Details.UPDATED_EMAIL, userRep.getEmail()).success();

                if (realm.isRegistrationEmailAsUsername()) {
                    user.setUsername(userRep.getEmail());
                }
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
        } catch (ReadOnlyException e) {
            return ErrorResponse.error(uiMessages.localize(Messages.READ_ONLY_USER), Response.Status.BAD_REQUEST);
        }
    }
    
    /**
     * Get application information.
     *
     * @return
     */
    @Path("/applications")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response applications() {
        // ApplicationsBean gets "Cannot access delegate without a transaction"
        // in RealmCacheSession line 179.
        // ApplicationsBean apps = new ApplicationsBean(session, realm, user);
        // TODO: also eventually need to return AdvancedMessageFormatterMethod to get the localized messages.
        // see FreeMarkerAcocuntProvider
        
        SkinnyApplicationsBean apps = new SkinnyApplicationsBean(session, realm, user);
        return Cors.add(request, Response.ok(apps)).auth().allowedOrigins(auth.getToken()).build();
    }

    /**
     * Get session information.
     *
     * @return
     */
    @Path("/sessions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response sessions() {
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
    @NoCache
    public Response sessionsLogout(@QueryParam("current") boolean removeCurrent) {
        UserSessionModel userSession = auth.getSession();

        List<UserSessionModel> userSessions = session.sessions().getUserSessions(realm, user);
        for (UserSessionModel s : userSessions) {
            if (removeCurrent || !s.getId().equals(userSession.getId())) {
                AuthenticationManager.backchannelLogout(session, s, true);
            }
        }

        return Cors.add(request, Response.ok()).auth().allowedOrigins(auth.getToken()).build();
    }


    public static class PasswordChangeRequest {
        private String password;
        private String newPassword;
        private String confirmation;

        public PasswordChangeRequest() {}
        
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmation() {
            return confirmation;
        }

        public void setConfirmation(String confirmation) {
            this.confirmation = confirmation;
        }
    }
    
    @Path("/credentials")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response updateCredentials(PasswordChangeRequest passReq) {
        auth.require(AccountRoles.MANAGE_ACCOUNT);

        String password = passReq.getPassword();
        String passwordNew = passReq.getNewPassword();
        String passwordConfirm = passReq.getConfirmation();

        // why did old code call auth.getClientSession().getUserSession().getUser()?
        /*EventBuilder errorEvent = event.clone().event(EventType.UPDATE_PASSWORD_ERROR)
                .client(auth.getClient())
                .user(auth.getClientSession().getUserSession().getUser());*/
        
        EventBuilder errorEvent = event.clone().event(EventType.UPDATE_PASSWORD_ERROR)
                .client(client)
                .user(user);

        boolean requireCurrent = isPasswordSet(session, realm, user);
        if (requireCurrent) {
            if (Validation.isBlank(password)) {
                errorEvent.error(Errors.PASSWORD_MISSING);
                //return account.setError(Messages.MISSING_PASSWORD).createResponse(AccountPages.PASSWORD);
                return ErrorResponse.error(uiMessages.localize(Messages.MISSING_PASSWORD), 
                                           Response.Status.PRECONDITION_FAILED);
            }

            UserCredentialModel cred = UserCredentialModel.password(password);
            if (!session.userCredentialManager().isValid(realm, user, cred)) {
                errorEvent.error(Errors.INVALID_USER_CREDENTIALS);
                //return account.setError(Messages.INVALID_PASSWORD_EXISTING).createResponse(AccountPages.PASSWORD);
                return ErrorResponse.error(uiMessages.localize(Messages.INVALID_PASSWORD_EXISTING), 
                                           Response.Status.PRECONDITION_FAILED);
            }
        }

        if (Validation.isBlank(passwordNew)) {
            errorEvent.error(Errors.PASSWORD_MISSING);
            //return account.setError(Messages.MISSING_PASSWORD).createResponse(AccountPages.PASSWORD);
            return ErrorResponse.error(uiMessages.localize(Messages.MISSING_PASSWORD), 
                                       Response.Status.PRECONDITION_FAILED);
        }

        if (!passwordNew.equals(passwordConfirm)) {
            errorEvent.error(Errors.PASSWORD_CONFIRM_ERROR);
            //return account.setError(Messages.INVALID_PASSWORD_CONFIRM).createResponse(AccountPages.PASSWORD);
            return ErrorResponse.error(uiMessages.localize(Messages.INVALID_PASSWORD_CONFIRM), 
                                       Response.Status.PRECONDITION_FAILED);
        }

        try {
            session.userCredentialManager().updateCredential(realm, user, UserCredentialModel.password(passwordNew, false));
        } catch (ReadOnlyException mre) {
            errorEvent.error(Errors.NOT_ALLOWED);
            //return account.setError(Messages.READ_ONLY_PASSWORD).createResponse(AccountPages.PASSWORD);
            return Cors.add(request, Response.serverError()).auth().allowedOrigins(auth.getToken()).build();
        } catch (ModelException me) {
            ServicesLogger.LOGGER.failedToUpdatePassword(me);
            errorEvent.detail(Details.REASON, me.getMessage()).error(Errors.PASSWORD_REJECTED);
            //return account.setError(me.getMessage(), me.getParameters()).createResponse(AccountPages.PASSWORD);
            return Cors.add(request, Response.serverError()).auth().allowedOrigins(auth.getToken()).build();
        } catch (Exception ape) {
            ServicesLogger.LOGGER.failedToUpdatePassword(ape);
            errorEvent.detail(Details.REASON, ape.getMessage()).error(Errors.PASSWORD_REJECTED);
            //return account.setError(ape.getMessage()).createResponse(AccountPages.PASSWORD);
            return Cors.add(request, Response.serverError()).auth().allowedOrigins(auth.getToken()).build();
        }

        List<UserSessionModel> sessions = session.sessions().getUserSessions(realm, user);
        for (UserSessionModel s : sessions) {
            if (!s.getId().equals(auth.getSession().getId())) {
                AuthenticationManager.backchannelLogout(session, realm, s, uriInfo, clientConnection, headers, true);
            }
        }

        event.event(EventType.UPDATE_PASSWORD).client(auth.getClient()).user(auth.getUser()).success();

        return Cors.add(request, Response.ok()).auth().allowedOrigins(auth.getToken()).build(); 
    }
    
    private static boolean isPasswordSet(KeycloakSession session, RealmModel realm, UserModel user) {
        return session.userCredentialManager().isConfiguredFor(realm, user, CredentialModel.PASSWORD);
    }


    // TODO Federated identities
    // TODO Applications
    // TODO Logs

}
