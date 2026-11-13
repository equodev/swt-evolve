package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Light or dark: the theme the application defines prevails; without one, Evolve follows the system
 * its client runs on (the browser on web, the OS on desktop); light when nothing answers, as native
 * SWT is.
 */
class ConfigThemeRuleTest {

    private ConfigFlags savedFlags;
    private Supplier<String> savedOsAppearance;
    private String savedMode;
    private int desktopReads;

    @BeforeEach
    void captureState() {
        savedFlags = Config.getConfigFlags();
        savedOsAppearance = SystemAppearance.osAppearance;
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        System.clearProperty(ConfigFlags.MODE_PROPERTY);
    }

    @AfterEach
    void restoreState() {
        SystemAppearance.osAppearance = savedOsAppearance;
        if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else System.setProperty(ConfigFlags.MODE_PROPERTY, savedMode);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void withNoThemeOfItsOwnTheApplicationFollowsTheBrowser() {
        appTheme(null);

        assertThat(Config.darkTheme(reported("dark"))).isTrue();
        assertThat(Config.darkTheme(reported("light"))).isFalse();
    }

    @Test
    void theApplicationsThemePrevailsOverTheSystem() {
        appTheme("light");
        assertThat(Config.darkTheme(reported("dark"))).isFalse();

        appTheme("dark");
        assertThat(Config.darkTheme(reported("light"))).isTrue();
    }

    @Test
    void aThemeNamingNoVariantIsNoThemeOfTheApplication() {
        appTheme("solarized");

        assertThat(Config.appTheme()).isNull();
        assertThat(Config.darkTheme(reported("dark"))).isTrue();
    }

    @Test
    void theVariantIsReadInAnyCase() {
        appTheme(" Dark ");

        assertThat(Config.darkTheme(reported("light"))).isTrue();
    }

    @Test
    void aWebDisplayIsLightUntilItsBrowserReports() {
        appTheme(null);
        desktopIs("dark");

        assertThat(Config.darkTheme(new SystemAppearance()))
                .as("on web the server's desktop is not the system the user sees")
                .isFalse();
        assertThat(desktopReads).isZero();
    }

    @Test
    void aDesktopDisplayFollowsTheDesktopUntilItsClientReports() {
        appTheme(null);
        desktopIs("dark");
        System.setProperty(ConfigFlags.MODE_PROPERTY, ConfigFlags.MODE_DESKTOP);
        SystemAppearance system = new SystemAppearance();

        assertThat(Config.darkTheme(system)).isTrue();
        assertThat(Config.darkTheme(system)).isTrue();
        assertThat(desktopReads).as("the desktop is read once per Display").isEqualTo(1);

        system.report("light");
        assertThat(Config.darkTheme(system)).as("the client's own report prevails").isFalse();
    }

    @Test
    void aDesktopThatCannotBeReadIsLight() {
        appTheme(null);
        desktopIs(null);
        System.setProperty(ConfigFlags.MODE_PROPERTY, ConfigFlags.MODE_DESKTOP);

        assertThat(Config.darkTheme(new SystemAppearance())).isFalse();
    }

    @Test
    void theApplicationsThemeNeverReadsTheDesktop() {
        appTheme("dark");
        desktopIs("light");
        System.setProperty(ConfigFlags.MODE_PROPERTY, ConfigFlags.MODE_DESKTOP);

        Config.darkTheme(new SystemAppearance());

        assertThat(desktopReads).isZero();
    }

    @Test
    void aReportNamingNoVariantIsIgnored() {
        SystemAppearance system = reported("dark");
        system.report("high-contrast");

        assertThat(system.reported()).isEqualTo("dark");
    }

    @Test
    void aNewWindowOpensInTheThemeItsClientWillPaint() {
        appTheme("dark");
        assertThat(Config.windowTheme(reported("light"))).isEqualTo("dark");

        appTheme(null);
        assertThat(Config.windowTheme(reported("light"))).isEqualTo("light");
        assertThat(Config.windowTheme(new SystemAppearance()))
                .as("before the client reports, the client resolves it against its own system")
                .isEqualTo("system");
    }

    private static void appTheme(String theme) {
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = theme;
        Config.setConfigFlags(flags);
    }

    private void desktopIs(String appearance) {
        SystemAppearance.osAppearance = () -> {
            desktopReads++;
            return appearance;
        };
    }

    private static SystemAppearance reported(String appearance) {
        SystemAppearance system = new SystemAppearance();
        system.report(appearance);
        return system;
    }
}
