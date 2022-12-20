package org.keycloak.theme;

import org.keycloak.Config;
import org.keycloak.common.Version;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class DefaultThemeResourceVersionProvider implements ThemeResourceVersionProvider, ThemeResourceVersionProviderFactory {

    private final String THEME_RESOURCE_VERSION_NONCE_KEY = "themeResourceVersionNonce";
    private String nonce;

    @Override
    public ThemeResourceVersionProvider create(KeycloakSession session) {
        if (nonce == null) {
            initNonce(session);
        }

        return this;
    }

    public String getThemeResourceVersion(List<Theme> themes) {
        return "fixed";
    }

    private synchronized void initNonce(KeycloakSession session) {
        if (nonce == null) {
            RealmModel masterRealm = session.realms().getRealmByName(Config.getAdminRealm());
            nonce = masterRealm.getAttribute(THEME_RESOURCE_VERSION_NONCE_KEY);
            if (nonce == null) {
                nonce = SecretGenerator.getInstance().randomString(32);
                masterRealm.setAttribute(THEME_RESOURCE_VERSION_NONCE_KEY, nonce);
            }
        }
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "default";
    }

    public String createResourceVersion(Theme theme) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(nonce.getBytes(StandardCharsets.UTF_8));

            String themeVersion = theme.getProperties().getProperty("version", "none");
            if (themeVersion != null) {
                digest.update(themeVersion.getBytes(StandardCharsets.UTF_8));
            } else {
                digest.update(Version.VERSION.getBytes(StandardCharsets.UTF_8));
            }

            String resourceVersion =  Base64Url.encode(digest.digest());
            System.out.println(theme.getType() + " " + theme.getName() + " " + resourceVersion);
            return resourceVersion;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static String createResourceVersion(List<Theme> themes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            for (Theme t : themes) {
                digest.update(t.getResourceVersion().getBytes(StandardCharsets.UTF_8));
            }

            String resourceVersion = Base64Url.encode(digest.digest());
            return resourceVersion;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
