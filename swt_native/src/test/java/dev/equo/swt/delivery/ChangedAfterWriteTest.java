package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.WireFrames;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartLabel;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A property changed after the record that did not carry it was written.
 *
 * <p>Crediting a record must forget only what it carried, or a change made after it is dropped with
 * a value nothing ever sent - and nothing marks it again, so the far side keeps the state from before
 * it until some unrelated write to the same widget happens to repair it.
 */
@ExtendWith(Mocks.class)
class ChangedAfterWriteTest {

    private static final int CLIENT = 1;

    private RecordingBridge bridge;

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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a change made after the record was written is still outstanding")
    void changeAfterTheWriteSurvives() {
        Shell shell = Mocks.shell();
        Label label = new Label(shell, SWT.NONE);
        DartLabel impl = (DartLabel) label.getImpl();
        // Delivered once, so the next record is a change rather than the whole widget.
        WireFrames.record(impl, CLIENT);

        label.setText("written");
        WireFrames.record(impl, CLIENT);
        label.setToolTipText("changed after the walk");

        assertThat(impl.getValue().changedNames())
                .as("the record carried text, so only text is settled")
                .containsExactly("toolTipText");
        assertThat(impl.getValue().anyDirty())
                .as("and the widget still has something to say, so it will be asked to say it")
                .isTrue();
    }

    @Test
    @DisplayName("what the record did carry is forgotten")
    void whatItCarriedIsForgotten() {
        Shell shell = Mocks.shell();
        Label label = new Label(shell, SWT.NONE);
        DartLabel impl = (DartLabel) label.getImpl();
        WireFrames.record(impl, CLIENT);

        label.setText("written");
        WireFrames.record(impl, CLIENT);

        assertThat(impl.getValue().anyDirty())
                .as("nothing changed after the write, so nothing is outstanding")
                .isFalse();
    }
}
