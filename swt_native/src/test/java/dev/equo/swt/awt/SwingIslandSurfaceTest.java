package dev.equo.swt.awt;

import java.awt.Frame;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.SwingIsland;
import dev.equo.swt.WindowPolicy;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A secondary Swing window the client's mirror bundle routes to a Shell of its own: Java builds
 * that Shell on the bundle's request, with a {@link SwingIsland} bound to the window the engine
 * already has.
 * swing-evolve's engine is not on this classpath: the Shell entry reaches nothing of it.
 */
class SwingIslandSurfaceTest {

    private static final int PORT = 8765;

    private RecordingBridge bridge;
    private Display display;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        SwingIslandHost.listen(display, bridge.comm);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private static Map<String, Object> request(int windowId, String kind, int hostOwnerId) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("windowId", windowId);
        request.put("port", PORT);
        request.put("kind", kind);
        request.put("title", "Point Info");
        request.put("resizable", true);
        request.put("hostOwnerId", hostOwnerId);
        request.put("x", 40);
        request.put("y", 30);
        request.put("w", 300);
        request.put("h", 200);
        return request;
    }

    /** The bundle's request, as it arrives from the client, and the asyncExec it queues. */
    private Shell open(Map<String, Object> request) {
        bridge.comm.fireContaining(SwingIslandHost.SURFACE_EVENT, request);
        drain();
        return SwingIslandHost.surfaceShell(display, (Integer) request.get("windowId"));
    }

    private void drain() {
        while (display.readAndDispatch()) {}
    }

    private static SwingIsland islandIn(Shell shell) {
        Control[] children = shell.getChildren();
        assertThat(children).hasSize(1);
        assertThat(children[0]).isInstanceOf(SwingIsland.class);
        return (SwingIsland) children[0];
    }

    /** What Java answers the island's region with when it asks which window it mirrors. */
    private Map<String, String> answerTo(SwingIsland island) {
        String channel = FlutterBridge.eventName(island.getImpl(), SwingIslandHost.ISLAND_EVENT);
        bridge.comm.sent.clear();
        bridge.comm.fireContaining(channel, new byte[0]);
        return lastSentOn(channel);
    }

    private Map<String, String> lastSentOn(String channel) {
        List<RecordingComm.Frame> frames = bridge.comm.sent.stream()
                .filter(f -> f.event.equals(channel)).collect(Collectors.toList());
        assertThat(frames).as("frames sent on " + channel).isNotEmpty();
        return fields(frames.get(frames.size() - 1).json);
    }

    /** The JSON object's fields, as text: enough for the flat maps this entry sends. */
    private static Map<String, String> fields(String json) {
        String body = json.substring(json.indexOf('{') + 1, json.lastIndexOf('}'));
        Map<String, String> fields = new LinkedHashMap<>();
        for (String pair : body.split(",")) {
            String[] kv = pair.split(":", 2);
            fields.put(kv[0].trim().replace("\"", ""), kv[1].trim().replace("\"", ""));
        }
        return fields;
    }

    @Test
    @DisplayName("a top-level frame gets a parentless trimmed Shell, holding an island bound to its window")
    void topLevelFrame() {
        int framesBefore = Frame.getFrames().length;

        Shell shell = open(request(9, "frame", 0));

        assertThat(shell).isNotNull();
        assertThat(shell.getParent()).isNull();
        assertThat(shell.getStyle() & SWT.SHELL_TRIM).isEqualTo(SWT.SHELL_TRIM);
        assertThat(shell.getText()).isEqualTo("Point Info");
        assertThat(shell.isVisible()).isTrue();
        Map<String, String> answer = answerTo(islandIn(shell));
        assertThat(answer).containsEntry("windowId", "9").containsEntry("port", String.valueOf(PORT))
                .containsEntry("surface", "true");
        assertThat(Frame.getFrames()).as("the window is the engine's; Java makes no AWT frame for it")
                .hasSize(framesBefore);
    }

    @Test
    @DisplayName("the bundle is told once the Shell is open, wherever it is drawn")
    void openedIsAnnounced() {
        bridge.comm.sent.clear();

        open(request(9, "frame", 0));

        assertThat(lastSentOn(SwingIslandHost.OPENED_EVENT)).containsEntry("windowId", "9");
    }

    @Test
    @DisplayName("a Shell takes the size Java gives its window afterwards")
    void resizeFollowsJava() {
        Shell shell = open(request(9, "frame", 0));
        Point location = shell.getLocation();

        bridge.comm.fireContaining(SwingIslandHost.RESIZE_EVENT, Map.of("windowId", 9, "w", 500.0, "h", 250.0));
        drain();

        Rectangle area = shell.getClientArea();
        assertThat(new Point(area.width, area.height)).isEqualTo(new Point(500, 250));
        assertThat(shell.getLocation()).as("position is the host's after the first show").isEqualTo(location);
    }

    @Test
    @DisplayName("a Swing window's Shell stays in the Display's window, whatever the multi-window mode")
    void staysInline() {
        String mode = System.getProperty(WindowPolicy.MODE_PROPERTY);
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        try {
            Shell shell = open(request(9, "frame", 0));

            assertThat(WindowPolicy.ownsWindow(shell, false))
                    .as("a Shell drawn by another Flutter client would have no mirror to show")
                    .isFalse();
        } finally {
            if (mode == null) System.clearProperty(WindowPolicy.MODE_PROPERTY);
            else System.setProperty(WindowPolicy.MODE_PROPERTY, mode);
        }
    }

    @Test
    @DisplayName("the window's size is the Shell's client area, and a frame sits at its own position")
    void bounds() {
        Shell shell = open(request(9, "frame", 0));

        Rectangle area = shell.getClientArea();
        assertThat(new Point(area.width, area.height)).isEqualTo(new Point(300, 200));
        assertThat(shell.getLocation()).isEqualTo(new Point(40, 30));
    }

    @Test
    @DisplayName("a frame that is not resizable cannot be resized or maximized")
    void fixedFrame() {
        Map<String, Object> request = request(9, "frame", 0);
        request.put("resizable", false);

        Shell shell = open(request);

        assertThat(shell.getStyle() & (SWT.RESIZE | SWT.MAX)).isZero();
        assertThat(shell.getStyle() & SWT.TITLE).isNotZero();
    }

    @Test
    @DisplayName("a dialog owned by an island is parented to the island's Shell")
    void dialogOwnedByIsland() {
        Shell host = new Shell(display);
        Composite embedded = new Composite(host, SWT.EMBEDDED);
        embedded.setLayout(new FillLayout());
        SwingIsland island = new SwingIsland(embedded, SWT.NONE);
        SwingIslandHost.mount(island, 3, PORT, false);

        Shell dialog = open(request(5, "dialog", 3));

        assertThat(dialog.getParent()).isSameAs(host);
        assertThat(dialog.getStyle() & SWT.DIALOG_TRIM).isEqualTo(SWT.DIALOG_TRIM);
        assertThat(dialog.getStyle() & SWT.RESIZE).as("the dialog is resizable").isNotZero();
    }

    @Test
    @DisplayName("a dialog keeps its offset from the island that owns it")
    void dialogKeepsItsOffset() {
        Shell host = new Shell(display);
        host.setBounds(100, 100, 800, 600);
        host.setLayout(new FillLayout());
        SwingIsland island = new SwingIsland(host, SWT.NONE);
        host.layout(true);
        SwingIslandHost.mount(island, 3, PORT, false);
        Map<String, Object> request = request(5, "dialog", 3);
        request.put("dx", 20.0);
        request.put("dy", 10.0);

        Shell dialog = open(request);

        assertThat(dialog.getLocation()).isEqualTo(island.toDisplay(20, 10));
    }

    @Test
    @DisplayName("a dialog owned by a window with a Shell of its own is parented to that Shell")
    void dialogOwnedByShell() {
        Shell frame = open(request(9, "frame", 0));
        Map<String, Object> request = request(5, "dialog", 9);
        request.put("resizable", false);

        Shell dialog = open(request);

        assertThat(dialog.getParent()).isSameAs(frame);
        assertThat(dialog.getStyle() & SWT.RESIZE).isZero();
    }

    @Test
    @DisplayName("an ownerless dialog is parented to the active Shell and centred on it")
    void ownerlessDialog() {
        Shell main = new Shell(display);
        main.setBounds(100, 100, 800, 600);
        main.open();

        Shell dialog = open(request(5, "dialog", 0));

        assertThat(dialog.getParent()).isSameAs(main);
        Rectangle bounds = dialog.getBounds();
        assertThat(new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2))
                .isEqualTo(new Point(500, 400));
    }

    @Test
    @DisplayName("an undecorated window has no trim")
    void undecoratedWindow() {
        Shell shell = open(request(6, "window", 0));

        assertThat(shell.getStyle() & (SWT.TITLE | SWT.BORDER | SWT.RESIZE)).isZero();
    }

    @Test
    @DisplayName("closing the Shell asks Java's window to close, and leaves the Shell to Java's decision")
    void closeIsVetoedAndForwarded() {
        Shell shell = open(request(9, "frame", 0));
        bridge.comm.sent.clear();

        shell.close();

        assertThat(shell.isDisposed()).as("a DO_NOTHING_ON_CLOSE window must stay").isFalse();
        assertThat(lastSentOn(SwingIslandHost.CLOSE_EVENT)).containsEntry("windowId", "9");
    }

    @Test
    @DisplayName("the bundle's teardown disposes the Shell")
    void disposeOnRequest() {
        Shell shell = open(request(9, "frame", 0));

        bridge.comm.fireContaining(SwingIslandHost.DISPOSE_EVENT, Map.of("windowId", 9));
        drain();

        assertThat(shell.isDisposed()).isTrue();
        assertThat(SwingIslandHost.surfaceShell(display, 9)).isNull();
    }

    @Test
    @DisplayName("a second request for one window replaces its Shell")
    void reopenReplaces() {
        Shell first = open(request(9, "frame", 0));

        Shell second = open(request(9, "frame", 0));

        assertThat(first.isDisposed()).isTrue();
        assertThat(second.isDisposed()).isFalse();
        assertThat(Arrays.stream(display.getShells()).filter(s -> !s.isDisposed())).containsExactly(second);
    }
}
