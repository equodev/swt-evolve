package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * What one pointer move costs while a GEF selection is being dragged.
 *
 * <p>A drag moves a ghost figure and eight handles per selected part. draw2d's
 * {@code DeferredUpdateManager} unions every dirty region into one rectangle and repaints the whole
 * figure tree under it, so the work per move grows with how far apart the selected parts are — the
 * reported collapse somewhere around five to ten of them. That union is draw2d's own design and
 * native SWT pays it too; what this backend adds on top is the cost of putting each figure's drawing
 * on the wire, and that is what the assertions here pin down.
 *
 * <p>Drives the real draw2d stack over a Dart-backed Canvas through a {@link RecordingBridge}, so it
 * needs no renderer and no display.
 */
@Tag("native-unit")
class GefDragFeedbackCostNativeTest {

    private static final int CANVAS_W = 900;
    private static final int CANVAS_H = 700;
    private static final int NODES = 40;
    private static final int STEPS = 10;

    /** A state push for the GC: {@code GC/<id>}, with no op name after it. */
    private static final String GC_CHANNEL_PREFIX = "GC/";

    private RecordingBridge bridge;

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
    @DisplayName("a drag frame describes the GC whole once and sends every later state push as a change")
    void gcStateTravelsAsWhatChanged() {
        Drag drag = new Drag(8);
        drag.step();

        List<String> states = new ArrayList<>();
        for (RecordingComm.Frame frame : bridge.comm.sent) {
            for (String[] entry : entries(frame.json)) {
                if (isGcState(entry[0])) states.add(entry[1]);
            }
        }

        assertThat(states)
                .as("a repaint of eight selected parts sets an attribute between primitives many times over")
                .hasSizeGreaterThan(10);
        assertThat(states.get(0))
                .as("the client holds nothing yet, so the first push has to describe the GC")
                .doesNotContain("\"_d\"");
        assertThat(states.subList(1, states.size()))
                .as("re-sending the whole GC per primitive is what made a multi-selection drag "
                        + "cost several times what it draws")
                .allMatch(body -> body.contains("\"_d\""));
    }

    @Test
    @DisplayName("a change carries no property it did not name")
    void aChangeCarriesOnlyWhatItNames() {
        Drag drag = new Drag(8);
        drag.step();

        for (RecordingComm.Frame frame : bridge.comm.sent) {
            for (String[] entry : entries(frame.json)) {
                if (!isGcState(entry[0])) continue;
                List<String> named = namedIn(entry[1]);
                if (named == null) continue;
                for (String[] property : fields(entry[1])) {
                    if (property[0].startsWith("_") || property[0].equals("id")
                            || property[0].equals("swt")) {
                        continue;
                    }
                    assertThat(named)
                            .as("a property the client is not told changed cannot be applied to "
                                    + "the state it holds")
                            .contains(property[0]);
                }
            }
        }
    }

    @Test
    @DisplayName("a pointer move costs one Paint however many parts are selected")
    void onePaintPerPointerMove() {
        // draw2d asks for one union rectangle per update, so the client stacks one scoped region
        // per pointer move whatever the selection size: the count of retained repaint layers never
        // tracks the selection, and so cannot be what degrades with it.
        System.out.println();
        System.out.printf("%-4s %-10s %-8s %-12s %s%n", "N", "bytes/move", "paints", "damage px2", "%canvas");
        for (int selected : new int[] {0, 1, 2, 3, 5, 8, 16, 32}) {
            Drag drag = new Drag(selected);
            long bytes0 = bytes();
            for (int s = 0; s < STEPS; s++) drag.step();

            long avgDamage = drag.damages.stream().mapToLong(r -> (long) r.width * r.height).sum()
                    / Math.max(1, drag.damages.size());
            System.out.printf("%-4d %-10d %-8s %-12d %d%%%n", selected,
                    (bytes() - bytes0) / STEPS, drag.damages.size() + "/" + STEPS,
                    avgDamage, avgDamage * 100 / ((long) CANVAS_W * CANVAS_H));

            assertThat(drag.damages)
                    .as("one union rectangle per update means one Paint, at every selection size")
                    .hasSize(STEPS);
        }
    }

    /** A GEF drag in progress: a diagram, a feedback layer, and a pointer that keeps moving. */
    private final class Drag {
        private final FigureCanvas canvas;
        private final Layer handleLayer = new Layer();
        private final List<Figure> moving = new ArrayList<>();
        private final List<Rectangle> damages = new ArrayList<>();
        private int step;

        Drag(int selected) {
            Display display = DartMocks.dartDisplay();
            doAnswer(inv -> {
                ((Runnable) inv.getArgument(0)).run();
                return null;
            }).when(display).asyncExec(any(Runnable.class));
            // DartMocks stubs the Display whole, and dispatch is one of the things it stubs out --
            // so without this no listener runs, draw2d's SWT.Paint hook included.
            DartDisplay impl = (DartDisplay) display.getImpl();
            // That Display is cached for the whole JVM and bound to whichever test thread built it
            // first, so every checkWidget() here answers "Invalid thread access" unless this test
            // takes it over. Harmless: the display is a mock and only one test runs at a time.
            impl.thread = Thread.currentThread();
            doAnswer(inv -> {
                EventTable table = inv.getArgument(0);
                if (table != null) table.sendEvent(inv.getArgument(1));
                return null;
            }).when(impl).sendEvent(any(EventTable.class), any(Event.class));

            canvas = new FigureCanvas(DartMocks.dartShell(display), SWT.DOUBLE_BUFFERED);
            canvas.setSize(CANVAS_W, CANVAS_H);
            // FigureCanvas bound its LightweightSystem to a control that had no size yet, and the
            // root figure stays 0x0 until draw2d hears the control resized.
            canvas.notifyListeners(SWT.Resize, new Event());

            LayeredPane root = new LayeredPane();
            Layer primary = new Layer();
            primary.setLayoutManager(new XYLayout());
            handleLayer.setLayoutManager(new XYLayout());
            root.add(primary, "primary");
            root.add(handleLayer, "handle");
            canvas.setContents(root);

            List<Rectangle> nodes = new ArrayList<>();
            for (int i = 0; i < NODES; i++) {
                Rectangle bounds = new Rectangle(20 + (i % 8) * 108, 20 + (i / 8) * 130, 96, 54);
                nodes.add(bounds);
                Label node = new Label("Node " + i);
                node.setOpaque(true);
                node.setBackgroundColor(ColorConstants.lightGray);
                node.setBorder(new LineBorder(ColorConstants.gray, 1));
                primary.add(node, bounds);
            }
            // What GEF puts on the handle layer for a selection: a ghost of each selected part and
            // the eight handles around it, all of them moving with the pointer.
            for (int i = 0; i < selected; i++) {
                Rectangle bounds = nodes.get(i % NODES);
                RectangleFigure ghost = new RectangleFigure();
                ghost.setFill(false);
                handleLayer.add(ghost, new Rectangle(bounds));
                moving.add(ghost);
                for (int h = 0; h < 8; h++) {
                    RectangleFigure handle = new RectangleFigure();
                    handle.setBackgroundColor(ColorConstants.black);
                    handleLayer.add(handle, handleAt(bounds, h));
                    moving.add(handle);
                }
            }

            // Everything the construction dirtied, so only the drag itself is measured.
            update();
            bridge.comm.sent.clear();
            canvas.addPaintListener(e -> damages.add(new Rectangle(e.x, e.y, e.width, e.height)));
        }

        /** One pointer move. */
        void step() {
            for (Figure figure : moving) {
                Rectangle bounds = figure.getBounds().getCopy();
                bounds.x += 3;
                bounds.y += 2;
                handleLayer.setConstraint(figure, bounds);
            }
            // A marquee drag selects nothing and still invalidates its rubber-band rectangle.
            if (moving.isEmpty()) handleLayer.repaint(10 + step * 3, 10 + step * 2, 120, 90);
            step++;
            update();
        }

        private void update() {
            canvas.getLightweightSystem().getUpdateManager().performUpdate();
        }
    }

    private static Rectangle handleAt(Rectangle b, int i) {
        int[] xs = {b.x, b.x + b.width / 2, b.right(), b.right(), b.right(), b.x + b.width / 2, b.x, b.x};
        int[] ys = {b.y, b.y, b.y, b.y + b.height / 2, b.bottom(), b.bottom(), b.bottom(), b.y + b.height / 2};
        return new Rectangle(xs[i] - 3, ys[i] - 3, 7, 7);
    }

    private long bytes() {
        long total = 0;
        for (RecordingComm.Frame frame : bridge.comm.sent) total += frame.json.length();
        return total;
    }

    private static boolean isGcState(String channel) {
        return channel.startsWith(GC_CHANNEL_PREFIX)
                && channel.indexOf('/', GC_CHANNEL_PREFIX.length()) < 0;
    }

    /** The properties a partial update names, or null when the body is a whole description. */
    private static List<String> namedIn(String body) {
        int at = body.indexOf("\"_d\":[");
        if (at < 0) return null;
        int end = body.indexOf(']', at);
        List<String> names = new ArrayList<>();
        for (String name : body.substring(at + 6, end).split(",")) {
            if (!name.isEmpty()) names.add(name.replace("\"", ""));
        }
        return names;
    }

    /** Splits a batch payload into its {@code (channel, body)} entries. */
    private static List<String[]> entries(String json) {
        List<String[]> out = new ArrayList<>();
        int at = 0;
        while (true) {
            int open = json.indexOf("[\"", at);
            if (open < 0) return out;
            int close = json.indexOf('"', open + 2);
            if (close < 0) return out;
            int body = json.indexOf('{', close);
            if (body < 0) return out;
            int depth = 0, i = body;
            for (; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') depth++;
                else if (c == '}' && --depth == 0) break;
            }
            out.add(new String[] {json.substring(open + 2, close),
                    json.substring(body, Math.min(i + 1, json.length()))});
            at = i + 1;
        }
    }

    /** The top-level {@code key}/{@code value} pairs of one body. */
    private static List<String[]> fields(String body) {
        List<String[]> out = new ArrayList<>();
        int at = 1;
        while (at < body.length()) {
            int open = body.indexOf('"', at);
            if (open < 0) return out;
            int close = body.indexOf('"', open + 1);
            if (close < 0) return out;
            int colon = body.indexOf(':', close);
            if (colon < 0) return out;
            int i = colon + 1, depth = 0;
            while (i < body.length()) {
                char c = body.charAt(i);
                if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') {
                    if (depth == 0) break;
                    depth--;
                } else if (c == ',' && depth == 0) break;
                i++;
            }
            out.add(new String[] {body.substring(open + 1, close), body.substring(colon + 1, i)});
            at = i + 1;
        }
        return out;
    }
}
