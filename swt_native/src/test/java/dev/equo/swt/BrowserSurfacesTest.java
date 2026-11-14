package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** A build without a browser surface renders in a native window, and says so when asked for another. */
class BrowserSurfacesTest {

    private String savedMode;

    @BeforeEach
    void withoutBrowserSurface() {
        assumeTrue(BrowserSurfaces.find() == null, "this build includes a browser surface");
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
    }

    @AfterEach
    void restoreMode() {
        if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else System.setProperty(ConfigFlags.MODE_PROPERTY, savedMode);
    }

    @Test
    void an_unset_mode_starts_the_desktop() {
        System.clearProperty(ConfigFlags.MODE_PROPERTY);

        assertThat(BrowserSurfaces.open(null, () -> { })).isNull();

        assertThat(ConfigFlags.isDesktopMode()).isTrue();
    }

    @Test
    void the_mode_asked_for_is_kept() {
        System.setProperty(ConfigFlags.MODE_PROPERTY, "web");

        BrowserSurfaces.open(null, () -> { });

        assertThat(ConfigFlags.mode()).isEqualTo("web");
    }

    @Test
    void asking_for_the_web_is_a_plain_error() {
        System.setProperty(ConfigFlags.MODE_PROPERTY, "web");

        assertThatThrownBy(BrowserSurfaces::require)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("dev.equo.swt.mode=web is not available in this build, which renders in a native window only (dev.equo.swt.mode=desktop).");
    }
}
