package org.keycloak.theme;

import org.keycloak.provider.Provider;

import java.util.List;

public interface ThemeResourceVersionProvider extends Provider {

    public String getThemeResourceVersion(List<Theme> themes);

}
