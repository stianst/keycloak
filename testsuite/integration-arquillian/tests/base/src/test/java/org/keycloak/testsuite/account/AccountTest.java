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
package org.keycloak.testsuite.account;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.representations.account.SessionRepresentation;
import org.keycloak.representations.account.UserRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.testsuite.AbstractTestRealmKeycloakTest;
import org.keycloak.testsuite.AssertEvents;
import org.keycloak.testsuite.util.TokenUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.keycloak.services.resources.account.AccountService.PasswordChangeRequest;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AccountTest extends AbstractTestRealmKeycloakTest {

    @Rule
    public TokenUtil tokenUtil = new TokenUtil();

    @Rule
    public AssertEvents events = new AssertEvents(this);

    private CloseableHttpClient client;

    @Before
    public void before() {
        client = HttpClientBuilder.create().build();
    }

    @After
    public void after() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configureTestRealm(RealmRepresentation testRealm) {
    }

    @Test
    public void testGetProfile() throws IOException {
        UserRepresentation user = SimpleHttp.doGet(getAccountUrl(null), client).auth(tokenUtil.getToken()).asJson(UserRepresentation.class);
        assertEquals("Tom", user.getFirstName());
        assertEquals("Brady", user.getLastName());
        assertEquals("test-user@localhost", user.getEmail());
        assertFalse(user.isEmailVerified());
        assertTrue(user.getAttributes().isEmpty());
    }

    @Test
    public void testUpdateProfile() throws IOException {
        UserRepresentation user = SimpleHttp.doGet(getAccountUrl(null), client).auth(tokenUtil.getToken()).asJson(UserRepresentation.class);

        int status = SimpleHttp.doPost(getAccountUrl(null), client).auth(tokenUtil.getToken()).json(user).asStatus();
        assertEquals(200, status);
    }

    @Test
    public void testGetSessions() throws IOException {
        List<SessionRepresentation> sessions = SimpleHttp.doGet(getAccountUrl("sessions"), client).auth(tokenUtil.getToken()).asJson(new TypeReference<List<SessionRepresentation>>() {});

        assertEquals(1, sessions.size());
    }
    
    @Test
    public void testUpdatePassword() throws IOException {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setPassword("password");
        request.setNewPassword("foo");
        request.setConfirmation("foo");
        
        // First one success
        assertEquals(200, updatePassword(request));

        // Second time old password is no longer "password"
        assertEquals(412, updatePassword(request));

        // success
        request.setPassword("foo");
        request.setNewPassword("bar");
        request.setConfirmation("bar");
        assertEquals(200, updatePassword(request));
        
        // I have no idea why I need to reset the client.  For some reason it
        // get stuck after a certain number of requests.  It freezes on line
        // 199 of SimpleHttp where it says "return client.execute(httpRequest);"
        after();
        before();
        
        // mismatched confirmation
        request.setPassword("bar");
        request.setNewPassword("cookiemonster");
        request.setConfirmation("bigbird");
        assertEquals(412, updatePassword(request));
        
        after();
        before();
        
        // password blank
        request.setPassword("");
        request.setNewPassword("cookiemonster");
        request.setConfirmation("cookiemonster");
        assertEquals(412, updatePassword(request));
        
        after();
        before();
        
        // new password blank
        request.setPassword("bar");
        request.setNewPassword("");
        request.setConfirmation("cookiemonster");
        assertEquals(412, updatePassword(request));
        
        after();
        before();
        
        // confirmation blank
        request.setPassword("bar");
        request.setNewPassword("cookiemonster");
        request.setConfirmation("");
        assertEquals(412, updatePassword(request));
        
        after();
        before();
        
        // set back to original
        request.setPassword("bar");
        request.setNewPassword("password");
        request.setConfirmation("password");
        assertEquals(200, updatePassword(request));
    }
    
    private int updatePassword(PasswordChangeRequest request) throws IOException {
        return SimpleHttp.doPost(getAccountUrl("credentials"), client).auth(tokenUtil.getToken()).json(request).asStatus();
    }
    
    private String getAccountUrl(String resource) {
        return suiteContext.getAuthServerInfo().getContextRoot().toString() + "/auth/realms/test/account" + (resource != null ? "/" + resource : "");
    }

}
