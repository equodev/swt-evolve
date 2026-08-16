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
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * JFace painters (AnnotationPainter, MatchingCharacterPainter, CursorLinePainter) hook a
 * PaintListener on the editor's StyledText and then invalidate the damaged region through
 * {@code redraw()} / {@code redraw(x,y,w,h,all)} / {@code redrawRange(start,len,clear)},
 * expecting a Paint event back. If the invalidation never produces a Paint event, their
 * decorations neither appear nor clear until an unrelated full repaint — the reported
 * mark-occurrences / matching-bracket symptom.
 *
 * Lives in org.eclipse.swt.widgets so the mocked display's package-private
 * {@code sendEvent(EventTable, Event)} can be wired to really dispatch; paint delivery is
 * coalesced through {@code asyncExec}, which the test captures and pumps.
 */
// The redraw->paint chain under test (the DartComposite redraw override, ControlHelper.paint,
// firePaint) is shared main-source code, identical on every backend; one platform pins it. On the
// Linux/Windows embed backends, StyledText.setText routes renderer font metrics into real
// GTK/Pango / GDI, which cannot run under the mocked headless display.
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
@ExtendWith(Mocks.class)
class StyledTextRedrawPaintTest {

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
        // Dispatch only the events this test is about: Paint (the delivery under test) and
        // Resize (establishes the client area). Everything else stays a mock no-op — the full
        // text-change pipeline takes platform-specific paths that don't run under the mocked
        // display.
        doAnswer(inv -> {
            Event ev = inv.getArgument(1);
            if (ev != null && (ev.type == SWT.Paint || ev.type == SWT.Resize)) {
                return inv.callRealMethod();
            }
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        StyledText st = new StyledText(shell, SWT.NONE);
        st.setText("int alpha = 1;\nint beta = alpha;\n");
        st.setBounds(0, 0, 400, 300);
        pumpAsync();
        // redrawRange bails out early on a zero client area; the live editor always has one.
        assertThat(st.getClientArea().height).as("harness: client area established").isPositive();
        return st;
    }

    private void pumpAsync() {
        Runnable r;
        while ((r = asyncQueue.poll()) != null) {
            r.run();
        }
    }

    @Test
    @DisplayName("redraw() delivers a Paint event to a hooked painter")
    void fullRedrawDeliversPaint() {
        StyledText st = styledText();
        AtomicInteger paints = new AtomicInteger();
        st.addPaintListener(e -> paints.incrementAndGet());
        st.redraw();
        pumpAsync();
        assertThat(paints.get()).as("Paint events after redraw()").isPositive();
    }

    @Test
    @DisplayName("redraw(x,y,w,h,all) delivers a Paint event to a hooked painter")
    void rectRedrawDeliversPaint() {
        StyledText st = styledText();
        AtomicInteger paints = new AtomicInteger();
        st.addPaintListener(e -> paints.incrementAndGet());
        st.redraw(0, 0, 50, 20, false);
        pumpAsync();
        assertThat(paints.get()).as("Paint events after redraw(x,y,w,h,all)").isPositive();
    }

    @Test
    @DisplayName("redrawRange(start,len,clear) delivers a Paint event to a hooked painter")
    void redrawRangeDeliversPaint() {
        StyledText st = styledText();
        AtomicInteger paints = new AtomicInteger();
        st.addPaintListener(e -> paints.incrementAndGet());
        // the damage window MatchingCharacterPainter would invalidate around "alpha"
        st.redrawRange(4, 5, true);
        pumpAsync();
        assertThat(paints.get()).as("Paint events after redrawRange(4,5,true)").isPositive();
    }
}
