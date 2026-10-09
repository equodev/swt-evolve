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

/** What {@link DartButton} does when it is driven directly rather than through {@link Button}. */
@Tag("native-unit")
class ButtonImplementationNativeTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartButton impl = new DartButton(DartMocks.dartShell(), SWT.PUSH, null);

        Button api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
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
