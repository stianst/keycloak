import org.junit.Test;
import org.keycloak.common.Profile;
import org.keycloak.test.providers.mocks.TestCloakSession;
import org.keycloak.test.providers.TestCloakBuilder;
import org.keycloak.theme.DefaultThemeSelectorProviderFactory;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeSelectorProvider;

import static org.junit.Assert.assertEquals;

public class SecondDefaultThemeSelectorProviderTest {

    @Test
    public void defaults() {
        TestCloakSession session = TestCloakBuilder.create()
                .providers().add(DefaultThemeSelectorProviderFactory.class)
                .build();

        ThemeSelectorProvider selector = session.getProvider(ThemeSelectorProvider.class, DefaultThemeSelectorProviderFactory.ID);
        assertEquals("keycloak", selector.getDefaultThemeName(Theme.Type.LOGIN));
        assertEquals("keycloak.v2", selector.getDefaultThemeName(Theme.Type.ACCOUNT));
    }

    @Test
    public void account2Disabled() {
        TestCloakSession session = TestCloakBuilder.create()
                .providers().add(DefaultThemeSelectorProviderFactory.class)
                .features().disable(Profile.Feature.ACCOUNT2)
                .build();

        ThemeSelectorProvider selector = session.getProvider(ThemeSelectorProvider.class, DefaultThemeSelectorProviderFactory.ID);
        assertEquals("keycloak", selector.getDefaultThemeName(Theme.Type.LOGIN));
        assertEquals("keycloak", selector.getDefaultThemeName(Theme.Type.ACCOUNT));
    }

    @Test
    public void account3Disabled() {
        TestCloakSession session = TestCloakBuilder.create()
                .providers().add(DefaultThemeSelectorProviderFactory.class)
                .features().disable(Profile.Feature.ACCOUNT2)
                .build();

        ThemeSelectorProvider selector = session.getProvider(ThemeSelectorProvider.class, DefaultThemeSelectorProviderFactory.ID);
        assertEquals("keycloak", selector.getDefaultThemeName(Theme.Type.LOGIN));
        assertEquals("keycloak", selector.getDefaultThemeName(Theme.Type.ACCOUNT));
    }

    @Test
    public void account4Disabled() {
        TestCloakSession session = TestCloakBuilder.create()
                .providers().add(DefaultThemeSelectorProviderFactory.class)
                .features().disable(Profile.Feature.ACCOUNT2)
                .build();

        ThemeSelectorProvider selector = session.getProvider(ThemeSelectorProvider.class, DefaultThemeSelectorProviderFactory.ID);
        assertEquals("keycloak", selector.getDefaultThemeName(Theme.Type.LOGIN));
        assertEquals("keycloak", selector.getDefaultThemeName(Theme.Type.ACCOUNT));
    }

}
