package org.eclipse.swt.widgets;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * On the desktop-native surface {@link Display#getCursorLocation()} answers where the OS pointer is,
 * as native SWT does, wherever it rests: before any client MouseMove and outside every Evolve window;
 * {@link Display#setCursorLocation(Point)} moves it.
 *
 * <p>The native window and the OS pointer are stubbed behind the bridge's seams, as in
 * {@link DisplayWakeDeskFlutterTest}.
 */
@Tag("flutter-it")
class DisplayCursorLocationDeskFlutterTest {

    private Display display;
    private String savedMode;

    /** The desk bridge sets the mode for the whole JVM; a later test class must find it as it was. */
    @BeforeEach
    void saveMode() {
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
        if (savedMode == null)
            System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else
            System.setProperty(ConfigFlags.MODE_PROPERTY, savedMode);
    }

    @Test
    void beforeAnyClientMouseMove_answersTheOsPointer() {
        TestDeskBridge desk = install();
        desk.osPointer = new Point(412, 287);

        assertThat(display.getCursorLocation()).isEqualTo(new Point(412, 287));
    }

    @Test
    void afterThePointerLeftTheWindow_answersTheOsPointer() {
        TestDeskBridge desk = install();
        Shell shell = new Shell(display);
        shell.setBounds(100, 100, 300, 200);
        Event move = new Event();
        move.x = 10;
        move.y = 10;
        desk.comm.fireContaining("MouseMove/MouseMove", move);
        while (display.readAndDispatch()) {
            /* deliver the client's MouseMove */
        }

        desk.osPointer = new Point(900, 700);

        assertThat(display.getCursorLocation()).isEqualTo(new Point(900, 700));
    }

    @Test
    void setCursorLocation_movesTheOsPointer() {
        TestDeskBridge desk = install();
        desk.osPointer = new Point(5, 5);

        display.setCursorLocation(new Point(640, 360));

        assertThat(desk.osPointer).isEqualTo(new Point(640, 360));
        assertThat(display.getCursorLocation()).isEqualTo(new Point(640, 360));
    }

    /** The OS reports its own move through the client, as natively; a posted one would make two. */
    @Test
    void setCursorLocation_postsNoMouseMoveOfItsOwnWhenTheOsPointerMoved() {
        TestDeskBridge desk = install();
        java.util.List<Event> moves = mouseEventsOn(openShell(), SWT.MouseMove);

        display.setCursorLocation(150, 150);
        dispatchPending();

        assertThat(desk.osPointer).isEqualTo(new Point(150, 150));
        assertThat(moves).isEmpty();
    }

    @Test
    void setCursorLocation_postsTheMouseMoveWhenTheOsPointerCannotMove() {
        TestDeskBridge desk = install();
        desk.canMovePointer = false;
        Shell shell = openShell();
        java.util.List<Event> moves = mouseEventsOn(shell, SWT.MouseMove);

        display.setCursorLocation(150, 150);
        dispatchPending();

        assertThat(moves).hasSize(1);
        assertThat(new Point(moves.get(0).x, moves.get(0).y)).isEqualTo(shell.toControl(150, 150));
    }

    @Test
    void aPostedClickAfterSetCursorLocation_landsAtTheNewPoint() {
        install();
        Shell shell = openShell();
        java.util.List<Event> downs = mouseEventsOn(shell, SWT.MouseDown);
        Event earlier = new Event();
        earlier.type = SWT.MouseMove;
        earlier.x = 120;
        earlier.y = 120;
        display.post(earlier);
        dispatchPending();

        display.setCursorLocation(150, 150);
        Event down = new Event();
        down.type = SWT.MouseDown;
        down.button = 1;
        display.post(down);
        dispatchPending();

        assertThat(downs).hasSize(1);
        assertThat(new Point(downs.get(0).x, downs.get(0).y)).isEqualTo(shell.toControl(150, 150));
    }

    @Test
    void returnedPoint_isACopy() {
        TestDeskBridge desk = install();
        desk.osPointer = new Point(412, 287);

        display.getCursorLocation().x = 0;

        assertThat(display.getCursorLocation()).isEqualTo(new Point(412, 287));
    }

    /** A drag opens the Tracker with a button held: native SWT leaves the pointer where the user has it. */
    @Test
    void aTrackerOpenedWithAButtonHeld_leavesTheOsPointer() {
        TestDeskBridge desk = install();
        desk.osPointer = new Point(412, 287);
        desk.buttonDown = true;

        openTrackerOver(new Rectangle(10000, -10000, 200, 100));

        assertThat(desk.osPointer).isEqualTo(new Point(412, 287));
    }

    /** Opened from the keyboard, the Tracker takes the pointer to its rectangles, as native SWT does. */
    @Test
    void aTrackerOpenedWithNoButtonHeld_movesTheOsPointerToItsRectangles() {
        TestDeskBridge desk = install();
        desk.osPointer = new Point(412, 287);

        openTrackerOver(new Rectangle(300, 200, 200, 100));

        assertThat(desk.osPointer).isEqualTo(new Point(400, 200));
    }

    private void openTrackerOver(Rectangle rectangle) {
        Tracker tracker = new Tracker(display, SWT.NONE);
        tracker.setRectangles(new Rectangle[] {rectangle});
        display.asyncExec(tracker::close);
        tracker.open();
        tracker.dispose();
    }

    private Shell openShell() {
        Shell shell = new Shell(display);
        shell.setBounds(100, 100, 300, 200);
        shell.open();
        dispatchPending();
        return shell;
    }

    private static java.util.List<Event> mouseEventsOn(Control control, int type) {
        java.util.List<Event> events = new java.util.ArrayList<>();
        control.addListener(type, events::add);
        return events;
    }

    private void dispatchPending() {
        while (display.readAndDispatch()) {
            /* deliver what is queued */
        }
    }

    private TestDeskBridge install() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestDeskBridge bridge = new TestDeskBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** Desktop-native bridge with the native window stubbed and the OS pointer set by the test. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        Point osPointer;
        boolean canMovePointer = true;
        boolean buttonDown;

        TestDeskBridge(DartDisplay display) {
            super(display);
        }

        @Override
        public Point cursorLocation() {
            return osPointer == null ? null : new Point(osPointer.x, osPointer.y);
        }

        @Override
        public boolean pointerButtonDown() {
            return buttonDown;
        }

        @Override
        protected boolean moveCursor(int x, int y) {
            if (canMovePointer)
                osPointer = new Point(x, y);
            return canMovePointer;
        }

        @Override
        protected Point shellWindowOrigin(long context) {
            return null;
        }

        @Override
        protected int workAreaTop(Rectangle window) {
            return 0;
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }

        @Override
        protected boolean hasWindowSurface() {
            return true;
        }

        @Override
        protected void wakeNativeWindow() {
        }

        @Override
        protected int pumpWindow() {
            return 0;
        }

        @Override
        protected void disposeNativeWindow() {
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
        }
    }
}
