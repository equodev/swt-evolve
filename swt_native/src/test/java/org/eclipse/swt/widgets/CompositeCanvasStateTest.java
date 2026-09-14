package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Upstream sets the {@code CANVAS} state bit in {@code Composite#createHandle}. Scrollable,
 * Composite and Canvas all branch on it — scrollbar visibility and initial range, isTabGroup,
 * setFocus, traversalCode, and checkBuffered.
 *
 * checkBuffered is the one with a visible cost: draw2d's {@code LightweightSystem#setControl} picks
 * {@code NativeGraphicsSource} for a control carrying {@link SWT#DOUBLE_BUFFERED} and
 * {@code BufferedGraphicsSource} for one that does not. {@code FigureCanvas} asks for the style
 * explicitly, so losing the bit sends every GEF/draw2d repaint through an off-screen image that is
 * rasterized and blitted, instead of painting straight onto the control.
 *
 * Lives in org.eclipse.swt.widgets to reach the package-private state constants.
 */
@ExtendWith(Mocks.class)
class CompositeCanvasStateTest {

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
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a Composite carries the CANVAS state bit")
    void compositeIsACanvas() {
        Composite composite = new Composite(Mocks.swtShell(), SWT.NONE);
        assertThat(composite.state & DartWidget.CANVAS).isNotZero();
    }

    @Test
    @DisplayName("a Canvas carries the CANVAS state bit")
    void canvasIsACanvas() {
        Canvas canvas = new Canvas(Mocks.swtShell(), SWT.NONE);
        assertThat(canvas.state & DartWidget.CANVAS).isNotZero();
    }

    @Test
    @DisplayName("a Canvas asked for SWT.DOUBLE_BUFFERED keeps it — draw2d reads this style")
    void canvasKeepsDoubleBuffered() {
        Canvas canvas = new Canvas(Mocks.swtShell(), SWT.DOUBLE_BUFFERED);
        assertThat(canvas.getStyle() & SWT.DOUBLE_BUFFERED).isNotZero();
    }
}
