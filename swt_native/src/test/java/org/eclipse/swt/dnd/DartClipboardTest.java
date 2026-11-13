package org.eclipse.swt.dnd;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.DartDisplay;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the Clipboard API never reaches in its implementation: the overloads the API answers itself, an
 * implementation made without an API, and the accessors the generator adds.
 */
@ExtendWith(Mocks.class)
class DartClipboardTest {

    private Display display;

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
        display = Mocks.display();
        ((DartDisplay) display.getImpl()).clipboards = null;
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void theImplementationAnswersTheShortOverloadsItself() {
        Clipboard clipboard = new Clipboard(display);
        try {
            IClipboard impl = clipboard.getImpl();
            impl.setContents(new Object[] {"short"}, new Transfer[] {TextTransfer.getInstance()});
            assertThat(impl.getContents(TextTransfer.getInstance())).isEqualTo("short");
            assertThat(Arrays.stream(impl.getAvailableTypes()).anyMatch(TextTransfer.getInstance()::isSupportedType)).isTrue();
            impl.clearContents();
            assertThat(impl.getContents(TextTransfer.getInstance())).isNull();
        } finally {
            clipboard.dispose();
        }
    }

    @Test
    void anImplementationWithoutAnApiMakesOne() {
        DartClipboard impl = new DartClipboard(display, null);
        Clipboard api = impl.getApi();
        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
        impl.dispose();
        assertThat(api.isDisposed()).isTrue();
    }

    @Test
    void theGeneratedAccessorsAnswerTheFields() {
        DartClipboard impl = (DartClipboard) new Clipboard(display).getImpl();
        assertThat(impl._display()).isSameAs(display);
        assertThat(impl._availableTypeNames()).isEmpty();
        assertThat(impl._availableTypes()).isEmpty();
        assertThat(impl._contents()).isNull();
        assertThat(impl.getValue()).isNotNull().isSameAs(impl.getValue());
        impl.dispose();
        assertThat(impl._display()).isNull();
    }
}
