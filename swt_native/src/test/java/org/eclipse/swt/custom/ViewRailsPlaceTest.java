package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.custom.ViewRails.LEFT_BOTTOM;
import static org.eclipse.swt.custom.ViewRails.LEFT_TOP;
import static org.eclipse.swt.custom.ViewRails.RIGHT_BOTTOM;
import static org.eclipse.swt.custom.ViewRails.RIGHT_TOP;

/**
 * Where a view stack's icons go, by where the stack sits against the editor area. The layout is the
 * Java perspective's, with a stack added at the bottom right, as an RCP can have.
 */
class ViewRailsPlaceTest {

    /** The editor area: x 300-1100, y 50-650. */
    private static final Rectangle EDITOR = new Rectangle(300, 50, 800, 600);

    @Test
    void aStackLeftOfTheEditorGoesToTheLeftRailsTop() {
        assertThat(ViewRails.placeOf(new Rectangle(0, 50, 296, 900), EDITOR)).isEqualTo(LEFT_TOP);
    }

    @Test
    void aStackUnderTheEditorGoesToTheLeftRailsBottom() {
        assertThat(ViewRails.placeOf(new Rectangle(300, 654, 1100, 300), EDITOR)).isEqualTo(LEFT_BOTTOM);
    }

    @Test
    void aStackRightOfTheEditorGoesToTheRightRailsTop() {
        assertThat(ViewRails.placeOf(new Rectangle(1104, 50, 300, 300), EDITOR)).isEqualTo(RIGHT_TOP);
    }

    @Test
    void aStackRightOfTheEditorsLowerHalfGoesToTheRightRailsBottom() {
        assertThat(ViewRails.placeOf(new Rectangle(1104, 400, 300, 250), EDITOR)).isEqualTo(RIGHT_BOTTOM);
    }

    @Test
    void aStackUnderTheEditorsRightHalfGoesToTheRightRailsBottom() {
        assertThat(ViewRails.placeOf(new Rectangle(800, 654, 600, 300), EDITOR)).isEqualTo(RIGHT_BOTTOM);
    }

    @Test
    void aPlaceOnAMissingRailMovesToTheOtherRailsSameEnd() {
        assertThat(ViewRails.hostedPlace(RIGHT_BOTTOM, true, false)).isEqualTo(LEFT_BOTTOM);
        assertThat(ViewRails.hostedPlace(LEFT_TOP, false, true)).isEqualTo(RIGHT_TOP);
        assertThat(ViewRails.hostedPlace(RIGHT_TOP, true, true)).isEqualTo(RIGHT_TOP);
    }

    @Test
    void aPlaceNamesItsRailAndEndWhetherOrNotItsStackIsMinimized() {
        for (int place = LEFT_TOP; place <= RIGHT_BOTTOM; place++) {
            for (int flags : new int[] {0, ViewRails.MINIMIZED}) {
                int p = place | flags;
                assertThat(ViewRails.place(ViewRails.isRight(p), ViewRails.isBottom(p))).isEqualTo(place);
            }
        }
        assertThat(ViewRails.isRight(RIGHT_BOTTOM | ViewRails.MINIMIZED)).isTrue();
        assertThat(ViewRails.isBottom(LEFT_TOP | ViewRails.MINIMIZED)).isFalse();
    }
}
