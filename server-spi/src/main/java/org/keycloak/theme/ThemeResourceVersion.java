package org.keycloak.theme;

import org.keycloak.common.Version;
import org.keycloak.common.util.Base64Url;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class ThemeResourceVersion {

    private static byte[] NONCE;

    public static void setNone(String nonce) {
        ThemeResourceVersion.NONCE = nonce.getBytes(StandardCharsets.UTF_8);
    }

    public static String createResourceVersion(Theme theme) {
        try {
            String themeVersion = theme.getProperties().getProperty("version", "none");


            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(NONCE);
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
            System.out.println("Extending: " + themes.get(0).getType() + " " + themes.get(0).getName() + " " + resourceVersion);
            return resourceVersion;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
