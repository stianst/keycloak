import org.junit.Before;
import org.junit.Test;
import org.keycloak.common.Profile;
import org.keycloak.test.providers.mocks.TestCloakSession;
import org.keycloak.test.providers.TestCloakBuilder;
import org.keycloak.theme.DefaultThemeSelectorProviderFactory;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeSelectorProvider;
import org.keycloak.test.providers.ClientBuilder;
import org.keycloak.test.providers.RealmBuilder;

import static org.junit.Assert.assertEquals;
import static org.keycloak.theme.DefaultThemeSelectorProvider.LOGIN_THEME_KEY;

public class DefaultThemeSelectorProviderTest {

    private TestCloakBuilder sb;

    @Before
    public void createDefaultBuilder() {
        sb = TestCloakBuilder.create()
                .providers().add(DefaultThemeSelectorProviderFactory.class)
                .context().realm(RealmBuilder.standard())
                .context().client(ClientBuilder.standard());
    }

    @Test
    public void themeName() {
        TestCloakSession session = sb.build();

        ThemeSelectorProvider selector = session.getProvider(ThemeSelectorProvider.class, DefaultThemeSelectorProviderFactory.ID);
        assertEquals("keycloak", selector.getThemeName(Theme.Type.LOGIN));
    }

    @Test
    public void themeNameRealmOverrides() {
        TestCloakSession session = sb.context().realm(RealmBuilder.standard().loginTheme("myRealmTheme")).build();

        ThemeSelectorProvider selector = session.getProvider(ThemeSelectorProvider.class, DefaultThemeSelectorProviderFactory.ID);
        assertEquals("myRealmTheme", selector.getThemeName(Theme.Type.LOGIN));
    }

    @Test
    public void themeNameClientOverrides() {
        TestCloakSession session = sb.context().client(ClientBuilder.standard().attribute(LOGIN_THEME_KEY, "myClientTheme")).build();


        ThemeSelectorProvider selector = session.getProvider(ThemeSelectorProvider.class, DefaultThemeSelectorProviderFactory.ID);
        assertEquals("myClientTheme", selector.getThemeName(Theme.Type.LOGIN));
    }

    @Test
    public void defaultThemeName() {
        TestCloakSession session = sb.build();

        ThemeSelectorProvider selector = session.getProvider(ThemeSelectorProvider.class, DefaultThemeSelectorProviderFactory.ID);
        assertEquals("keycloak", selector.getDefaultThemeName(Theme.Type.LOGIN));
        assertEquals("keycloak.v2", selector.getDefaultThemeName(Theme.Type.ACCOUNT));
    }

    @Test
    public void defaultThemeNameAccount2Disabled() {
        TestCloakSession session = sb.features().disable(Profile.Feature.ACCOUNT2).build();

        ThemeSelectorProvider selector = session.getProvider(ThemeSelectorProvider.class, DefaultThemeSelectorProviderFactory.ID);
        assertEquals("keycloak", selector.getDefaultThemeName(Theme.Type.LOGIN));
        assertEquals("keycloak", selector.getDefaultThemeName(Theme.Type.ACCOUNT));
    }

    @Test
    public void defaultThemeNameServerConfig() {
        TestCloakSession session = sb.config().set("theme.default", "mytheme").build();

        ThemeSelectorProvider selector = session.getProvider(ThemeSelectorProvider.class, DefaultThemeSelectorProviderFactory.ID);
        assertEquals("mytheme", selector.getDefaultThemeName(Theme.Type.LOGIN));
    }

}
