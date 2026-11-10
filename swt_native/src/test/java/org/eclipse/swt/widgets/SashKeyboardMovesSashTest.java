package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * An arrow key on a focused Sash moves it across its axis, as every native Sash does: by 9 pixels,
 * or 1 with Ctrl, reported after the KeyDown as a Selection the listeners can veto, kept inside the
 * parent, and with the pointer put back on the moved Sash.
 */
class SashKeyboardMovesSashTest extends KeyRoutingTestBase {

    private Sash sash(int style, int x, int y, int width, int height) {
        Composite parent = new Composite(shell(), SWT.NONE);
        parent.setBounds(0, 0, 300, 200);
        Sash sash = new Sash(parent, style);
        sash.setBounds(x, y, width, height);
        clearInvocations(sash.getDisplay());
        return sash;
    }

    @Test
    @DisplayName("Down and Up move a horizontal sash by 9 pixels, each a Selection after its KeyDown")
    void arrowsAcrossAHorizontalSashMoveIt() {
        Sash sash = sash(SWT.HORIZONTAL, 0, 50, 300, 10);
        java.util.List<String> events = new ArrayList<>();
        sash.addListener(SWT.KeyDown, e -> events.add("KeyDown"));
        sash.addListener(SWT.Selection, e -> events.add("Selection " + e.y + " detail " + e.detail));

        press(sash, SWT.ARROW_DOWN);
        press(sash, SWT.ARROW_DOWN);
        press(sash, SWT.ARROW_UP);

        assertThat(events).containsExactly("KeyDown", "Selection 59 detail 0", "KeyDown", "Selection 68 detail 0",
                "KeyDown", "Selection 59 detail 0");
        assertThat(sash.getBounds().y).isEqualTo(59);
    }

    @Test
    @DisplayName("with Ctrl, an arrow moves the sash by a single pixel")
    void ctrlArrowsMoveOnePixel() {
        Sash sash = sash(SWT.VERTICAL, 100, 0, 10, 200);
        java.util.List<Integer> masks = new ArrayList<>();
        sash.addListener(SWT.Selection, e -> masks.add(e.stateMask));

        press(sash, SWT.ARROW_RIGHT, SWT.CTRL);
        press(sash, SWT.ARROW_RIGHT, SWT.CTRL);
        press(sash, SWT.ARROW_LEFT, SWT.CTRL);

        assertThat(sash.getBounds().x).isEqualTo(101);
        assertThat(masks).as("each Selection carries the Ctrl the key was pressed with").containsOnly(SWT.CTRL);
    }

    @Test
    @DisplayName("the arrows along the sash move nothing and report nothing")
    void arrowsAlongTheSashDoNothing() {
        Sash sash = sash(SWT.HORIZONTAL, 0, 50, 300, 10);
        java.util.List<Event> selections = new ArrayList<>();
        sash.addListener(SWT.Selection, selections::add);

        press(sash, SWT.ARROW_LEFT);
        press(sash, SWT.ARROW_RIGHT);

        assertThat(selections).isEmpty();
        assertThat(sash.getBounds().y).isEqualTo(50);
    }

    @Test
    @DisplayName("the sash stays inside its parent, and a move that changes nothing is not reported")
    void theMoveIsKeptInsideTheParent() {
        Sash sash = sash(SWT.HORIZONTAL, 0, 185, 300, 10);
        java.util.List<Integer> reported = new ArrayList<>();
        sash.addListener(SWT.Selection, e -> reported.add(e.y));

        press(sash, SWT.ARROW_DOWN);
        press(sash, SWT.ARROW_DOWN);

        assertThat(reported).containsExactly(190);
        assertThat(sash.getBounds().y).isEqualTo(190);
    }

    @Test
    @DisplayName("a Selection listener setting doit=false keeps the sash and the pointer where they are")
    void aVetoedSelectionKeepsTheSash() {
        Sash sash = sash(SWT.HORIZONTAL, 0, 50, 300, 10);
        sash.addListener(SWT.Selection, e -> e.doit = false);

        press(sash, SWT.ARROW_DOWN);

        assertThat(sash.getBounds().y).isEqualTo(50);
        verify(sash.getDisplay(), never()).setCursorLocation(any(Point.class));
    }

    @Test
    @DisplayName("the pointer is put on the moved sash, at the middle of its leading edge")
    void thePointerFollowsTheSash() {
        Sash sash = sash(SWT.HORIZONTAL, 0, 50, 300, 10);
        Point expected = sash.getParent().toDisplay(150, 59);

        press(sash, SWT.ARROW_DOWN);

        verify(sash.getDisplay()).setCursorLocation(expected);
    }

    @Test
    @DisplayName("an arrow while a mouse button is held moves nothing")
    void anArrowDuringAPressMovesNothing() {
        Sash sash = sash(SWT.HORIZONTAL, 0, 50, 300, 10);
        java.util.List<Event> selections = new ArrayList<>();
        sash.addListener(SWT.Selection, selections::add);

        press(sash, SWT.ARROW_DOWN, SWT.BUTTON1);

        assertThat(selections).isEmpty();
        assertThat(sash.getBounds().y).isEqualTo(50);
    }

    @Test
    @DisplayName("a sash its Selection listener disposes is neither moved nor followed by the pointer")
    void aSashDisposedBySelectionGoesNoFurther() {
        Sash sash = sash(SWT.HORIZONTAL, 0, 50, 300, 10);
        Display display = sash.getDisplay();
        sash.addListener(SWT.Selection, e -> sash.dispose());

        press(sash, SWT.ARROW_DOWN);

        assertThat(sash.isDisposed()).isTrue();
        verify(display, never()).setCursorLocation(any(Point.class));
    }

    @Test
    @DisplayName("a sash its Move listener disposes does not take the pointer")
    void aSashDisposedByItsMoveTakesNoPointer() {
        Sash sash = sash(SWT.HORIZONTAL, 0, 50, 300, 10);
        Display display = sash.getDisplay();
        sash.addListener(SWT.Move, e -> sash.dispose());

        press(sash, SWT.ARROW_DOWN);

        assertThat(sash.isDisposed()).isTrue();
        verify(display, never()).setCursorLocation(any(Point.class));
    }

    @Test
    @DisplayName("a KeyDown listener that vetoes the key keeps the sash")
    void aVetoedKeyDoesNotMove() {
        Sash sash = sash(SWT.HORIZONTAL, 0, 50, 300, 10);
        sash.addListener(SWT.KeyDown, e -> e.doit = false);

        press(sash, SWT.ARROW_DOWN);

        assertThat(sash.getBounds().y).isEqualTo(50);
    }
}
