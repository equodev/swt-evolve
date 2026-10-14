package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The alignment a Dart-backed {@link Label} answers. */
@Tag("native-unit")
class LabelAlignmentTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a label given several alignments is read LEFT first, then CENTER, as on Win32 and GTK")
    void severalAlignmentsAreReadLeftFirst() {
        Label label = new Label(shell, SWT.NONE);

        label.setAlignment(SWT.LEFT | SWT.CENTER | SWT.RIGHT);
        assertThat(label.getAlignment()).isEqualTo(SWT.LEFT);
        label.setAlignment(SWT.CENTER | SWT.RIGHT);
        assertThat(label.getAlignment()).isEqualTo(SWT.CENTER);
        label.setAlignment(-1);
        assertThat(label.getAlignment()).isEqualTo(SWT.LEFT);
    }

    @Test
    @DisplayName("a label given one alignment answers that one")
    void oneAlignmentIsReadAsGiven() {
        Label label = new Label(shell, SWT.NONE);

        assertThat(label.getAlignment()).isEqualTo(SWT.LEFT);
        label.setAlignment(SWT.RIGHT);
        assertThat(label.getAlignment()).isEqualTo(SWT.RIGHT);
        label.setAlignment(SWT.CENTER);
        assertThat(label.getAlignment()).isEqualTo(SWT.CENTER);
    }

    @Test
    @DisplayName("a separator has no alignment, as the javadoc says")
    void aSeparatorHasNoAlignment() {
        for (int style : new int[] {SWT.SEPARATOR | SWT.HORIZONTAL, SWT.SEPARATOR | SWT.VERTICAL | SWT.CENTER}) {
            Label separator = new Label(shell, style);
            separator.setAlignment(SWT.RIGHT);

            assertThat(separator.getAlignment()).isEqualTo(SWT.NONE);
        }
    }
}
