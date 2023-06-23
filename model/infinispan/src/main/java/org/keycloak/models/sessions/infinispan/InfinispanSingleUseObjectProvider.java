/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.sessions.infinispan.entities.SingleUseObjectValueEntity;

import java.util.Map;

/**
 * TODO: Check if Boolean can be used as single-use cache argument instead of SingleUseObjectValueEntity. With respect to other single-use cache usecases like "Revoke Refresh Token" .
 * Also with respect to the usage of streams iterating over "actionTokens" cache (check there are no ClassCastExceptions when casting values directly to SingleUseObjectValueEntity)
 *
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanSingleUseObjectProvider implements SingleUseObjectProvider {

    public static final Logger logger = Logger.getLogger(InfinispanSingleUseObjectProvider.class);

    private final Map<String, SingleUseObjectValueEntity> singleUseObjectCache;
    private final KeycloakSession session;

    public InfinispanSingleUseObjectProvider(KeycloakSession session, Map<String, SingleUseObjectValueEntity> singleUseObjectCache) {
        this.session = session;
        this.singleUseObjectCache = singleUseObjectCache;
    }

    @Override
    public void put(String key, long lifespanSeconds, Map<String, String> notes) {
        SingleUseObjectValueEntity tokenValue = new SingleUseObjectValueEntity(notes);
        singleUseObjectCache.put(key, tokenValue);
    }

    @Override
    public Map<String, String> get(String key) {
        SingleUseObjectValueEntity singleUseObjectValueEntity;
        singleUseObjectValueEntity = singleUseObjectCache.get(key);
        return singleUseObjectValueEntity != null ? singleUseObjectValueEntity.getNotes() : null;
    }

    @Override
    public Map<String, String> remove(String key) {
        SingleUseObjectValueEntity singleUseObjectValueEntity = singleUseObjectCache.get(key);
        if (singleUseObjectValueEntity != null) {
            singleUseObjectCache.remove(key);
            return singleUseObjectValueEntity.getNotes();
        }
        return null;
    }

    @Override
    public boolean replace(String key, Map<String, String> notes) {
        return singleUseObjectCache.replace(key, new SingleUseObjectValueEntity(notes)) != null;
    }

    @Override
    public boolean putIfAbsent(String key, long lifespanInSeconds) {
        SingleUseObjectValueEntity tokenValue = new SingleUseObjectValueEntity(null);

        SingleUseObjectValueEntity existing = singleUseObjectCache.putIfAbsent(key, tokenValue);
        return existing == null;
    }

    @Override
    public boolean contains(String key) {
        return singleUseObjectCache.containsKey(key);
    }

    @Override
    public void close() {

    }
}
