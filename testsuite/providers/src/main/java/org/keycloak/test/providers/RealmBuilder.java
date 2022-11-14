package org.keycloak.test.providers;

import org.keycloak.models.RealmModel;
import org.keycloak.test.providers.mocks.TestRealmModel;

import java.util.HashMap;

public class RealmBuilder {

    public TestRealmModel realm;

    public static RealmBuilder standard() {
        return create("myrealm");
    }

    public static RealmBuilder create(String name) {
        return new RealmBuilder(name);
    }

    RealmBuilder(String name) {
        realm = new TestRealmModel();
        realm.name = name;
        realm.attributes = new HashMap<>();
    }

    public RealmBuilder attribute(String name, String value) {
        realm.attributes.put(name, value);
        return this;
    }

    public RealmBuilder loginTheme(String loginTheme) {
        realm.loginTheme = loginTheme;
        return this;
    }

    public RealmModel build() {
        return realm;
    }

}
