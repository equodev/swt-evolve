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

/** Drag detection on a Dart-backed {@link Button}. */
@Tag("native-unit")
class ButtonDragDetectTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a primary click's drag detection does not fail, as on Win32 and GTK")
    void dragDetectOnAPrimaryClickDoesNotFail() {
        Event click = new Event();
        click.button = 1;
        click.count = 1;

        assertThat(new Button(DartMocks.dartShell(), SWT.PUSH).dragDetect(click)).isFalse();
    }

    @Test
    @DisplayName("dragDetect stores its answer in the consume flag")
    void dragDetectStoresItsAnswerInConsume() {
        Button button = new Button(DartMocks.dartShell(), SWT.PUSH);
        boolean[] consume = {true};

        boolean dragging = ((DartButton) button.getImpl()).dragDetect(1, 1, false, consume);

        assertThat(consume[0]).isEqualTo(dragging);
    }
}
