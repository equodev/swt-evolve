package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Every public selection entry point on a Dart-backed StyledText has to reach a terminal.
 *
 * {@code setSelection(int, int)} is not one: upstream its body is
 * {@code setSelectionRange(start, end - start)}. So a generated {@code setSelectionRange} that
 * delegates back to it closes a cycle, and the first call from any entry point exhausts the stack
 * before the widget is ever drawn. The terminal differs per SWT version — {@code
 * setSelectionRanges(int[])} is @since 3.117, and before it the package-private
 * {@code setSelection(int, int, boolean, boolean)} — which is why this is asserted per version
 * rather than once.
 */
// Same platform pin as StyledTextKeyboardCaretSyncTest: on the Linux/Windows embed backends
// StyledText.setText routes renderer font metrics into real GTK/Pango / GDI, which cannot run
// under the mocked headless display.
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
@ExtendWith(Mocks.class)
class StyledTextSelectionTerminationTest {

    private final Deque<Runnable> asyncQueue = new ArrayDeque<>();

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
        asyncQueue.clear();
    }

    private StyledText styledText() {
        Shell shell = Mocks.swtShell();
        Display display = shell.getDisplay();
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        StyledText st = new StyledText(shell, SWT.MULTI);
        st.setText("int alpha = 1;\nint beta = alpha;\n");
        st.setBounds(0, 0, 400, 300);
        while (!asyncQueue.isEmpty()) {
            asyncQueue.poll().run();
        }
        return st;
    }

    @Test
    @DisplayName("setSelectionRange terminates and records the range")
    void setSelectionRangeTerminates() {
        StyledText st = styledText();

        st.setSelectionRange(4, 5);

        assertThat(st.getSelection()).isEqualTo(new Point(4, 9));
    }

    @Test
    @DisplayName("setSelection(int, int) terminates and records the range")
    void setSelectionStartEndTerminates() {
        StyledText st = styledText();

        st.setSelection(4, 9);

        assertThat(st.getSelection()).isEqualTo(new Point(4, 9));
    }

    @Test
    @DisplayName("setSelection(int) terminates and collapses the caret")
    void setSelectionCaretTerminates() {
        StyledText st = styledText();

        st.setSelection(4);

        assertThat(st.getSelection()).isEqualTo(new Point(4, 4));
    }
}
