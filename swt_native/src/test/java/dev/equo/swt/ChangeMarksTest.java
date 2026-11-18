package dev.equo.swt;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.VWidget;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChangeMarksTest {

    private final VWidget value = mock(VWidget.class);

    private DartWidget widget(boolean disposed) {
        DartWidget w = mock(DartWidget.class);
        when(w.isDisposed()).thenReturn(disposed);
        when(w.getValue()).thenReturn(value);
        return w;
    }

    @Test
    void a_value_that_changed_is_marked_and_one_that_did_not_is_not() {
        DartWidget w = widget(false);
        ChangeMarks.ifChanged(w, 1, 5, self -> 5);
        ChangeMarks.ifChanged(w, 2, true, self -> true);
        ChangeMarks.ifChanged(w, 3, new int[] {1, 2}, self -> new int[] {1, 2});
        verify(value, never()).markDirty(anyInt());

        ChangeMarks.ifChanged(w, 4, 5, self -> 6);
        ChangeMarks.ifChanged(w, 5, true, self -> false);
        ChangeMarks.ifChanged(w, 6, "a", self -> "b");
        verify(value).markDirty(4);
        verify(value).markDirty(5);
        verify(value).markDirty(6);
    }

    @Test
    void a_slot_given_what_it_holds_is_not_marked() {
        DartWidget w = widget(false);
        ChangeMarks.ifDiffers(w, 1, "a", "a");
        ChangeMarks.ifDiffers(w, 2, new String[] {"a"}, new String[] {"a"});
        verify(value, never()).markDirty(anyInt());
        ChangeMarks.ifDiffers(w, 3, "a", "b");
        verify(value).markDirty(3);
    }

    @Test
    void a_disposed_widget_is_neither_read_nor_marked() {
        DartWidget w = widget(true);
        ChangeMarks.ifChanged(w, 1, 5, self -> { throw new AssertionError("read a disposed widget"); });
        ChangeMarks.ifChanged(w, 2, true, self -> { throw new AssertionError("read a disposed widget"); });
        ChangeMarks.ifChanged(w, 3, "a", self -> { throw new AssertionError("read a disposed widget"); });
        assertThat(ChangeMarks.markChange(w, 4, "a", self -> { throw new AssertionError("read a disposed widget"); }, null)).isNull();
        assertThat(ChangeMarks.readInt(w, self -> 7)).isZero();
        assertThat(ChangeMarks.readBoolean(w, self -> true)).isFalse();
        assertThat(ChangeMarks.<DartWidget, String>read(w, self -> "a")).isNull();
        verify(value, never()).markDirty(anyInt());
    }

    @Test
    void a_snapshot_is_a_copy_the_setter_cannot_move() {
        Point point = new Point(1, 2);
        Point pointCopy = ChangeMarks.copy(point);
        point.x = 9;
        assertThat(pointCopy).isEqualTo(new Point(1, 2));

        Rectangle rect = new Rectangle(1, 2, 3, 4);
        Rectangle rectCopy = ChangeMarks.copy(rect);
        rect.width = 9;
        assertThat(rectCopy).isEqualTo(new Rectangle(1, 2, 3, 4));

        int[] ints = {1, 2};
        int[] intsCopy = ChangeMarks.copy(ints);
        ints[0] = 9;
        assertThat(intsCopy).containsExactly(1, 2);

        String[] strings = {"a"};
        String[] stringsCopy = ChangeMarks.copy(strings);
        strings[0] = "b";
        assertThat(stringsCopy).containsExactly("a");
        assertThat(ChangeMarks.copy((Point) null)).isNull();
    }
}
