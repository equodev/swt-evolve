package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** What {@link DartComposite} and {@link DartCanvas} do when they are driven directly rather than through their API. */
@Tag("native-unit")
class CompositeImplementationNativeTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartComposite composite = new DartComposite(shell, SWT.NONE, null);
        Composite api = composite.getApi();
        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(composite);
        assertThat(composite.getApi()).isSameAs(api);

        DartCanvas canvas = new DartCanvas(shell, SWT.NONE, null);
        Canvas canvasApi = canvas.getApi();
        assertThat(canvasApi.getImpl()).isSameAs(canvas);
        assertThat(canvas.getApi()).isSameAs(canvasApi);
    }

    @Test
    @DisplayName("the composite's implementation reports its layout, tab list, children, mode and deferral")
    void compositeReportsWhatItHolds() {
        Composite composite = new Composite(shell, SWT.NONE);
        DartComposite impl = (DartComposite) composite.getImpl();
        assertThat(impl._layout()).isNull();
        assertThat(impl._tabList()).isNull();
        assertThat(impl._children()).isEmpty();
        assertThat(impl._backgroundMode()).isEqualTo(SWT.INHERIT_NONE);
        assertThat(impl._layoutCount()).isZero();
        assertThat(impl._layoutDeferred()).isFalse();

        FillLayout layout = new FillLayout();
        composite.setLayout(layout);
        Composite child = new Composite(composite, SWT.NONE);
        composite.setTabList(new Control[] {child});
        composite.setBackgroundMode(SWT.INHERIT_FORCE);
        composite.setLayoutDeferred(true);
        assertThat(impl._layout()).isSameAs(layout);
        assertThat(impl._tabList()).containsExactly(child);
        assertThat(impl._children()).containsExactly(child);
        assertThat(impl._backgroundMode()).isEqualTo(SWT.INHERIT_FORCE);
        assertThat(impl._layoutCount()).isEqualTo(1);
        assertThat(impl._layoutDeferred()).isTrue();
    }

    @Test
    @DisplayName("the canvas's implementation reports its caret and IME")
    void canvasReportsItsCaretAndIme() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        DartCanvas impl = (DartCanvas) canvas.getImpl();
        assertThat(impl._caret()).isNull();
        assertThat(impl._ime()).isNull();
        Caret caret = new Caret(canvas, SWT.NONE);
        IME ime = new IME(canvas, SWT.NONE);
        canvas.setIME(ime);
        assertThat(impl._caret()).isSameAs(caret);
        assertThat(impl._ime()).isSameAs(ime);
    }
}
