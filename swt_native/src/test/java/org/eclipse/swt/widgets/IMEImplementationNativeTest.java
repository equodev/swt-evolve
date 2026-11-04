package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What {@link DartIME} reports when it is driven directly rather than through {@link IME}. */
@Tag("native-unit")
class IMEImplementationNativeTest {

    private RecordingBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void anImplementationBuiltWithoutItsApiCreatesOneOnceBoundToItself() {
        Canvas canvas = new Canvas(DartMocks.dartShell(), SWT.NONE);
        DartIME impl = new DartIME(canvas, SWT.NONE, null);
        IME api = impl.getApi();
        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
        assertThat(impl.getValue()).isSameAs(impl.getValue());
        // An IME makes no handle of its own, so nothing else hooks its events.
        impl._hookEvents();
    }

    @Test
    void theImplementationReportsWhatItHolds() {
        Canvas canvas = new Canvas(DartMocks.dartShell(), SWT.NONE);
        IME ime = new IME(canvas, SWT.NONE);
        DartIME impl = (DartIME) ime.getImpl();
        assertThat(impl._parent()).isSameAs(canvas);
        assertThat(impl._caretOffset()).isZero();
        assertThat(impl._startOffset()).isEqualTo(-1);
        assertThat(impl._commitCount()).isZero();
        assertThat(impl._text()).isEmpty();
        assertThat(impl._ranges()).isNull();
        assertThat(impl._styles()).isNull();
    }

    @Test
    void anImeTalksThroughTheBridgeOfItsCanvas() {
        Canvas canvas = new Canvas(DartMocks.dartShell(), SWT.NONE);
        DartIME impl = (DartIME) new IME(canvas, SWT.NONE).getImpl();
        assertThat(impl.getBridge()).isSameAs(((DartWidget) canvas.getImpl()).getBridge());
        assertThat(impl.getBridge()).isSameAs(impl.getBridge());
        RecordingBridge own = new RecordingBridge();
        impl.bridge = own;
        assertThat(impl.getBridge()).isSameAs(own);
    }

    @Test
    void aCompositionIsReportedFromItsOffset() {
        Canvas canvas = new Canvas(DartMocks.dartShell(), SWT.NONE);
        IME ime = new IME(canvas, SWT.NONE);
        DartIME impl = (DartIME) ime.getImpl();
        TextStyle underlined = new TextStyle();
        underlined.underline = true;
        impl.startOffset = 3;
        impl.caretOffset = 2;
        impl.ranges = new int[] {0, 1, 2, 4};
        impl.styles = new TextStyle[] {underlined, new TextStyle()};
        assertThat(ime.getCompositionOffset()).isEqualTo(3);
        assertThat(ime.getCaretOffset()).isEqualTo(5);
        assertThat(ime.getRanges()).containsExactly(3, 4, 5, 7);
        assertThat(ime.getStyles()).containsExactly(impl.styles).isNotSameAs(impl.styles);
        ime.setCompositionOffset(10);
        assertThat(ime.getCompositionOffset()).isEqualTo(10);
        assertThat(ime.getRanges()).containsExactly(10, 11, 12, 14);
        ime.dispose();
        assertThat(impl._styles()).isNull();
        assertThat(impl._ranges()).isNull();
    }

    @Test
    void theConstructorWithoutACanvasLeavesTheImeUnattached() {
        IME ime = new IME();
        DartIME impl = (DartIME) ime.getImpl();
        assertThat(impl.getApi()).isSameAs(ime);
        assertThat(impl._parent()).isNull();
        assertThat(impl._text()).isNull();
    }
}
