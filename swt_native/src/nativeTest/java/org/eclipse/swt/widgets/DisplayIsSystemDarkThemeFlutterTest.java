package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Pins #817: isSystemDarkTheme() must reflect Config's force_theme, not a hardcoded value.
@Tag("flutter-it")
class DisplayIsSystemDarkThemeFlutterTest {

    private ConfigFlags savedFlags;

    @BeforeEach
    void setUp() {
        savedFlags = Config.getConfigFlags();
    }

    @AfterEach
    void tearDown() {
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void reflectsDarkForceTheme() {
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = "dark";
        Config.setConfigFlags(flags);

        assertThat(Display.isSystemDarkTheme()).isTrue();
    }

    @Test
    void reflectsLightForceTheme() {
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = "light";
        Config.setConfigFlags(flags);

        assertThat(Display.isSystemDarkTheme()).isFalse();
    }
}
