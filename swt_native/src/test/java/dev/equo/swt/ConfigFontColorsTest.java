package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code swt.use_swt_font_colors} and its per-widget form {@code swt.evolve.use_swt_font_colors_<widget>}
 * reach the flags only when set, so the Flutter side can fall back to {@code use_swt_fonts}.
 */
class ConfigFontColorsTest {

    private static final String GLOBAL = "swt.use_swt_font_colors";
    private static final String BY_WIDGET = "swt.evolve.use_swt_font_colors_";
    private static final List<String> KEYS = List.of(GLOBAL, BY_WIDGET + "Tree", BY_WIDGET + "table", BY_WIDGET);

    private ConfigFlags savedFlags;

    @BeforeEach
    void captureState() {
        savedFlags = Config.getConfigFlags();
        KEYS.forEach(System::clearProperty);
        Config.setConfigFlags(null);
    }

    @AfterEach
    void restoreState() {
        KEYS.forEach(System::clearProperty);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void unset_follows_use_swt_fonts() {
        ConfigFlags flags = Config.getConfigFlags();
        assertThat(flags.use_swt_font_colors).isNull();
        assertThat(flags.use_swt_font_colors_by_widget).isNull();
    }

    @Test
    void explicit_false_is_kept_apart_from_unset() {
        System.setProperty(GLOBAL, "false");
        assertThat(Config.getConfigFlags().use_swt_font_colors).isFalse();
    }

    @Test
    void per_widget_values_are_keyed_by_lower_case_name() {
        System.setProperty(BY_WIDGET + "Tree", "true");
        System.setProperty(BY_WIDGET + "table", "false");
        System.setProperty(BY_WIDGET, "true");

        assertThat(Config.getConfigFlags().use_swt_font_colors_by_widget)
                .isEqualTo(Map.of("tree", true, "table", false));
    }
}
