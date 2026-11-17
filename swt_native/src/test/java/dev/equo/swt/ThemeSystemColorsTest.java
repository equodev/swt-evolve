package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeSystemColorsTest {

    private static final RGB SELECTION = new RGB(1, 2, 3);

    private static final NamedThemeSystemColors SETS_SELECTION_IN_LIGHT_ONE =
            (themeName, dark, id) -> "one".equals(themeName) && !dark && id == SWT.COLOR_LIST_SELECTION ? SELECTION : null;

    @Test
    void without_a_provider_evolve_keeps_its_colours() {
        assertThat(ThemeSystemColors.of(Collections.emptyList(), "one", false, SWT.COLOR_LIST_SELECTION)).isNull();
    }

    @Test
    void a_provider_sets_only_the_theme_mode_and_colour_it_names() {
        List<NamedThemeSystemColors> providers = Collections.singletonList(SETS_SELECTION_IN_LIGHT_ONE);
        assertThat(ThemeSystemColors.of(providers, " one ", false, SWT.COLOR_LIST_SELECTION)).isEqualTo(SELECTION);
        assertThat(ThemeSystemColors.of(providers, "one", true, SWT.COLOR_LIST_SELECTION)).isNull();
        assertThat(ThemeSystemColors.of(providers, "two", false, SWT.COLOR_LIST_SELECTION)).isNull();
        assertThat(ThemeSystemColors.of(providers, null, false, SWT.COLOR_LIST_SELECTION)).isNull();
        assertThat(ThemeSystemColors.of(providers, "one", false, SWT.COLOR_LIST_SELECTION_TEXT)).isNull();
    }

    @Test
    void the_first_provider_that_sets_a_colour_wins() {
        RGB other = new RGB(9, 9, 9);
        NamedThemeSystemColors setsEverything = (themeName, dark, id) -> other;
        assertThat(ThemeSystemColors.of(Arrays.asList(SETS_SELECTION_IN_LIGHT_ONE, setsEverything), "one", false, SWT.COLOR_LIST_SELECTION))
                .isEqualTo(SELECTION);
        assertThat(ThemeSystemColors.of(Arrays.asList(SETS_SELECTION_IN_LIGHT_ONE, setsEverything), "one", false, SWT.COLOR_LIST_FOREGROUND))
                .isEqualTo(other);
    }
}
