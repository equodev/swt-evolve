package dev.equo.swt.awt;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.awt.Frame;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Swing content embedded here is painted into a canvas by a lightweight frame, which has no peer to
 * tell it where on screen it ended up — AWT leaves it at the origin. Every
 * {@code getLocationOnScreen()} inside the embedding is then short by wherever the canvas really
 * is, and embedded content that maps its own geometry back into SWT
 * ({@code Display.map(null, control, p)}) gets a position that far off — far enough to come out
 * negative, and to be clamped against an edge by whatever consumes it.
 *
 * <p>So the frame has to be told the canvas's screen position, and told again whenever that
 * changes: the canvas moving or resizing, and the window moving, which the canvas does not report.
 *
 * <p>The frame is a mock rather than a real one: constructing an AWT frame needs a display, and CI
 * runs headless. What is under test is the coordinate this hands it, not AWT's own placement.
 */
@ExtendWith(Mocks.class)
class SwingHostFrameLocationTest {

    /** Away from the origin on both axes, so an unsynced frame cannot pass by coincidence. */
    private static final int SCREEN_X = 580;
    private static final int SCREEN_Y = 931;

    private static Canvas canvasAt(int x, int y) {
        Canvas canvas = mock(Canvas.class);
        when(canvas.isDisposed()).thenReturn(false);
        when(canvas.toDisplay(anyInt(), anyInt()))
                .thenAnswer(call -> new org.eclipse.swt.graphics.Point(
                        x + (int) call.getArgument(0), y + (int) call.getArgument(1)));
        return canvas;
    }

    /** Runs the pending EDT work the sync posts, so the assertion sees the result. */
    private static void drainEdt() throws Exception {
        java.awt.EventQueue.invokeAndWait(() -> { });
    }

    @Test
    void theFrameIsPutWhereTheCanvasIs() throws Exception {
        Frame frame = mock(Frame.class);

        EvolveSwingHost.syncFrameLocation(canvasAt(SCREEN_X, SCREEN_Y), frame);
        drainEdt();

        verify(frame).setLocation(SCREEN_X, SCREEN_Y);
    }

    @Test
    void movingTheCanvasMovesTheFrame() throws Exception {
        Frame frame = mock(Frame.class);

        EvolveSwingHost.syncFrameLocation(canvasAt(SCREEN_X, SCREEN_Y), frame);
        drainEdt();
        EvolveSwingHost.syncFrameLocation(canvasAt(SCREEN_X + 120, SCREEN_Y - 40), frame);
        drainEdt();

        verify(frame).setLocation(SCREEN_X + 120, SCREEN_Y - 40);
    }

    @Test
    void aDisposedCanvasIsLeftAlone() throws Exception {
        // Dispose races the listeners this is called from; asking a disposed canvas for its
        // position would throw on the SWT thread.
        Frame frame = mock(Frame.class);
        Canvas canvas = mock(Canvas.class);
        when(canvas.isDisposed()).thenReturn(true);

        EvolveSwingHost.syncFrameLocation(canvas, frame);
        drainEdt();

        verify(frame, never()).setLocation(anyInt(), anyInt());
    }
}
