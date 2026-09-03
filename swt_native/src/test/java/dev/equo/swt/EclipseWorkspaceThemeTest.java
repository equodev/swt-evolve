package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The workbench theme ids Evolve has to map. "System" is the one that carries no variant of its
 * own: it delegates to the desktop, so classifying it as anything fixed would show a dark UI to
 * someone whose desktop is light.
 */
class EclipseWorkspaceThemeTest {

    private final Supplier<String> realOsAppearance = EclipseWorkspaceTheme.osAppearance;

    @AfterEach
    void restoreOsLookup() {
        EclipseWorkspaceTheme.osAppearance = realOsAppearance;
    }

    @ParameterizedTest
    @CsvSource({
            "org.eclipse.e4.ui.css.theme.e4_dark,    dark",
            "org.eclipse.e4.ui.css.theme.e4_default, light",
            "org.eclipse.e4.ui.css.theme.e4_classic, light",
    })
    void maps_the_themes_that_name_their_own_variant(String themeId, String expected) {
        assertThat(EclipseWorkspaceTheme.classify(themeId)).isEqualTo(expected);
    }

    @Test
    void the_system_theme_follows_the_desktop() {
        EclipseWorkspaceTheme.osAppearance = () -> "light";
        assertThat(EclipseWorkspaceTheme.classify("org.eclipse.e4.ui.css.theme.e4_system"))
                .isEqualTo("light");

        EclipseWorkspaceTheme.osAppearance = () -> "dark";
        assertThat(EclipseWorkspaceTheme.classify("org.eclipse.e4.ui.css.theme.e4_system"))
                .isEqualTo("dark");
    }

    @Test
    void the_system_theme_reports_nothing_when_the_desktop_cannot_be_read() {
        EclipseWorkspaceTheme.osAppearance = () -> null;
        assertThat(EclipseWorkspaceTheme.classify("org.eclipse.e4.ui.css.theme.e4_system")).isNull();
    }

    @Test
    void an_unknown_theme_reports_nothing() {
        assertThat(EclipseWorkspaceTheme.classify("com.acme.solarized")).isNull();
        assertThat(EclipseWorkspaceTheme.classify(null)).isNull();
    }
}
