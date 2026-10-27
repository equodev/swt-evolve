package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
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
 * A property changed after the frame that did not carry it was written.
 *
 * <p>Writing a frame and crediting it are two moments, and the widget goes on living between them:
 * a layout that adds a child while its parent is being serialized changes the parent after the walk
 * has already read it. Crediting the frame must forget only what the frame carried, or that change
 * is dropped with a value nothing ever sent - and nothing marks it again, so the far side keeps the
 * state from before it until some unrelated write to the same widget happens to repair it.
 */
@ExtendWith(Mocks.class)
class ChangedAfterWriteTest {

    private static final int CLIENT = 1;

    private RecordingBridge bridge;
    private final Serializer serializer = new Serializer();

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
        Serializer.enterConnection(CLIENT);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a change made after the frame was written survives it being credited")
    void changeAfterTheWriteSurvives() throws java.io.IOException {
        Shell shell = Mocks.shell();
        Label label = new Label(shell, SWT.NONE);
        DartLabel impl = (DartLabel) label.getImpl();
        // Delivered once, so the next frame is an update rather than the whole widget.
        serializer.to(label);
        Serializer.markDelivered();

        label.setText("written");
        serializer.toDiff(impl);

        // The widget goes on living between the write and the credit.
        label.setToolTipText("changed after the walk");
        Serializer.markDelivered();

        assertThat(impl.getValue().changedKeys())
                .as("the frame carried text, so only text is settled")
                .containsExactly("toolTipText");
        assertThat(impl.getValue().anyDirty())
                .as("and the widget still has something to say, so it will be asked to say it")
                .isTrue();
    }

    @Test
    @DisplayName("what the frame did carry is forgotten")
    void whatItCarriedIsForgotten() throws java.io.IOException {
        Shell shell = Mocks.shell();
        Label label = new Label(shell, SWT.NONE);
        DartLabel impl = (DartLabel) label.getImpl();
        serializer.to(label);
        Serializer.markDelivered();

        label.setText("written");
        serializer.toDiff(impl);
        Serializer.markDelivered();

        assertThat(impl.getValue().anyDirty())
                .as("nothing changed after the write, so nothing is outstanding")
                .isFalse();
    }
}
