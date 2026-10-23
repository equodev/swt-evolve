package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An arrow key offers a Traverse whose doit says whether the control traverses on arrows: a radio
 * button moves between the radios of its group, other controls keep the arrows.
 */
class ArrowTraversalDoitTest extends KeyRoutingTestBase {

    private static final int[] ARROWS = { SWT.ARROW_LEFT, SWT.ARROW_RIGHT, SWT.ARROW_UP, SWT.ARROW_DOWN };

    private static java.util.List<String> traversalsOf(Control control) {
        java.util.List<String> events = new ArrayList<>();
        control.addListener(SWT.Traverse, e -> events.add("Traverse " + e.detail + " doit=" + e.doit));
        control.addListener(SWT.KeyDown, e -> events.add("KeyDown"));
        for (int arrow : ARROWS)
            press(control, arrow);
        return events;
    }

    @Test
    @DisplayName("a radio button offers an arrow traversal with doit=true, and a lone radio still gets the KeyDown")
    void radioTraversesOnArrows() {
        Button radio = new Button(shell(), SWT.RADIO);

        assertThat(traversalsOf(radio)).containsExactly(
                "Traverse " + SWT.TRAVERSE_ARROW_PREVIOUS + " doit=true", "KeyDown",
                "Traverse " + SWT.TRAVERSE_ARROW_NEXT + " doit=true", "KeyDown",
                "Traverse " + SWT.TRAVERSE_ARROW_PREVIOUS + " doit=true", "KeyDown",
                "Traverse " + SWT.TRAVERSE_ARROW_NEXT + " doit=true", "KeyDown");
    }

    @Test
    @DisplayName("a check box and a push button offer an arrow traversal with doit=false")
    void checkAndPushKeepTheArrows() {
        Shell shell = shell();
        for (int style : new int[] { SWT.CHECK, SWT.PUSH }) {
            assertThat(traversalsOf(new Button(shell, style)))
                    .filteredOn(e -> e.startsWith("Traverse"))
                    .hasSize(ARROWS.length)
                    .allMatch(e -> e.endsWith("doit=false"));
        }
    }

    @Test
    @DisplayName("controls other than a radio button offer an arrow traversal with doit=false")
    void otherControlsKeepTheArrows() {
        Shell shell = shell();
        Control[] controls = {
                new Button(shell, SWT.TOGGLE), new Button(shell, SWT.ARROW), new Label(shell, SWT.NONE),
                new Text(shell, SWT.SINGLE), new Text(shell, SWT.MULTI), new Combo(shell, SWT.DROP_DOWN),
                new List(shell, SWT.SINGLE), new Composite(shell, SWT.NONE), new Canvas(shell, SWT.NONE),
                new Scale(shell, SWT.HORIZONTAL), new Spinner(shell, SWT.NONE), new Link(shell, SWT.NONE) };
        for (Control control : controls) {
            assertThat(traversalsOf(control))
                    .as(control.toString())
                    .filteredOn(e -> e.startsWith("Traverse"))
                    .hasSize(ARROWS.length)
                    .allMatch(e -> e.endsWith("doit=false"));
        }
    }
}
