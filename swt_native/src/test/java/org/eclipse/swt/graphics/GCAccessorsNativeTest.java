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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The state {@link DartGC} holds for the wire, read through its generated accessors. */
@Tag("native-unit")
class GCAccessorsNativeTest {

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
        if (!gc.isDisposed())
            gc.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("the accessors report what the setters left")
    void accessorsReportTheState() {
        DartGC impl = (DartGC) gc.getImpl();
        Color red = new Color(display, 255, 0, 0);
        Pattern pattern = new Pattern(display, 0, 0, 1, 1, red, red);
        Transform transform = new Transform(display, 2, 0, 0, 2, 0, 0);
        gc.setForeground(red);
        gc.setBackground(red);
        gc.setXORMode(true);
        gc.setAdvanced(true);
        gc.setAlpha(9);
        gc.setAntialias(SWT.ON);
        gc.setTextAntialias(SWT.OFF);
        gc.setInterpolation(SWT.HIGH);
        gc.setFillRule(SWT.FILL_WINDING);
        gc.setLineCap(SWT.CAP_ROUND);
        gc.setLineJoin(SWT.JOIN_BEVEL);
        gc.setLineWidth(3);
        gc.setLineDash(new int[] {2, 1});
        gc.setLineAttributes(new LineAttributes(4));
        gc.setTransform(transform);
        gc.setClipping(1, 2, 3, 4);
        gc.setBackgroundPattern(pattern);
        gc.setForegroundPattern(pattern);

        assertThat(impl._drawable()).isNotNull();
        assertThat(impl._data()).isSameAs(gc.getGCData());
        assertThat(impl._count()).isZero();
        assertThat(impl._typeCount()).isZero();
        assertThat(impl._types()).isNull();
        assertThat(impl._points()).isNull();
        assertThat(impl._point()).isNull();
        assertThat(impl._XORMode()).isTrue();
        assertThat(impl._advanced()).isTrue();
        assertThat(impl._alpha()).isEqualTo(9);
        assertThat(impl._antialias()).isEqualTo(SWT.ON);
        assertThat(impl._textAntialias()).isEqualTo(SWT.OFF);
        assertThat(impl._interpolation()).isEqualTo(SWT.HIGH);
        assertThat(impl._fillRule()).isEqualTo(SWT.FILL_WINDING);
        assertThat(impl._background().getRGB()).isEqualTo(red.getRGB());
        assertThat(impl._foreground().getRGB()).isEqualTo(red.getRGB());
        assertThat(impl._backgroundPattern()).isSameAs(pattern);
        assertThat(impl._foregroundPattern()).isSameAs(pattern);
        assertThat(impl._font()).isSameAs(gc.getFont());
        assertThat(impl._lineAttributes().width).isEqualTo(4);
        assertThat(impl._lineWidth()).isEqualTo(4);
        assertThat(impl._lineCap()).isEqualTo(gc.getLineCap());
        assertThat(impl._lineJoin()).isEqualTo(gc.getLineJoin());
        assertThat(impl._lineStyle()).isEqualTo(gc.getLineStyle());
        assertThat(impl._lineDash()).isEqualTo(gc.getLineDash());
        assertThat(impl._style()).as("getStyle answers the GC data, not this field").isZero();
        assertThat(impl._transform()).isNotNull();
        assertThat(impl._clipping()).isEqualTo(new Rectangle(2, 4, 6, 8));
        assertThat(impl._clippingPath()).isNull();
        assertThat(impl._clippingRects()).isNull();
        transform.dispose();
        pattern.dispose();
    }
}
