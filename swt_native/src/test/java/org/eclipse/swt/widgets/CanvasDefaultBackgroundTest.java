package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;

/**
 * With SWT's colours a canvas has the background of any control, as upstream. With the theme's, a canvas is painted
 * as the theme's drawing surface, and the list background is that colour (white, or the dark surface), so
 * getBackground() reports what is drawn.
 */
@ExtendWith(Mocks.class)
class CanvasDefaultBackgroundTest {

    private boolean useSwtColors;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        useSwtColors = Config.getConfigFlags().use_swt_colors;
    }

    @AfterEach
    void tearDown() {
        Config.getConfigFlags().use_swt_colors = useSwtColors;
        FlutterBridge.set(null);
    }

    /** The system colour the canvas asks its display for. */
    private static void defaultOf(boolean useSwtColors, int expected) {
        ConfigFlags flags = Config.getConfigFlags();
        flags.use_swt_colors = useSwtColors;
        Canvas canvas = new Canvas(Mocks.shell(), SWT.NONE);
        DartDisplay display = (DartDisplay) canvas.getDisplay().getImpl();
        clearInvocations(display);
        ((DartCanvas) canvas.getImpl()).defaultBackground();
        verify(display).getWidgetColor(expected);
    }

    @Test
    @DisplayName("SWT's colours give a canvas the widget background")
    void swtColorsGiveTheWidgetBackground() {
        defaultOf(true, SWT.COLOR_WIDGET_BACKGROUND);
    }

    @Test
    @DisplayName("the theme's colours give a canvas the list background")
    void themeColorsGiveTheListBackground() {
        defaultOf(false, SWT.COLOR_LIST_BACKGROUND);
    }
}
