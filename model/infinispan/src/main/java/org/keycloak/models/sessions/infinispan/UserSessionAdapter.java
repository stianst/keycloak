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

package org.keycloak.models.sessions.infinispan;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.sessions.infinispan.entities.AuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.AuthenticatedClientSessionStore;
import org.keycloak.models.sessions.infinispan.entities.UserSessionEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class UserSessionAdapter implements UserSessionModel {

    private final KeycloakSession session;

    private final InfinispanUserSessionProvider provider;


    private final RealmModel realm;

    private final UserModel user;

    private final UserSessionEntity entity;

    private final boolean offline;

    private SessionPersistenceState persistenceState;

    public UserSessionAdapter(KeycloakSession session, UserModel user, InfinispanUserSessionProvider provider,
                              RealmModel realm, UserSessionEntity entity, boolean offline) {
        this.session = session;
        this.user = user;
        this.provider = provider;
        this.realm = realm;
        this.entity = entity;
        this.offline = offline;
    }

    @Override
    public Map<String, AuthenticatedClientSessionModel> getAuthenticatedClientSessions() {
        Map<String, AuthenticatedClientSessionModel> result = new HashMap<>();

        List<String> removedClientUUIDS = new LinkedList<>();

        if (entity.getAuthenticatedClientSessions() != null) {
            entity.getAuthenticatedClientSessions().keySet().forEach(key -> {
                // Check if client still exists
                ClientModel client = realm.getClientById(key);
                if (client != null) {
                    final AuthenticatedClientSessionAdapter clientSession = provider.getClientSession(this, client, key, offline);
                    if (clientSession != null) {
                        result.put(key, clientSession);
                    }
                } else {
                    removedClientUUIDS.add(key);
                }
            });
        }

        removeAuthenticatedClientSessions(removedClientUUIDS);

        return Collections.unmodifiableMap(result);
    }

    @Override
    public AuthenticatedClientSessionModel getAuthenticatedClientSessionByClient(String clientUUID) {
        Optional<AuthenticatedClientSessionEntity> first =
                entity.getAuthenticatedClientSessions().values().stream().filter(s -> s.getClientId().equals(clientUUID)).findFirst();

        if (first.isPresent()) {
            ClientModel client = realm.getClientById(clientUUID);
            if (client == null) {
                return null;
            }
            return provider.getClientSession(this, client, first.get().getId(), offline);
        } else {
            return null;
        }
    }

    private static final int MINIMUM_INACTIVE_CLIENT_SESSIONS_TO_CLEANUP = 5;

    @Override
    public void removeAuthenticatedClientSessions(Collection<String> removedClientUUIDS) {


    }

    public String getId() {
        return entity.getId();
    }

    @Override
    public RealmModel getRealm() {
        return realm;
    }

    @Override
    public String getBrokerSessionId() {
        return entity.getBrokerSessionId();
    }

    @Override
    public String getBrokerUserId() {
        return entity.getBrokerUserId();
    }

    public UserModel getUser() {
        return this.user;
    }

    @Override
    public String getLoginUsername() {
        if (entity.getLoginUsername() == null) {
            // this is a hack so that UserModel doesn't have to be available when offline token is imported.
            // see related JIRA - KEYCLOAK-5350 and corresponding test
            return getUser().getUsername();
        } else {
            return entity.getLoginUsername();
        }
    }

    public String getIpAddress() {
        return entity.getIpAddress();
    }

    @Override
    public String getAuthMethod() {
        return entity.getAuthMethod();
    }

    @Override
    public boolean isRememberMe() {
        return entity.isRememberMe();
    }

    public int getStarted() {
        return entity.getStarted();
    }

    public int getLastSessionRefresh() {
        return entity.getLastSessionRefresh();
    }

    public void setLastSessionRefresh(int lastSessionRefresh) {
        entity.setLastSessionRefresh(lastSessionRefresh);
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public String getNote(String name) {
        return entity.getNotes() != null ? entity.getNotes().get(name) : null;
    }

    @Override
    public void setNote(String name, String value) {
        entity.getNotes().put(name, value);
    }

    @Override
    public void removeNote(String name) {
        entity.getNotes().remove(name);
    }

    @Override
    public Map<String, String> getNotes() {
        return entity.getNotes();
    }

    @Override
    public State getState() {
        return entity.getState();
    }

    @Override
    public void setState(State state) {
       entity.setState(state);
    }

    public SessionPersistenceState getPersistenceState() {
        return persistenceState;
    }

    public void setPersistenceState(SessionPersistenceState persistenceState) {
        this.persistenceState = persistenceState;
    }

    @Override
    public void restartSession(RealmModel realm, UserModel user, String loginUsername, String ipAddress, String authMethod, boolean rememberMe, String brokerSessionId, String brokerUserId) {
                entity.setState(null);
                entity.getNotes().clear();
                entity.getAuthenticatedClientSessions().clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof UserSessionModel)) {
            return false;
        }

        UserSessionModel that = (UserSessionModel) o;
        return that.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    UserSessionEntity getEntity() {
        return entity;
    }


}
