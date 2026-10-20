package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A wheel reported to Java reaches the control's MouseWheel listeners; Java moves the vertical bar
 * only of a control the client does not scroll itself.
 */
class MouseWheelScrollTest extends KeyRoutingTestBase {

    private static Event wheelDown() {
        Event e = new Event();
        e.count = -3;
        return e;
    }

    @Test
    @DisplayName("a ScrolledComposite's bar is left to the client, which scrolls its content")
    void scrolledCompositeIsScrolledByTheClient() {
        ScrolledComposite scrolled = new ScrolledComposite(shell(), SWT.V_SCROLL);
        ScrollBar bar = scrolled.getVerticalBar();
        bar.setValues(0, 0, 100, 10, 1, 10);
        java.util.List<String> events = new ArrayList<>();
        scrolled.addListener(SWT.MouseWheel, e -> events.add("MouseWheel"));
        bar.addListener(SWT.Selection, e -> events.add("Selection"));

        ControlHelper.handleMouseWheel((DartControl) scrolled.getImpl(), wheelDown());

        assertThat(events).containsExactly("MouseWheel");
        assertThat(bar.getSelection()).isZero();
    }

    @Test
    @DisplayName("a Canvas's bar is moved by the wheel, with a Selection")
    void canvasBarMoves() {
        Canvas canvas = new Canvas(shell(), SWT.V_SCROLL);
        ScrollBar bar = canvas.getVerticalBar();
        bar.setValues(0, 0, 100, 10, 1, 10);
        java.util.List<String> events = new ArrayList<>();
        canvas.addListener(SWT.MouseWheel, e -> events.add("MouseWheel"));
        bar.addListener(SWT.Selection, e -> events.add("Selection"));

        ControlHelper.handleMouseWheel((DartControl) canvas.getImpl(), wheelDown());

        assertThat(events).containsExactly("MouseWheel", "Selection");
        assertThat(bar.getSelection()).isEqualTo(3);
    }
}
