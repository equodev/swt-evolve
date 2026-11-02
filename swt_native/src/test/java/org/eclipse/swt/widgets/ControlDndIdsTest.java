package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/** The drag source and drop target a control names to the client: only live ones, of a live control. */
@ExtendWith(Mocks.class)
class ControlDndIdsTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a control names its drag source and its drop target")
    void namesLiveOnes() {
        Label label = new Label(Mocks.shell(), SWT.NONE);
        DragSource source = new DragSource(label, DND.DROP_MOVE);
        DropTarget target = new DropTarget(label, DND.DROP_MOVE);
        DartControl impl = (DartControl) label.getImpl();
        assertThat(impl.getDragSource()).isTrue();
        assertThat(impl.getDragSourceId()).isEqualTo(FlutterBridge.id(source.getImpl()));
        assertThat(impl.getDropTargetId()).isEqualTo(FlutterBridge.id(target.getImpl()));
    }

    @Test
    @DisplayName("a disposed drag source or drop target is named no more")
    void disposedOnesAreNotNamed() {
        Label label = new Label(Mocks.shell(), SWT.NONE);
        DragSource source = new DragSource(label, DND.DROP_MOVE);
        DropTarget target = new DropTarget(label, DND.DROP_MOVE);
        DartControl impl = (DartControl) label.getImpl();
        source.dispose();
        target.dispose();
        assertThat(impl.getDragSource()).isFalse();
        assertThat(impl.getDragSourceId()).isNull();
        assertThat(impl.getDropTargetId()).isNull();
    }

    @Test
    @DisplayName("a disposed control names neither")
    void disposedControlNamesNeither() {
        Label label = new Label(Mocks.shell(), SWT.NONE);
        new DragSource(label, DND.DROP_MOVE);
        new DropTarget(label, DND.DROP_MOVE);
        DartControl impl = (DartControl) label.getImpl();
        label.dispose();
        assertThat(impl.getDragSource()).isFalse();
        assertThat(impl.getDragSourceId()).isNull();
        assertThat(impl.getDropTargetId()).isNull();
    }

    @Test
    @DisplayName("data under the drag and drop keys that is no drag source or drop target names neither")
    void otherDataNamesNeither() {
        Label label = new Label(Mocks.shell(), SWT.NONE);
        label.setData(DND.DRAG_SOURCE_KEY, "not a drag source");
        label.setData(DND.DROP_TARGET_KEY, "not a drop target");
        DartControl impl = (DartControl) label.getImpl();
        assertThat(impl.getDragSource()).isFalse();
        assertThat(impl.getDragSourceId()).isNull();
        assertThat(impl.getDropTargetId()).isNull();
    }
}
