/*
 * Copyright 2017 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.keycloak.services.resources.account;

import java.util.LinkedList;
import java.util.List;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2017 Red Hat Inc.
 */
public class SkinnyApplicationsBean {
    
    private List<SkinnyApplicationsBean.ApplicationEntry> applications = new LinkedList<SkinnyApplicationsBean.ApplicationEntry>();

    public SkinnyApplicationsBean(KeycloakSession session, RealmModel realm, UserModel user) {
    
        List<ClientModel> realmClients = realm.getClients();
        for (ClientModel client : realmClients) {
            // Don't show bearerOnly clients
            if (client.isBearerOnly()) {
                continue;
            }
            
            ApplicationEntry appEntry = new ApplicationEntry(client);
            applications.add(appEntry);
        }
    }
    
    public List<SkinnyApplicationsBean.ApplicationEntry> getApplications() {
        return this.applications;
    }
    
    public static class ApplicationEntry {
        private final String clientId;
        private final String name;
        private final String description;
        private final String effectiveUrl;
        
        public ApplicationEntry(ClientModel client) {
            this.clientId = client.getClientId();
            this.name = client.getName();
            this.description = client.getDescription();
            this.effectiveUrl = calculateEffectiveUrl(client);
        }
        
        public String getClientId() {
            return this.clientId;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getDescription() {
            return this.description;
        }
        
        public String getEffectiveUrl() {
            return effectiveUrl;
        }
        
        private String calculateEffectiveUrl(ClientModel client) {
            String rootUrl = client.getRootUrl();
            String baseUrl = client.getBaseUrl();
            
            if (rootUrl == null) rootUrl = "";
            if (baseUrl == null) baseUrl = "";
            
            if (rootUrl.equals("") && baseUrl.equals("")) {
                return "";
            }
            
            if (rootUrl.equals("") && !baseUrl.equals("")) {
                return baseUrl;
            }
            
            if (!rootUrl.equals("") && baseUrl.equals("")) {
                return rootUrl;
            }
            
            if (isBaseUrlRelative(baseUrl) && !rootUrl.equals("")) {
                return concatUrls(rootUrl, baseUrl);
            }
            
            return baseUrl;
        }
        
        private String concatUrls(String u1, String u2) {
            if (u1.endsWith("/")) u1 = u1.substring(0, u1.length() - 1);
            if (u2.startsWith("/")) u2 = u2.substring(1);
            return u1 + "/" + u2;
        }
        
        private boolean isBaseUrlRelative(String baseUrl) {
            if (baseUrl.equals("")) return false;
            if (baseUrl.startsWith("/")) return true;
            if (baseUrl.startsWith("./")) return true;
            if (baseUrl.startsWith("../")) return true;
            return false;
        }
        
    }

}
