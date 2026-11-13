package org.eclipse.swt.dnd;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/** The label handlers of a DropTarget, which upstream SWT added after 3.120: this backend hooks none. */
@ExtendWith(Mocks.class)
class DropTargetLabelHandlersTest {

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
    void aDropTargetOverLabelsHooksNoLabelHandlers() {
        Composite control = new Composite(Mocks.shell(), SWT.NONE);
        new Label(control, SWT.NONE);
        DartDropTarget impl = (DartDropTarget) new DropTarget(control, DND.DROP_COPY).getImpl();
        assertThat(impl._labelDragHandlersAdded()).isFalse();
    }
}
