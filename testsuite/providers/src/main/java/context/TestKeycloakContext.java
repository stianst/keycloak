package context;

import org.keycloak.common.ClientConnection;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.urls.UrlType;

import javax.ws.rs.core.HttpHeaders;
import java.net.URI;
import java.util.Locale;

public class TestKeycloakContext implements KeycloakContext {
    private RealmModel realm;
    private ClientModel client;

    @Override
    public URI getAuthServerUrl() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public KeycloakUriInfo getUri() {
        return null;
    }

    @Override
    public KeycloakUriInfo getUri(UrlType type) {
        return null;
    }

    @Override
    public HttpHeaders getRequestHeaders() {
        return null;
    }

    @Override
    public <T> T getContextObject(Class<T> clazz) {
        return null;
    }

    @Override
    public RealmModel getRealm() {
        return realm;
    }

    @Override
    public void setRealm(RealmModel realm) {
        this.realm = realm;
    }

    @Override
    public ClientModel getClient() {
        return client;
    }

    @Override
    public void setClient(ClientModel client) {
        this.client = client;
    }

    @Override
    public ClientConnection getConnection() {
        return null;
    }

    @Override
    public Locale resolveLocale(UserModel user) {
        return null;
    }

    @Override
    public AuthenticationSessionModel getAuthenticationSession() {
        return null;
    }

    @Override
    public void setAuthenticationSession(AuthenticationSessionModel authenticationSession) {

    }

}
