package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/**
 * The theme colours {@link DartText} keeps per appearance. Apart from
 * {@link TextImplementationNativeTest} because the members it drives only exist from SWT 3.116.
 */
@Tag("native-unit")
class TextThemeColorsNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        // The display mock is shared by the whole suite: hand it back as the other tests expect it.
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private DartText impl(int style) {
        return (DartText) new Text(DartMocks.dartShell(display), style).getImpl();
    }

    @Test
    @DisplayName("the theme colours of a text are refreshed once per appearance, and only for a multi-line text")
    void themeColorsAreRefreshedOncePerAppearance() {
        DartText multi = impl(SWT.MULTI);
        DartText single = impl(SWT.SINGLE);
        multi.lastAppAppearance = Display.APPEARANCE.Dark;
        single.lastAppAppearance = Display.APPEARANCE.Dark;

        multi.updateThemeColors();
        single.updateThemeColors();
        multi.updateThemeColors();

        assertThat(multi.lastAppAppearance).isEqualTo(((DartDisplay) display.getImpl()).appAppearance);
        assertThat(single.lastAppAppearance).isEqualTo(((DartDisplay) display.getImpl()).appAppearance);
    }
}
