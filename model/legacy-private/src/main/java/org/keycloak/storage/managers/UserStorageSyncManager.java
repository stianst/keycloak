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
package org.keycloak.storage.managers;

import org.jboss.logging.Logger;
import org.keycloak.common.util.Time;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.LegacyRealmModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.user.ImportSynchronization;
import org.keycloak.storage.user.SynchronizationResult;
import org.keycloak.timer.TimerProvider;

import java.util.Objects;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserStorageSyncManager {

    private static final String USER_STORAGE_TASK_KEY = "user-storage";

    private static final Logger logger = Logger.getLogger(UserStorageSyncManager.class);

    /**
     * Check federationProviderModel of all realms and possibly start periodic sync for them
     *
     * @param sessionFactory
     * @param timer
     */
    public static void bootstrapPeriodic(final KeycloakSessionFactory sessionFactory, final TimerProvider timer) {

    }


    public static SynchronizationResult syncAllUsers(final KeycloakSessionFactory sessionFactory, final String realmId, final UserStorageProviderModel provider) {
        return null;
    }

    public static SynchronizationResult syncChangedUsers(final KeycloakSessionFactory sessionFactory, final String realmId, final UserStorageProviderModel provider) {
        return null;
    }


    public static void notifyToRefreshPeriodicSyncAll(KeycloakSession session, RealmModel realm, boolean removed) {
        ((LegacyRealmModel) realm).getUserStorageProvidersStream().forEachOrdered(fedProvider ->
           notifyToRefreshPeriodicSync(session, realm, fedProvider, removed));
    }
    
    public static void notifyToRefreshPeriodicSyncSingle(KeycloakSession session, RealmModel realm, ComponentModel component, boolean removed) {
        notifyToRefreshPeriodicSync(session, realm, new UserStorageProviderModel(component), removed);
    }
    
    // Ensure all cluster nodes are notified
    public static void notifyToRefreshPeriodicSync(KeycloakSession session, RealmModel realm, UserStorageProviderModel provider, boolean removed) {
        UserStorageProviderFactory factory = (UserStorageProviderFactory) session.getKeycloakSessionFactory().getProviderFactory(UserStorageProvider.class, provider.getProviderId());
        if (!(factory instanceof ImportSynchronization) || !provider.isImportEnabled()) {
            return;

        }
    }


    // Executed once it receives notification that some UserFederationProvider was created or updated
    protected static void refreshPeriodicSyncForProvider(final KeycloakSessionFactory sessionFactory, TimerProvider timer, final UserStorageProviderModel provider, final String realmId) {
        logger.debugf("Going to refresh periodic sync for provider '%s' . Full sync period: %d , changed users sync period: %d",
                provider.getName(), provider.getFullSyncPeriod(), provider.getChangedSyncPeriod());

        if (provider.getFullSyncPeriod() > 0) {
            // We want periodic full sync for this provider
            timer.schedule(new Runnable() {

                @Override
                public void run() {
                    try {
                        boolean shouldPerformSync = shouldPerformNewPeriodicSync(provider.getLastSync(), provider.getChangedSyncPeriod());
                        if (shouldPerformSync) {
                            syncAllUsers(sessionFactory, realmId, provider);
                        } else {
                            logger.debugf("Ignored periodic full sync with storage provider %s due small time since last sync", provider.getName());
                        }
                    } catch (Throwable t) {
                        logger.error("Error occurred during full sync of users", t);
                    }
                }

            }, provider.getFullSyncPeriod() * 1000, provider.getId() + "-FULL");
        } else {
            timer.cancelTask(provider.getId() + "-FULL");
        }

        if (provider.getChangedSyncPeriod() > 0) {
            // We want periodic sync of just changed users for this provider
            timer.schedule(new Runnable() {

                @Override
                public void run() {
                    try {
                        boolean shouldPerformSync = shouldPerformNewPeriodicSync(provider.getLastSync(), provider.getChangedSyncPeriod());
                        if (shouldPerformSync) {
                            syncChangedUsers(sessionFactory, realmId, provider);
                        } else {
                            logger.debugf("Ignored periodic changed-users sync with storage provider %s due small time since last sync", provider.getName());
                        }
                    } catch (Throwable t) {
                        logger.error("Error occurred during sync of changed users", t);
                    }
                }

            }, provider.getChangedSyncPeriod() * 1000, provider.getId() + "-CHANGED");

        } else {
            timer.cancelTask(provider.getId() + "-CHANGED");
        }
    }

    // Skip syncing if there is short time since last sync time.
    private static boolean shouldPerformNewPeriodicSync(int lastSyncTime, int period) {
        if (lastSyncTime <= 0) {
            return true;
        }

        int currentTime = Time.currentTime();
        int timeSinceLastSync = currentTime - lastSyncTime;

        return (timeSinceLastSync * 2 > period);
    }

    // Executed once it receives notification that some UserFederationProvider was removed
    protected static void removePeriodicSyncForProvider(TimerProvider timer, UserStorageProviderModel fedProvider) {
        logger.debugf("Removing periodic sync for provider %s", fedProvider.getName());
        timer.cancelTask(fedProvider.getId() + "-FULL");
        timer.cancelTask(fedProvider.getId() + "-CHANGED");
    }

    // Update interval of last sync for given UserFederationProviderModel. Do it in separate transaction
    private static void updateLastSyncInterval(final KeycloakSessionFactory sessionFactory, UserStorageProviderModel provider, final String realmId, final int lastSync) {
        KeycloakModelUtils.runJobInTransaction(sessionFactory, new KeycloakSessionTask() {

            @Override
            public void run(KeycloakSession session) {
                RealmModel persistentRealm = session.realms().getRealm(realmId);
                ((LegacyRealmModel) persistentRealm).getUserStorageProvidersStream()
                        .filter(persistentFedProvider -> Objects.equals(provider.getId(), persistentFedProvider.getId()))
                        .forEachOrdered(persistentFedProvider -> {
                            // Update persistent provider in DB
                            persistentFedProvider.setLastSync(lastSync);
                            persistentRealm.updateComponent(persistentFedProvider);

                            // Update "cached" reference
                            provider.setLastSync(lastSync);
                        });
            }
        });
    }



}
