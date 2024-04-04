package org.keycloak.managerpoc;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ThemeManager;

public class ShowManagerIdea {

    public void show(KeycloakSession session) {
        // Gets a theme manager, but also exposes the theme manager to the public KeycloakSession interface
        ThemeManager theme = session.theme();

        // To be able to control what managers are available publicly vs internally could do something like

        // ThemeManager in public APIs, so publicly available
        ThemeManager themeManager = session.getManager(ThemeManager.class);

        // MyCustomManager is in private APIs, so should only be used internally within KC code
        MyCustomManager customManager = session.getManager(MyCustomManager.class);

        // Benefits:
        //
        // * Can support more re-use, especially for cases where we already have a lot of wrapper code around providers
        // * Can convert a lot of static utility classes to managers instead

        // Why not just a provider:
        //
        // Contract would be slightly different - a manager doesn't have configuration, there can only be a single
        // provider available

        // What would we need to do:
        //
        // Would need some way of registering the manager interfaces, some way of instantiating managers (factories perhaps?)

    }

}
