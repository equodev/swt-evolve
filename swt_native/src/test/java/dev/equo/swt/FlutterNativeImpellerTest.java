package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Impeller stays the Windows renderer unless shells can get windows, and engines, of their own. */
class FlutterNativeImpellerTest {

    private String savedMode;
    private String savedImpeller;

    @BeforeEach
    void save() {
        savedMode = System.getProperty(WindowPolicy.MODE_PROPERTY);
        savedImpeller = System.getProperty(FlutterNative.IMPELLER_PROPERTY);
        System.clearProperty(WindowPolicy.MODE_PROPERTY);
        System.clearProperty(FlutterNative.IMPELLER_PROPERTY);
    }

    @AfterEach
    void restore() {
        WindowPolicy.setResolver(null);
        restore(WindowPolicy.MODE_PROPERTY, savedMode);
        restore(FlutterNative.IMPELLER_PROPERTY, savedImpeller);
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    @Test
    void onWithASingleWindow() {
        assertThat(FlutterNative.impellerEnabled()).isTrue();
    }

    @Test
    void offWhenShellsGetWindowsOfTheirOwn() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        assertThat(FlutterNative.impellerEnabled()).isFalse();

        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        assertThat(FlutterNative.impellerEnabled()).isFalse();
    }

    @Test
    void offWhenTheApplicationDecidesWhichShellsGetWindows() {
        WindowPolicy.setResolver((shell, isMainShell) -> null);
        assertThat(FlutterNative.impellerEnabled()).isFalse();
    }

    @Test
    void thePropertyForcesEitherWay() {
        System.setProperty(FlutterNative.IMPELLER_PROPERTY, "true");
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        assertThat(FlutterNative.impellerEnabled()).isTrue();

        System.setProperty(FlutterNative.IMPELLER_PROPERTY, "false");
        System.clearProperty(WindowPolicy.MODE_PROPERTY);
        assertThat(FlutterNative.impellerEnabled()).isFalse();
    }
}
