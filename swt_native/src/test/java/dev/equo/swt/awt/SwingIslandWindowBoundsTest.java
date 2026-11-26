package dev.equo.swt.awt;

import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.WindowBridge;
import dev.equo.swt.harness.RecordingBridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The engine is told where the OS window that draws an island is, in the pixels and the moment of the
 * frame's own place: a Swing popup is drawn in that window and cut at its edge, which Swing, fitting
 * popups to the screen, knows nothing of.
 */
class SwingIslandWindowBoundsTest {

    /** The bridge of a surface whose shells can have windows of their own. */
    private static final class WindowsBridge extends RecordingBridge implements WindowBridge {
        final Map<Object, Rectangle> windows = new HashMap<>();
        final List<Runnable> listeners = new ArrayList<>();

        @Override
        public Rectangle ownWindowBounds(Object shell) {
            return windows.get(shell);
        }

        /** As the bridge answers it: the shell or its nearest ancestor with a window of its own. */
        @Override
        public Object windowOf(Object shell) {
            for (Composite c = (Shell) shell; c instanceof Shell; c = c.getParent()) {
                if (windows.containsKey(c)) return c;
            }
            return null;
        }

        @Override
        public void addShellWindowListener(Runnable listener) {
            listeners.add(listener);
        }

        @Override
        public void removeShellWindowListener(Runnable listener) {
            listeners.remove(listener);
        }

        void windowsChanged() {
            new ArrayList<>(listeners).forEach(Runnable::run);
        }

        @Override
        public void setWindowMaximized(DartControl control, boolean maximized) {
        }

        @Override
        public void setWindowMinimized(DartControl control, boolean minimized) {
        }

        @Override
        public void setWindowFullScreen(DartControl control, boolean fullScreen) {
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
        }

        @Override
        public Object[] shellsInDrawOrder() {
            return new Object[0];
        }
    }

    private final WindowsBridge bridge = new WindowsBridge();
    private final Frame frame = mock(Frame.class);
    /** What AWT's thread was asked for, in order: the frame's places, and the bounds the engine was told. */
    private final List<String> told = new ArrayList<>();
    private final BiConsumer<Frame, java.awt.Rectangle> engine =
            (of, bounds) -> told.add(of != frame ? "another frame" : bounds == null ? "null" : box(bounds));
    private Display display;
    private Shell main;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(bridge);
        display = new Display();
        main = new Shell(display, SWT.SHELL_TRIM);
        main.setBounds(0, 32, 1920, 1168);
        when(frame.getWidth()).thenReturn(400);
        doAnswer(call -> told.add("at " + call.getArgument(0) + "," + call.getArgument(1)))
                .when(frame).setLocation(anyInt(), anyInt());
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private static String box(java.awt.Rectangle r) {
        return r.x + "," + r.y + " " + r.width + "x" + r.height;
    }

    /** A dialog at 524,359 with a 400x300 island, in a window of its own when {@code window} is given. */
    private Composite islandIn(Shell dialog, Rectangle window) {
        dialog.setBounds(524, 359, 873, 497);
        if (window != null) bridge.windows.put(dialog, window);
        Composite island = new Composite(dialog, SWT.NONE);
        island.setBounds(0, 0, 400, 300);
        return island;
    }

    private void follow(Composite island) throws Exception {
        SwingIslandHost.followIsland(island, frame, engine);
        drainEdt();
    }

    private static void drainEdt() throws Exception {
        java.awt.EventQueue.invokeAndWait(() -> { });
    }

    /** Since the last call. */
    private List<String> told() throws Exception {
        drainEdt();
        List<String> since = new ArrayList<>(told);
        told.clear();
        return since;
    }

    private static String at(Composite island, double scale) {
        Point p = island.toDisplay(0, 0);
        return "at " + Math.round(p.x * scale) + "," + Math.round(p.y * scale);
    }

    @Test
    void theWindowOfTheIslandsShellIsToldWithTheFramesPlace() throws Exception {
        Composite island = islandIn(new Shell(main, SWT.DIALOG_TRIM), new Rectangle(524, 327, 873, 529));

        follow(island);

        assertThat(told()).containsExactly(at(island, 1), "524,327 873x529");
    }

    @Test
    void theBoundsAreInSwingPixels() throws Exception {
        Composite island = islandIn(new Shell(main, SWT.DIALOG_TRIM), new Rectangle(-101, 327, 873, 529));
        when(frame.getWidth()).thenReturn(600);

        follow(island);

        assertThat(told()).containsExactly(at(island, 1.5), "-151,491 1310x794");
    }

    @Test
    void noBoundsUntilTheMirrorHasLaidTheFrameOut() throws Exception {
        Composite island = islandIn(new Shell(main, SWT.DIALOG_TRIM), new Rectangle(524, 327, 873, 529));
        when(frame.getWidth()).thenReturn(0);
        follow(island);
        assertThat(told()).containsExactly(at(island, 1), "null");

        when(frame.getWidth()).thenReturn(400);
        ArgumentCaptor<ComponentListener> resized = ArgumentCaptor.forClass(ComponentListener.class);
        verify(frame).addComponentListener(resized.capture());
        resized.getValue().componentResized(new ComponentEvent(frame, ComponentEvent.COMPONENT_RESIZED));
        drainDisplay();

        assertThat(told()).containsExactly(at(island, 1), "524,327 873x529");
    }

    @Test
    void anIslandOfTheMainWindowHasNone() throws Exception {
        Composite island = new Composite(main, SWT.NONE);
        island.setBounds(10, 10, 400, 300);

        follow(island);

        assertThat(told()).containsExactly(at(island, 1), "null");
    }

    @Test
    void anInlineShellOfAWindowedOneIsDrawnInThatWindow() throws Exception {
        // A Shell without a window of its own is drawn in the window its parent is drawn in.
        Shell windowed = new Shell(main, SWT.DIALOG_TRIM);
        windowed.setBounds(100, 100, 500, 400);
        bridge.windows.put(windowed, new Rectangle(100, 68, 500, 432));
        Composite island = islandIn(new Shell(windowed, SWT.DIALOG_TRIM), null);

        follow(island);

        assertThat(told()).containsExactly(at(island, 1), "100,68 500x432");
    }

    @Test
    void theWindowOfAnInlineShellsParentMovingIsTold() throws Exception {
        Shell windowed = new Shell(main, SWT.DIALOG_TRIM);
        windowed.setBounds(100, 100, 500, 400);
        bridge.windows.put(windowed, new Rectangle(100, 68, 500, 432));
        Composite island = islandIn(new Shell(windowed, SWT.DIALOG_TRIM), null);
        follow(island);
        told();

        bridge.windows.put(windowed, new Rectangle(300, 268, 500, 432));
        windowed.setLocation(300, 300);

        assertThat(told()).containsExactly(at(island, 1), "300,268 500x432");
    }

    @Test
    void anInlineShellOfTheMainWindowsShellHasNone() throws Exception {
        Composite island = islandIn(new Shell(main, SWT.DIALOG_TRIM), null);

        follow(island);

        assertThat(told()).containsExactly(at(island, 1), "null");
    }

    @Test
    void theWindowMovingIsToldWithTheFramesNewPlace() throws Exception {
        Shell dialog = new Shell(main, SWT.DIALOG_TRIM);
        Composite island = islandIn(dialog, new Rectangle(524, 327, 873, 529));
        follow(island);
        told();

        bridge.windows.put(dialog, new Rectangle(100, 68, 873, 529));
        dialog.setLocation(100, 100);

        assertThat(told()).containsExactly(at(island, 1), "100,68 873x529");
    }

    @Test
    void theWindowResizedUnderAnIslandThatKeepsItsSizeIsTold() throws Exception {
        Shell dialog = new Shell(main, SWT.DIALOG_TRIM);
        Composite island = islandIn(dialog, new Rectangle(524, 327, 873, 529));
        follow(island);
        told();

        bridge.windows.put(dialog, new Rectangle(524, 327, 873, 232));
        dialog.setSize(873, 200);

        assertThat(told()).contains("524,327 873x232").doesNotContain("null");
    }

    @Test
    void aWindowOpenedAfterTheIslandIsTold() throws Exception {
        // The Shell's window opens on a flush, after the island was built, and moves nothing.
        Shell dialog = new Shell(main, SWT.DIALOG_TRIM);
        Composite island = islandIn(dialog, null);
        follow(island);
        assertThat(told()).containsExactly(at(island, 1), "null");

        bridge.windows.put(dialog, new Rectangle(524, 327, 873, 529));
        bridge.windowsChanged();

        assertThat(told()).containsExactly(at(island, 1), "524,327 873x529");
    }

    @Test
    void aWindowGoneWhileTheFrameLivesIsCleared() throws Exception {
        Shell dialog = new Shell(main, SWT.DIALOG_TRIM);
        Composite island = islandIn(dialog, new Rectangle(524, 327, 873, 529));
        follow(island);
        told();

        bridge.windows.remove(dialog);
        bridge.windowsChanged();

        assertThat(told()).containsExactly(at(island, 1), "null");
    }

    @Test
    void anIslandMovedToAnotherWindowIsToldThatOne() throws Exception {
        Composite island = islandIn(new Shell(main, SWT.DIALOG_TRIM), new Rectangle(524, 327, 873, 529));
        follow(island);
        told();
        Shell other = new Shell(main, SWT.DIALOG_TRIM);
        other.setBounds(50, 60, 700, 600);
        bridge.windows.put(other, new Rectangle(50, 28, 700, 632));

        island.setParent(other);
        island.setBounds(5, 5, 400, 300);

        assertThat(told()).as("no clear between the two windows").contains("50,28 700x632").doesNotContain("null");
    }

    @Test
    void anIslandMovedToTheMainWindowIsCleared() throws Exception {
        Composite island = islandIn(new Shell(main, SWT.DIALOG_TRIM), new Rectangle(524, 327, 873, 529));
        follow(island);
        told();

        island.setParent(main);
        island.setBounds(5, 5, 400, 300);

        List<String> since = told();
        assertThat(since.get(since.size() - 1)).isEqualTo("null");
    }

    @Test
    void aDisposedIslandClearsTheBoundsAndStopsListening() throws Exception {
        Composite island = islandIn(new Shell(main, SWT.DIALOG_TRIM), new Rectangle(524, 327, 873, 529));
        follow(island);
        told();

        island.dispose();

        assertThat(told()).contains("null").doesNotContain("524,327 873x529");
        assertThat(bridge.listeners).isEmpty();
    }

    @Test
    void theEnginesCallIsGivenTheFrameAndTheBounds() {
        FakeEngine.told.clear();
        BiConsumer<Frame, java.awt.Rectangle> call = SwingIslandHost.windowBoundsCall(FakeEngine.class);

        call.accept(frame, new java.awt.Rectangle(1, 2, 3, 4));
        call.accept(frame, null);

        assertThat(FakeEngine.told).containsExactly(frame, new java.awt.Rectangle(1, 2, 3, 4), frame, null);
    }

    @Test
    void anEngineWithoutTheCallIsLeftAlone() {
        SwingIslandHost.windowBoundsCall(OlderEngine.class).accept(frame, new java.awt.Rectangle(1, 2, 3, 4));
        SwingIslandHost.windowBoundsCall(null).accept(frame, null);
    }

    @Test
    void anEngineThatThrowsDoesNotStopAwt() {
        SwingIslandHost.windowBoundsCall(ThrowingEngine.class).accept(frame, null);
    }

    private void drainDisplay() {
        while (display.readAndDispatch()) {
            /* drain */
        }
    }

    public static final class FakeEngine {
        static final List<Object> told = new ArrayList<>();

        public static void setIslandWindowBounds(Frame island, java.awt.Rectangle bounds) {
            told.add(island);
            told.add(bounds);
        }
    }

    public static final class OlderEngine {
    }

    public static final class ThrowingEngine {
        public static void setIslandWindowBounds(Frame island, java.awt.Rectangle bounds) {
            throw new IllegalStateException("no peer");
        }
    }
}
