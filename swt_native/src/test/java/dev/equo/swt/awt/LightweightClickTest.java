package dev.equo.swt.awt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Input over lightweight content goes to the frame, whose own dispatcher routes it but — unlike the
 * frame's native peer — never turns a press and release into a {@code MOUSE_CLICKED}. Content that
 * acts on clicks (a click-to-zoom, a double-click to open) needs one after a click, and must not
 * get one at the end of a drag.
 */
@ExtendWith(Mocks.class)
class LightweightClickTest {

    @Test
    void aPressAndReleaseInPlaceIsAClick() throws Exception {
        assertEquals(List.of(MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_CLICKED),
                dispatchedFor(new int[] {60, 40}, new int[] {61, 40}));
    }

    @Test
    void aReleaseThatEndsADragIsNotAClick() throws Exception {
        assertEquals(List.of(MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_DRAGGED, MouseEvent.MOUSE_RELEASED),
                dispatchedFor(new int[] {60, 40}, new int[] {110, 70}));
    }

    /** Presses at {@code from}, moves to {@code to} if it differs, releases there. */
    private static List<Integer> dispatchedFor(int[] from, int[] to) throws Exception {
        Mocks.display();
        Canvas canvas = mock(Canvas.class);
        Frame frame = mock(Frame.class);
        when(frame.getLocationOnScreen()).thenReturn(new Point(0, 0));
        JPanel contentRoot = new JPanel(new BorderLayout());
        contentRoot.add(new JPanel());
        AwtInput.attach(canvas, frame, contentRoot, () -> { });
        Listener input = listenerFor(canvas, SWT.MouseDown);

        offDisplayThread(() -> {
            input.handleEvent(mouse(SWT.MouseDown, from));
            if (Math.abs(to[0] - from[0]) > 1) input.handleEvent(mouse(SWT.MouseMove, to));
            input.handleEvent(mouse(SWT.MouseUp, to));
        });
        EventQueue.invokeAndWait(() -> { });

        ArgumentCaptor<AWTEvent> events = ArgumentCaptor.forClass(AWTEvent.class);
        verify(frame, atLeastOnce()).dispatchEvent(events.capture());
        return events.getAllValues().stream().map(AWTEvent::getID).toList();
    }

    /**
     * Delivers input from a thread other than the mock display's, which is the test thread as long
     * as the display was created there first. On the display's own thread a press installs the
     * host's AWT dispatcher, which routes the whole event queue through that mock display for every
     * later test in the JVM.
     */
    private static void offDisplayThread(Runnable r) throws InterruptedException {
        Thread t = new Thread(r);
        t.start();
        t.join();
    }

    private static Listener listenerFor(Canvas canvas, int type) {
        ArgumentCaptor<Listener> captor = ArgumentCaptor.forClass(Listener.class);
        verify(canvas, atLeastOnce()).addListener(eq(type), captor.capture());
        return captor.getValue();
    }

    private static Event mouse(int type, int[] at) {
        Event e = new Event();
        e.type = type;
        e.button = type == SWT.MouseMove ? 0 : 1;
        e.x = at[0];
        e.y = at[1];
        return e;
    }
}
