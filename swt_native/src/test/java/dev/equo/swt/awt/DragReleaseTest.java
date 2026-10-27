package dev.equo.swt.awt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import sun.swing.JLightweightFrame;
import sun.swing.LightweightContent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A drag belongs to the component it started on until the button comes up, wherever the pointer is
 * by then. AWT's lightweight dispatcher decides that from the release's modifiers, which must read
 * as "the button was down just before this event": a native peer reports a release with the
 * released button's legacy mask but without its down mask. A release that still carries the down
 * mask reads as "no button was held", and goes to whatever is under the pointer instead — so a
 * split pane divider dragged over another control never learns the drag ended.
 */
@ExtendWith(Mocks.class)
class DragReleaseTest {

    @Test
    void theReleaseThatEndsADragReadsAsTheEndOfAGrab() throws Exception {
        Mocks.display();
        Canvas canvas = mock(Canvas.class);
        Frame frame = mock(Frame.class);
        when(frame.getLocationOnScreen()).thenReturn(new Point(0, 0));
        JPanel contentRoot = new JPanel(new BorderLayout());
        contentRoot.add(new JPanel());
        AwtInput.attach(canvas, frame, contentRoot, () -> { });
        Listener input = listenerFor(canvas);

        offDisplayThread(() -> drag(input, 60, 40, 60, 140));
        EventQueue.invokeAndWait(() -> { });

        ArgumentCaptor<AWTEvent> events = ArgumentCaptor.forClass(AWTEvent.class);
        verify(frame, atLeastOnce()).dispatchEvent(events.capture());
        MouseEvent release = (MouseEvent) events.getAllValues().stream()
                .filter(e -> e.getID() == MouseEvent.MOUSE_RELEASED).findFirst().orElseThrow();
        int beforeRelease = release.getModifiersEx() ^ InputEvent.getMaskForButton(release.getButton());
        assertTrue((beforeRelease & InputEvent.BUTTON1_DOWN_MASK) != 0,
                "the release does not read as ending a drag: modifiersEx=" + release.getModifiersEx());
        assertEquals(0, release.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
        assertEquals(MouseEvent.BUTTON1, release.getButton());
    }

    @Test
    @SuppressWarnings("deprecation")
    void theReleaseKeepsTheLegacyMaskOfTheButtonItReleases() throws Exception {
        Mocks.display();
        Canvas canvas = mock(Canvas.class);
        Frame frame = mock(Frame.class);
        when(frame.getLocationOnScreen()).thenReturn(new Point(0, 0));
        JPanel contentRoot = new JPanel(new BorderLayout());
        AwtInput.attach(canvas, frame, contentRoot, () -> { });
        Listener input = listenerFor(canvas);

        offDisplayThread(() -> drag(input, 60, 40, 60, 140));
        EventQueue.invokeAndWait(() -> { });

        ArgumentCaptor<AWTEvent> events = ArgumentCaptor.forClass(AWTEvent.class);
        verify(frame, atLeastOnce()).dispatchEvent(events.capture());
        MouseEvent release = (MouseEvent) events.getAllValues().stream()
                .filter(e -> e.getID() == MouseEvent.MOUSE_RELEASED).findFirst().orElseThrow();
        assertTrue((release.getModifiers() & InputEvent.BUTTON1_MASK) != 0);
    }

    @Test
    @DisabledOnOs(value = OS.MAC, disabledReason = "this suite runs with -XstartOnFirstThread on macOS,"
            + " which hands the main thread to Cocoa; AWT never gets to run its event queue there")
    void aSplitPaneDividerDraggedOverAnotherControlMovesWhenReleased() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "a lightweight frame needs a display");
        Mocks.display();
        Canvas canvas = mock(Canvas.class);
        JPanel contentRoot = new JPanel(new BorderLayout());
        JPanel bottom = new JPanel();
        bottom.addMouseListener(new MouseAdapter() { });
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JPanel(), bottom);
        split.setContinuousLayout(false);
        JLightweightFrame[] frame = new JLightweightFrame[1];
        EventQueue.invokeAndWait(() -> {
            frame[0] = new JLightweightFrame();
            frame[0].setContent(new Content(contentRoot));
            frame[0].getContentPane().add(contentRoot, BorderLayout.CENTER);
            contentRoot.add(split);
            frame[0].setSize(200, 300);
            frame[0].setVisible(true);
            frame[0].getRootPane().setBounds(0, 0, 200, 300);
            frame[0].getRootPane().validate();
            split.setDividerLocation(100);
            frame[0].getRootPane().validate();
        });
        try {
            AwtInput.attach(canvas, frame[0], contentRoot, () -> { });
            Listener input = listenerFor(canvas);
            int[] before = new int[2];
            EventQueue.invokeAndWait(() -> {
                before[0] = split.getDividerLocation();
                before[1] = split.getDividerSize();
            });
            int grabY = before[0] + before[1] / 2;

            offDisplayThread(() -> drag(input, 100, grabY, 100, grabY + 100));
            EventQueue.invokeAndWait(() -> { });
            EventQueue.invokeAndWait(() -> { });

            int[] after = new int[2];
            EventQueue.invokeAndWait(() -> {
                after[0] = split.getDividerLocation();
                after[1] = ((BasicSplitPaneUI) split.getUI()).getLastDragLocation();
            });
            assertEquals(-1, after[1], "the divider is still dragging: its preview band stays painted");
            assertNotEquals(before[0], after[0], "the divider did not move");
        } finally {
            EventQueue.invokeAndWait(() -> frame[0].dispose());
        }
    }

    /** Presses at (x0,y0), moves to (x1,y1) in steps, and releases there. */
    private static void drag(Listener input, int x0, int y0, int x1, int y1) {
        input.handleEvent(mouse(SWT.MouseDown, x0, y0));
        for (int step = 1; step <= 10; step++) {
            input.handleEvent(mouse(SWT.MouseMove, x0 + (x1 - x0) * step / 10, y0 + (y1 - y0) * step / 10));
        }
        input.handleEvent(mouse(SWT.MouseUp, x1, y1));
    }

    /**
     * Delivers input from a thread other than the mock display's; on the display's own thread a
     * press installs the host's AWT dispatcher for every later test in the JVM.
     */
    private static void offDisplayThread(Runnable r) throws InterruptedException {
        Thread t = new Thread(r);
        t.start();
        t.join();
    }

    private static Listener listenerFor(Canvas canvas) {
        ArgumentCaptor<Listener> captor = ArgumentCaptor.forClass(Listener.class);
        verify(canvas, atLeastOnce()).addListener(eq(SWT.MouseDown), captor.capture());
        return captor.getValue();
    }

    private record Content(JPanel root) implements LightweightContent {
        @Override public JComponent getComponent() { return root; }
        @Override public void paintLock() {}
        @Override public void paintUnlock() {}
        @Override public void imageBufferReset(int[] data, int x, int y, int width, int height,
                                               int linestride, double scaleX, double scaleY) {}
        @Override public void imageReshaped(int x, int y, int width, int height) {}
        @Override public void imageUpdated(int dirtyX, int dirtyY, int dirtyWidth, int dirtyHeight) {}
        @Override public void focusGrabbed() {}
        @Override public void focusUngrabbed() {}
        @Override public void preferredSizeChanged(int width, int height) {}
        @Override public void maximumSizeChanged(int width, int height) {}
        @Override public void minimumSizeChanged(int width, int height) {}
    }

    private static Event mouse(int type, int x, int y) {
        Event e = new Event();
        e.type = type;
        e.button = type == SWT.MouseMove ? 0 : 1;
        e.stateMask = type == SWT.MouseUp ? SWT.BUTTON1 : 0;
        e.x = x;
        e.y = y;
        return e;
    }
}
