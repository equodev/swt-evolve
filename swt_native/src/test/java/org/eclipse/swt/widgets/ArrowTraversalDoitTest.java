package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An arrow key offers a Traverse whose doit says whether the control traverses on arrows: a radio
 * button moves between the radios of its group, other controls keep the arrows.
 */
class ArrowTraversalDoitTest extends KeyRoutingTestBase {

    private static final int[] ARROWS = { SWT.ARROW_LEFT, SWT.ARROW_RIGHT, SWT.ARROW_UP, SWT.ARROW_DOWN };

    private static List<String> traversalsOf(Control control) {
        List<String> events = new ArrayList<>();
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
}
