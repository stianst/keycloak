package org.keycloak.tests;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.testframework.annotations.InjectUser;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.oauth.OAuthClient;
import org.keycloak.testframework.oauth.annotations.InjectOAuthClient;
import org.keycloak.testframework.realm.ManagedUser;
import org.keycloak.testframework.realm.UserConfigBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@KeycloakIntegrationTest
public class LoginUsingHttpClientTest {

    @InjectOAuthClient
    OAuthClient oauth;

    @InjectUser(config = UserConfig.class)
    ManagedUser user;

    @Test
    public void testLogin() throws IOException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        CloseableHttpClient httpClient = builder.build();

        // Open login form

        HttpGet loginFormRequest = new HttpGet(oauth.loginForm().build());

        loginFormRequest.setHeader("User-Agent", "Mozilla/5.0");
        loginFormRequest.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        loginFormRequest.setHeader("Accept-Language", "en-US,en;q=0.5");

        HttpResponse  loginFormResponse = httpClient.execute(loginFormRequest);

        String loginForm = EntityUtils.toString(loginFormResponse.getEntity());

        Assertions.assertEquals(200, loginFormResponse.getStatusLine().getStatusCode());

        // Submit login form

        List<NameValuePair> paramList = new ArrayList<>();
        paramList.add(new BasicNameValuePair("username", "user"));
        paramList.add(new BasicNameValuePair("password", "pass"));

        String actionUrl = getActionUrl(loginForm);

        HttpPost submitLoginFormRequest = new HttpPost(actionUrl);

        // Hack to just take cookies directly from response and send in next request bypassing cookie store
        String cookies = Arrays.stream(loginFormResponse.getAllHeaders()).filter(h -> h.getName().equals("Set-Cookie")).map(h -> h.getValue().split(";")[0]).collect(Collectors.joining("; "));
        submitLoginFormRequest.setHeader("Cookie", cookies);

        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8);
        submitLoginFormRequest.setEntity(formEntity);

        HttpResponse submitLoginFormResponse = httpClient.execute(submitLoginFormRequest);

        // Check redirecting to app
        Assertions.assertEquals(302, submitLoginFormResponse.getStatusLine().getStatusCode());
        Assertions.assertTrue(submitLoginFormResponse.getFirstHeader("Location").getValue().contains("callback/oauth"));
    }

    protected String getActionUrl(String html) {
        Pattern pattern = Pattern.compile("action=\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(html);
        matcher.find();
        String action = matcher.group(1);
        return action;
    }

    public static class UserConfig implements org.keycloak.testframework.realm.UserConfig {

        @Override
        public UserConfigBuilder configure(UserConfigBuilder user) {
            return user.username("user").password("pass").email("user@local").name("First", "Last");
        }
    }

}
