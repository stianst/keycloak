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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.Auth;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeProvider;

/**
 * Simple class to localize messages.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2017 Red Hat Inc.
 */
public class UIMessages {
    private static final Logger logger = Logger.getLogger(UIMessages.class);

    private Properties messagesBundle;
    
    public UIMessages(KeycloakSession session, Auth auth) {
        setUpMessages(session, auth);
    }
    
    private void setUpMessages(KeycloakSession session, Auth auth) {
        Locale locale = session.getContext().resolveLocale(auth.getUser());
        ThemeProvider themeProvider = session.getProvider(ThemeProvider.class, "extending");
        RealmModel realm = auth.getRealm();
        
        try {
            Theme theme = themeProvider.getTheme(realm.getAccountTheme(), Theme.Type.ACCOUNT);
            this.messagesBundle = theme.getMessages(locale);
        } catch (IOException e) {
            logger.warn("Failed to load messages", e);
            messagesBundle = new Properties();
        }
    }
    
    public String localize(String messageKey, Object... parameters) {
        if (messageKey == null) return null;
        
        if (this.messagesBundle.containsKey(messageKey)) {
            return new MessageFormat(this.messagesBundle.getProperty(messageKey)).format(parameters);
        }
        
        return messageKey;
    }
}
