package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What the Tracker implementation reports about what it holds. */
@Tag("native-unit")
class TrackerImplementationNativeTest extends SerializeTestBase {

    @Test
    void anImplementationBuiltWithoutItsApiCreatesOneOnceBoundToItself() {
        DartTracker impl = new DartTracker(Mocks.display(), SWT.NONE, null);
        Tracker api = impl.getApi();
        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
        assertThat(impl.getValue()).isSameAs(impl.getValue());
    }

    @Test
    void theImplementationReportsWhatItHolds() {
        Shell shell = Mocks.shell();
        Tracker tracker = new Tracker(shell, SWT.RESIZE);
        DartTracker impl = (DartTracker) tracker.getImpl();
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20), new Rectangle(30, 10, 20, 40)});
        tracker.setStippled(true);
        Cursor cursor = Mocks.display().getSystemCursor(SWT.CURSOR_HAND);
        tracker.setCursor(cursor);

        assertThat(impl._parent()).isSameAs(shell);
        assertThat(impl._tracking()).isFalse();
        assertThat(impl._cancelled()).isFalse();
        assertThat(impl._stippled()).isTrue();
        assertThat(impl._clientCursor()).isSameAs(cursor);
        assertThat(impl._resizeCursor()).isNull();
        assertThat(impl._rectangles()).containsExactly(new Rectangle(10, 10, 20, 20), new Rectangle(30, 10, 20, 40));
        assertThat(impl._bounds()).isEqualTo(new Rectangle(10, 10, 40, 40));
        assertThat(impl._proportions()).containsExactly(new Rectangle(0, 0, 50, 50), new Rectangle(50, 0, 50, 100));
        assertThat(impl._cursorOrientation()).isEqualTo(SWT.NONE);
        assertThat(impl._inEvent()).isFalse();
        assertThat(impl._oldX()).isZero();
        assertThat(impl._oldY()).isZero();
    }
}
