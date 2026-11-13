package org.eclipse.swt.dnd;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the DragSource and DropTarget API never reaches in their implementations: one made without an API, and the
 * accessors the generator adds.
 */
@ExtendWith(Mocks.class)
class DragAndDropImplementationsTest {

    private Composite control;

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
        control = new Composite(Mocks.shell(), SWT.NONE);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void aDragSourceMadeWithoutAnApiMakesOne() {
        DartDragSource impl = new DartDragSource(control, DND.DROP_COPY, null);
        DragSource api = impl.getApi();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(control.getData(DND.DRAG_SOURCE_KEY)).isSameAs(api);
        assertThat(impl._control()).isSameAs(control);
        assertThat(impl._controlListener()).isNotNull();
        assertThat(impl._transferAgents()).isEmpty();
        assertThat(impl._dragEffect()).isNull();
        assertThat(impl._dragImageFromListener()).isNull();
        assertThat(impl._paths()).isNull();
        assertThat(impl._exist()).isNull();
        assertThat(impl._lastDragData()).isNull();
    }

    @Test
    void aDropTargetMadeWithoutAnApiMakesOne() {
        DartDropTarget impl = new DartDropTarget(control, DND.DROP_COPY, null);
        DropTarget api = impl.getApi();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(control.getData(DND.DROP_TARGET_KEY)).isSameAs(api);
        assertThat(impl._control()).isSameAs(control);
        assertThat(impl._controlListener()).isNotNull();
        assertThat(impl._transferAgents()).isEmpty();
        assertThat(impl._dropEffect()).isNull();
        assertThat(impl._feedback()).isEqualTo(DND.FEEDBACK_NONE);
        assertThat(impl._selectedDataType()).isNull();
        assertThat(impl._selectedOperation()).isZero();
        assertThat(impl._keyOperation()).isEqualTo(-1);
    }
}
