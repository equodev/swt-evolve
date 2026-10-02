package dev.equo.swt.awt;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.WindowBridge;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.DartShell;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sun.swing.JLightweightFrame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * A Swing window opened from embedded content — typically a dialog owned by
 * {@code JOptionPane.getFrameForComponent(panel)}, which is the embedding frame — takes that frame
 * as its native owner. Natively the frame is a child of the shell, so the OS keeps the dialog above
 * the application window. Here the frame is off-screen and its peer is a hidden window: unless it is
 * told the real window to stand for, the dialog is owned by nothing visible and falls behind the
 * application window the moment that window is activated.
 *
 * <p>The frame is a mock: constructing one needs a display, and CI runs headless. What is under
 * test is the window handle this hands it.
 */
@ExtendWith(Mocks.class)
class SwingHostFrameOwnerTest {

    private static final long APP_WINDOW = 0x4708B6L;

    private static Canvas canvasInWindow(long window) {
        FlutterBridge bridge = mock(FlutterBridge.class, withSettings().extraInterfaces(WindowBridge.class));
        Shell shell = mock(Shell.class);
        DartShell impl = mock(DartShell.class);
        when(shell.getImpl()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(bridge);
        when(((WindowBridge) bridge).nativeWindowHandle(shell)).thenReturn(window);
        Canvas canvas = mock(Canvas.class);
        when(canvas.isDisposed()).thenReturn(false);
        when(canvas.getShell()).thenReturn(shell);
        return canvas;
    }

    @Test
    void theFrameStandsForTheWindowItsCanvasIsDrawnInto() {
        JLightweightFrame frame = mock(JLightweightFrame.class);

        long owner = EvolveSwingHost.syncFrameOwner(canvasInWindow(APP_WINDOW), frame, 0);

        verify(frame).overrideNativeWindowHandle(APP_WINDOW, null);
        assertThat(owner).isEqualTo(APP_WINDOW);
    }

    @Test
    void theSameWindowIsNotHandedOverAgain() {
        // Called on every move and resize; each hand-over is a synchronous round trip to the AWT
        // toolkit thread.
        JLightweightFrame frame = mock(JLightweightFrame.class);
        Canvas canvas = canvasInWindow(APP_WINDOW);

        long owner = EvolveSwingHost.syncFrameOwner(canvas, frame, 0);
        EvolveSwingHost.syncFrameOwner(canvas, frame, owner);

        verify(frame, times(1)).overrideNativeWindowHandle(anyLong(), any());
    }

    @Test
    void aCanvasMovedToAnotherWindowHandsThatOneOver() {
        JLightweightFrame frame = mock(JLightweightFrame.class);

        long owner = EvolveSwingHost.syncFrameOwner(canvasInWindow(APP_WINDOW), frame, 0);
        owner = EvolveSwingHost.syncFrameOwner(canvasInWindow(APP_WINDOW + 8), frame, owner);

        verify(frame).overrideNativeWindowHandle(APP_WINDOW + 8, null);
        assertThat(owner).isEqualTo(APP_WINDOW + 8);
    }

    @Test
    void withNoNativeWindowTheFrameIsLeftAlone() {
        // A browser-hosted surface has no window of its own to offer; overriding with 0 would drop
        // the peer's own handle.
        JLightweightFrame frame = mock(JLightweightFrame.class);

        long owner = EvolveSwingHost.syncFrameOwner(canvasInWindow(0), frame, 0);

        verify(frame, never()).overrideNativeWindowHandle(anyLong(), any());
        assertThat(owner).isZero();
    }

    @Test
    void aDisposedCanvasIsLeftAlone() {
        JLightweightFrame frame = mock(JLightweightFrame.class);
        Canvas canvas = mock(Canvas.class);
        when(canvas.isDisposed()).thenReturn(true);

        long owner = EvolveSwingHost.syncFrameOwner(canvas, frame, APP_WINDOW);

        verify(frame, never()).overrideNativeWindowHandle(anyLong(), any());
        assertThat(owner).isEqualTo(APP_WINDOW);
    }
}
