package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The workbench theme ids Evolve has to map. "System" is the one that carries no variant of its
 * own: it leaves the answer to the system the client runs on, which on web is the browser, not
 * the desktop this JVM reads.
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
    void the_system_theme_names_no_variant_whatever_the_desktop() {
        EclipseWorkspaceTheme.osAppearance = () -> "dark";
        assertThat(EclipseWorkspaceTheme.classify("org.eclipse.e4.ui.css.theme.e4_system")).isNull();
        assertThat(EclipseWorkspaceTheme.classify("Default (System)")).isNull();
    }

    @Test
    void an_unknown_theme_reports_nothing() {
        assertThat(EclipseWorkspaceTheme.classify("com.acme.solarized")).isNull();
        assertThat(EclipseWorkspaceTheme.classify(null)).isNull();
    }
}
