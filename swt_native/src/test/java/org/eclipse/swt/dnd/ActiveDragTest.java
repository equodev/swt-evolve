package org.eclipse.swt.dnd;

import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Each display has its own drag under way: one JVM serves several sessions, and a drop or a drag
 * end in one session must not end, or be taken for, another session's drag.
 */
class ActiveDragTest {

    private final Display first = displayWithData();
    private final Display second = displayWithData();

    @Test
    void aDragIsOnlyItsOwnDisplays() {
        DragSource mine = sourceOn(first);
        DragSource theirs = sourceOn(second);

        ActiveDrag.begin(mine);
        ActiveDrag.begin(theirs);

        assertThat(ActiveDrag.of(first)).isSameAs(mine);
        assertThat(ActiveDrag.of(second)).isSameAs(theirs);
        assertThat(ActiveDrag.is(mine)).isTrue();
    }

    @Test
    void endingOneDisplaysDragLeavesTheOthersUnderWay() {
        DragSource mine = sourceOn(first);
        DragSource theirs = sourceOn(second);
        ActiveDrag.begin(mine);
        ActiveDrag.begin(theirs);

        ActiveDrag.end(mine);

        assertThat(ActiveDrag.of(first)).isNull();
        assertThat(ActiveDrag.of(second)).isSameAs(theirs);
    }

    @Test
    void aDragThatIsNotTheDisplaysDoesNotEndIt() {
        DragSource current = sourceOn(first);
        DragSource stale = sourceOn(first);
        ActiveDrag.begin(current);

        ActiveDrag.end(stale);

        assertThat(ActiveDrag.of(first)).isSameAs(current);
    }

    @Test
    void aDisposedSourceIsNoDragUnderWay() {
        DragSource source = sourceOn(first);
        ActiveDrag.begin(source);

        when(source.isDisposed()).thenReturn(true);

        assertThat(ActiveDrag.of(first)).isNull();
    }

    private static DragSource sourceOn(Display display) {
        DragSource source = mock(DragSource.class);
        when(source.getDisplay()).thenReturn(display);
        return source;
    }

    /** A display that keeps what is set on it, as a real one does. */
    private static Display displayWithData() {
        Display display = mock(Display.class);
        Map<String, Object> data = new HashMap<>();
        doAnswer(inv -> data.put(inv.getArgument(0), inv.getArgument(1))).when(display).setData(anyString(), any());
        when(display.getData(anyString())).thenAnswer(inv -> data.get(inv.<String>getArgument(0)));
        return display;
    }
}
