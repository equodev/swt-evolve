package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a scroll performed on the render side owes Java: an echo the client can recognise by value
 * and drop, and the repaint every platform emits when the viewport moves.
 */
@Tag("flutter-it")
class StyledTextScrollEchoFlutterTest {

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;
    private StyledText styledText;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        shell = new Shell(display);
        shell.setSize(400, 300);
        shell.open();
        styledText = new StyledText(shell, SWT.V_SCROLL | SWT.WRAP);
        StringBuilder doc = new StringBuilder();
        for (int i = 0; i < 400; i++) doc.append("line ").append(i).append(" of the document\n");
        styledText.setText(doc.toString());
        styledText.setSize(400, 300);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private VStyledText wire() {
        return (VStyledText) ((DartStyledText) styledText.getImpl()).getValue();
    }

    /** The real inbound route: the render side pushes a StateUpdate payload. */
    private void clientScrolledTo(int topPixel) {
        bridge.comm.fireContaining("StateUpdate",
                ("{\"topPixel\":" + topPixel + "}").getBytes(StandardCharsets.UTF_8));
        while (display.readAndDispatch()) { }
    }

    @Test
    @DisplayName("the offset the client reported is the offset the echo carries")
    void echoCarriesWhatTheClientReported() {
        clientScrolledTo(137);
        assertThat(wire().getTopPixel())
                .as("an echo carrying anything else cannot be recognised by value")
                .isEqualTo(137);
    }

    @Test
    @DisplayName("the offset survives an invalidation of the cached scroll offset")
    void offsetSurvivesInvalidation() {
        clientScrolledTo(137);
        // What resetCache/handleResize do on a wrapped document; all three sites are gated on
        // !isFixedLineHeight().
        ((DartStyledText) styledText.getImpl()).verticalScrollOffset = -1;
        assertThat(wire().getTopPixel())
                .as("recomputed from topIndex, which the handler set to topPixel/lineHeight")
                .isEqualTo(137);
    }

    /**
     * Records the damage of every Paint dispatched after this point. The listener must exist before
     * the scroll — a control hooking no Paint listener is never damaged — and the full-area paint
     * installing it schedules is drained here, so the test sees the scroll's damage, not the setup's.
     */
    private List<Rectangle> recordPaintsFromSettled() {
        List<Rectangle> painted = new ArrayList<>();
        styledText.addPaintListener(e -> painted.add(new Rectangle(e.x, e.y, e.width, e.height)));
        while (display.readAndDispatch()) { }
        painted.clear();
        return painted;
    }

    @Test
    @DisplayName("a scroll performed on the render side still repaints the text widget")
    void clientScrollDamagesTheTextWidget() {
        List<Rectangle> painted = recordPaintsFromSettled();

        clientScrolledTo(40);

        assertThat(painted)
                .as("JFace's line-number ruler is keyed on a PaintListener on the text widget "
                        + "(VisibleLinesTracker), so a scroll that dispatches no Paint leaves the "
                        + "gutter standing still until something else repaints it")
                .isNotEmpty();
    }

    @Test
    @DisplayName("the repaint is scoped to the band the scroll exposed")
    void scrollDamageIsTheExposedBand() {
        int clientHeight = styledText.getClientArea().height;
        List<Rectangle> painted = recordPaintsFromSettled();

        clientScrolledTo(40);

        // What StyledText#scrollVertical invalidates natively: the pixels that stayed are blitted,
        // and only the band the scroll uncovered is repainted. A full-area damage here would
        // repaint the whole editor on every tick, and the render side scrolls a pixel at a time.
        assertThat(painted).isNotEmpty();
        assertThat(painted.get(0))
                .as("scrolled down 40px, so the exposed band is the bottom 40px")
                .isEqualTo(new Rectangle(painted.get(0).x, clientHeight - 40,
                        painted.get(0).width, 40));
    }

    @Test
    @DisplayName("a scroll the client performed does not echo the widget's state back")
    void clientScrollDoesNotEchoState() {
        recordPaintsFromSettled();
        bridge.comm.sent.clear();

        clientScrolledTo(40);

        // Echoing would cost a full-document push per tick for news the client told us.
        assertThat(bridge.comm.sent.stream().map(f -> f.event).toList())
                .as("every frame the scroll produced")
                .noneMatch(e -> e.startsWith("StyledText/")
                        && e.chars().filter(ch -> ch == '/').count() == 1);
    }
}
