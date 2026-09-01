package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FigureCanvasPaintTest extends SerializeTestBase {

    // firePaint used to single out a FigureCanvas by class name and send it no Paint, on the grounds
    // that draw2d paints it from its own LightweightSystem. That update loop can finish before the
    // Flutter client connects, and it renders through an off-screen blit the client is not there to
    // produce, so the canvas kept an unpainted buffer with nothing left to repaint it. draw2d answers
    // a Paint by painting its whole figure tree into the event's GC, so it takes one like any other
    // control. Needs the real draw2d type on the classpath: the skip was an isInstance() check, and
    // without draw2d it never applied.
    @Test
    void a_figure_canvas_is_sent_the_synthetic_paint_like_any_other_control() {
        // Registers the default Display that new GC(control) resolves its device from. Called before
        // any other mock is built: it stubs internally, and interleaving that with the stubbing below
        // makes Mockito report an UnfinishedStubbingException against an unrelated line.
        Mocks.swtDisplay();

        Rectangle bounds = new Rectangle(0, 0, 100, 100);
        FigureCanvas api = mock(FigureCanvas.class);
        DartCanvas impl = mock(DartCanvas.class);
        when(impl.getApi()).thenReturn(api);
        when(impl.getBounds()).thenReturn(bounds);

        ControlHelper.paint(impl, null);

        verify(impl).sendEvent(eq(SWT.Paint), any(Event.class));
    }
}
