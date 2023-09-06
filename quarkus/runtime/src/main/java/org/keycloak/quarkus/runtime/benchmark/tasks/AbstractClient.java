package org.keycloak.quarkus.runtime.benchmark.tasks;

import jakarta.ws.rs.core.MediaType;
import org.keycloak.common.constants.ServiceAccountConstants;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCConfigAttributes;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.managers.UserSessionManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

public abstract class AbstractClient implements BenchmarkTask {

    private KeycloakSession session;

    @Override
    public void init(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object run() {
        String clientId = getClientId();
        String algorithm = getAlgorithm();

        RealmModel realm = session.getContext().getRealm();
        ClientModel client = session.clients().getClientByClientId(realm, clientId);
        if (client == null) {
            client = session.clients().addClient(realm, clientId);
            client.setProtocol("openid-connect");
            client.setAttribute(OIDCConfigAttributes.ACCESS_TOKEN_SIGNED_RESPONSE_ALG, algorithm);
            client.setAttribute(OIDCConfigAttributes.ID_TOKEN_SIGNED_RESPONSE_ALG, algorithm);
            new ClientManager(new RealmManager(session)).enableServiceAccount(client);
        }

        session.getContext().setClient(client);

        UserModel clientUser = session.users().getServiceAccount(client);

        RootAuthenticationSessionModel rootAuthSession = new AuthenticationSessionManager(session).createAuthenticationSession(realm, false);
        AuthenticationSessionModel authSession = rootAuthSession.createAuthenticationSession(client);

        UserSessionModel userSession = new UserSessionManager(session).createUserSession(authSession.getParentSession().getId(), realm, clientUser, clientUser.getUsername(),
                session.getContext().getConnection().getRemoteAddr(), ServiceAccountConstants.CLIENT_AUTH, false, null, null, UserSessionModel.SessionPersistenceState.TRANSIENT);

        AuthenticationManager.setClientScopesInSession(authSession);
        ClientSessionContext clientSessionCtx = TokenManager.attachAuthenticationSession(session, userSession, authSession);

        EventBuilder event = new EventBuilder(realm, session);
        TokenManager.AccessTokenResponseBuilder responseBuilder = new TokenManager().responseBuilder(realm, client, event, session, userSession, clientSessionCtx)
                .generateAccessToken();

        return responseBuilder.build();
    }

    @Override
    public MediaType mediaType() {
        return MediaType.APPLICATION_JSON_TYPE;
    }

    public abstract String getClientId();
    public abstract String getAlgorithm();

}
