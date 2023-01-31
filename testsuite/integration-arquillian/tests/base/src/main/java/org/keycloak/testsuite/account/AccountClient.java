package org.keycloak.testsuite.account;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.representations.account.UserRepresentation;
import org.keycloak.testsuite.util.OAuthClient;
import org.keycloak.testsuite.util.ServerURLs;
import org.keycloak.testsuite.util.TokenUtil;

import java.io.IOException;
import java.util.function.Supplier;

public class AccountClient extends TestWatcher {

    private Supplier<CloseableHttpClient> httpClient = OAuthClient::newCloseableHttpClient;
    private String realm = "test";
    private TokenUtil tokenUtil;

    protected AccountClient(String realm, String username, String password) {
        this.realm = realm;
        this.tokenUtil = new TokenUtil();
    }

    public static AccountClient withDefaults() {
        return new AccountClientBuilder().build();
    }

    public static AccountClientBuilder build() {
        return new AccountClientBuilder();
    }

    @Override
    protected void starting(Description description) {
        super.starting(description);
        httpClient = OAuthClient.newCloseableHttpClient();
    }

    @Override
    protected void finished(Description description) {
        try {
            httpClient.close();
        } catch (IOException e) {
            throw new AccountClientException(e);
        }
    }

    public UserRepresentation getUser() throws IOException {
        return getUser(true);
    }

    public UserRepresentation getUser(boolean fetchMetadata) throws AccountClientException {
        String accountUrl = getAccountUrl(null) + "?userProfileMetadata=" + fetchMetadata;
        SimpleHttp a = SimpleHttp.doGet(accountUrl, httpClient).auth(token);

        try {
            return a.asJson(UserRepresentation.class);
        } catch (IOException e) {
            throw new AccountClientException("Failed to get user", e);
        }
    }

    public String getAccountUrl(String resource) {
        String url = ServerURLs.getAuthServerContextRoot() + "/auth/realms/" + realm + "/account";
        if (resource != null) {
            url += "/" + resource;
        }
        return url;
    }

}
