package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A keyboard caret move in the Flutter editor must be visible to Java-side KeyDown listeners.
 * JFace painters (MatchingCharacterPainter, CursorLinePainter) repaint from
 * {@code PaintManager.keyPressed}, which reads {@code getCaretOffset()} while the KeyDown is being
 * dispatched — the native contract is that the widget's own key handling has already applied the
 * caret move by then. If the caret Java reads is stale, the painters redraw at the old position and
 * the matching-bracket frame does not follow arrow-key navigation (the reported symptom).
 *
 * Simulates the Flutter side of the pipeline: a Key/KeyDown bridge event exactly as
 * styledtext_evolve.dart emits it (mapKeyEventToSwt: ARROW_LEFT, no character), fired through a
 * {@link RecordingBridge}. Lives in org.eclipse.swt.widgets so the mocked display's package-private
 * {@code sendEvent(EventTable, Event)} can be wired to really dispatch key events.
 */
// Same platform pin as StyledTextRedrawPaintTest: on the Linux/Windows embed backends,
// StyledText.setText routes renderer font metrics into real GTK/Pango / GDI, which cannot run
// under the mocked headless display. The pipeline under test is shared main-source code.
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
@ExtendWith(Mocks.class)
class StyledTextKeyboardCaretSyncTest {

    private RecordingBridge bridge;
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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        asyncQueue.clear();
    }

    private StyledText styledText() {
        Shell shell = Mocks.swtShell();
        Display display = shell.getDisplay();
        SwtDisplay displayImpl = (SwtDisplay) display.getImpl();
        // Dispatch key events for real (the pipeline under test); everything else stays a mock
        // no-op — the paint/text-change pipelines take platform-specific paths that don't run
        // under the mocked display.
        doAnswer(inv -> {
            Event ev = inv.getArgument(1);
            if (ev != null && (ev.type == SWT.KeyDown || ev.type == SWT.KeyUp
                    || ev.type == SWT.Selection || ev.type == SWT.Verify
                    || ev.type == org.eclipse.swt.custom.ST.VerifyKey)) {
                return inv.callRealMethod();
            }
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        StyledText st = new StyledText(shell, SWT.MULTI);
        st.setText("int alpha = 1;\nint beta = alpha;\n");
        st.setBounds(0, 0, 400, 300);
        pumpAsync();
        return st;
    }

    private void pumpAsync() {
        while (!asyncQueue.isEmpty()) {
            asyncQueue.poll().run();
        }
    }

    /** Fire a Key/KeyDown exactly as styledtext_evolve.dart's mapKeyEventToSwt produces it. */
    private void fireFlutterKeyDown(StyledText st, int keyCode) {
        Event e = new Event();
        e.keyCode = keyCode;
        e.character = 0;
        e.stateMask = 0;
        bridge.comm.fireContaining("/" + st.hashCode() + "/Key/KeyDown", e);
        pumpAsync();
    }

    @Test
    @DisplayName("an arrow-key caret move is applied to the Java caret")
    void arrowKeyMovesJavaCaret() {
        StyledText st = styledText();
        st.setCaretOffset(3);

        fireFlutterKeyDown(st, SWT.ARROW_LEFT);

        assertThat(st.getCaretOffset())
                .as("ArrowLeft from the Flutter editor must move the Java-side caret — any "
                        + "Java consumer of getCaretOffset() is otherwise wrong until the next "
                        + "mouse click")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("a KeyDown listener reads the already-moved caret (JFace painter contract)")
    void keyDownListenerSeesFreshCaret() {
        StyledText st = styledText();
        st.setCaretOffset(3);

        List<Integer> seenAtKeyDown = new ArrayList<>();
        // Registered after construction, like JFace's PaintManager: the widget's internal
        // listener runs first, so the caret must already be at the new offset here.
        st.addListener(SWT.KeyDown, ev -> seenAtKeyDown.add(st.getCaretOffset()));

        fireFlutterKeyDown(st, SWT.ARROW_LEFT);

        assertThat(seenAtKeyDown)
                .as("the app KeyDown listener must run (KeyDown reached the widget)")
                .hasSize(1);
        assertThat(seenAtKeyDown.get(0))
                .as("PaintManager.keyPressed reads getCaretOffset() during KeyDown dispatch; "
                        + "it must see the post-move caret or painters draw at the stale offset")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Home moves the Java caret to the line start")
    void homeMovesJavaCaret() {
        StyledText st = styledText();
        st.setCaretOffset(3);

        fireFlutterKeyDown(st, SWT.HOME);

        assertThat(st.getCaretOffset())
                .as("Home from the Flutter editor must be reflected in the Java-side caret")
                .isEqualTo(0);
    }
}
