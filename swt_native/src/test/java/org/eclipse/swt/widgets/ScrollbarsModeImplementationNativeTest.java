package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The scroll bars mode a Scrollable's implementation keeps (setScrollbarsMode exists from SWT 3.126). */
@Tag("native-unit")
class ScrollbarsModeImplementationNativeTest {

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
    void theModeSetIsKeptButNotAnswered() {
        DartCanvas impl = new DartCanvas(shell, SWT.H_SCROLL | SWT.V_SCROLL, null);
        Canvas canvas = impl.getApi();
        canvas.setScrollbarsMode(SWT.SCROLLBAR_OVERLAY);
        assertThat(impl._scrollbarsMode()).isEqualTo(SWT.SCROLLBAR_OVERLAY);
        assertThat(canvas.getScrollbarsMode()).isEqualTo(SWT.NONE);
    }
}
