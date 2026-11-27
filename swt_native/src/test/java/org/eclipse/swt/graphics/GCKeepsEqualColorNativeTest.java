package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A paint sets its colours before every op, mostly to the colour it already has. The GC keeps a copy
 * of the colour it is given, so the application may dispose its own; an equal colour keeps that copy
 * instead of making another.
 */
@Tag("native-unit")
class GCKeepsEqualColorNativeTest {

    private Display display;
    private GC gc;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        Shell shell = DartMocks.dartShell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 50, 50);
        gc = new GC(canvas);
    }

    @AfterEach
    void tearDown() {
        gc.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void anEqualColourKeepsTheCopyTheGcHolds() {
        gc.setBackground(new Color(display, 200, 10, 10));
        Color held = gc.getBackground();

        gc.setBackground(new Color(display, 200, 10, 10));
        gc.setForeground(new Color(display, 1, 2, 3));
        Color heldForeground = gc.getForeground();
        gc.setForeground(new Color(display, 1, 2, 3));

        assertThat(gc.getBackground()).isSameAs(held);
        assertThat(gc.getForeground()).isSameAs(heldForeground);
    }

    @Test
    void anotherColourOrAlphaIsCopied() {
        gc.setBackground(new Color(display, 200, 10, 10));
        Color held = gc.getBackground();

        gc.setBackground(new Color(display, 200, 10, 11));
        assertThat(gc.getBackground()).isNotSameAs(held);
        assertThat(gc.getBackground().getBlue()).isEqualTo(11);

        Color opaque = gc.getBackground();
        gc.setBackground(colorWithAlpha(200, 10, 11, 128));
        assertThat(gc.getBackground()).isNotSameAs(opaque);
        assertThat(GraphicsUtils.getAlpha(gc.getBackground())).isEqualTo(128);
    }

    /** SWT 3.103 has no Color(Device, int, int, int, int) nor Color.getAlpha; its DartColor keeps an alpha. */
    private Color colorWithAlpha(int red, int green, int blue, int alpha) {
        DartColor color = new DartColor(display, red, green, blue, null);
        color.alpha = alpha;
        return color.getApi();
    }

    @Test
    void aSystemColourIsNotTakenForTheSameRgb() {
        Color plain = new Color(display, 0, 0, 0);
        gc.setForeground(plain);
        Color held = gc.getForeground();
        Color system = GraphicsUtils.tagSystemColor(new Color(display, 0, 0, 0), SWT.COLOR_WIDGET_FOREGROUND);

        gc.setForeground(system);

        assertThat(gc.getForeground()).isNotSameAs(held);
        assertThat(((DartColor) gc.getForeground().getImpl()).systemColorId).isEqualTo(SWT.COLOR_WIDGET_FOREGROUND);
    }

    @Test
    void aHeldCopyTheApplicationDisposedIsReplaced() {
        gc.setBackground(new Color(display, 200, 10, 10));
        gc.getBackground().dispose();

        gc.setBackground(new Color(display, 200, 10, 10));

        assertThat(gc.getBackground().isDisposed()).isFalse();
    }
}
