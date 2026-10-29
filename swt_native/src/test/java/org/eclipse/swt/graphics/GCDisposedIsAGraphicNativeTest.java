package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A disposed GC is a disposed graphic: the javadoc of these four calls names ERROR_GRAPHIC_DISPOSED,
 * as every other GC call's does, and Win32 raises it. Cocoa raises ERROR_WIDGET_DISPOSED from all four
 * and GTK from getForeground; this backend answers the javadoc's error on every OS.
 */
@Tag("native-unit")
class GCDisposedIsAGraphicNativeTest {

    private RecordingBridge bridge;
    private GC gc;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        Display display = DartMocks.dartDisplay();
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Shell shell = DartMocks.dartShell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 200, 100);
        gc = new GC(canvas);
        gc.dispose();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a disposed GC refuses its patterns, its data and its foreground as ERROR_GRAPHIC_DISPOSED")
    void refusedAsAGraphic() {
        assertGraphicDisposed("getBackgroundPattern", gc::getBackgroundPattern);
        assertGraphicDisposed("getForegroundPattern", gc::getForegroundPattern);
        assertGraphicDisposed("getGCData", gc::getGCData);
        assertGraphicDisposed("getForeground", gc::getForeground);
    }

    private static void assertGraphicDisposed(String call, ThrowingCallable callable) {
        assertThatThrownBy(callable).as(call)
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).as(call).isEqualTo(SWT.ERROR_GRAPHIC_DISPOSED));
    }
}
