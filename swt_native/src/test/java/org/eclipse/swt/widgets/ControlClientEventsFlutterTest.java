package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a control does with the events the client reports for it: each reaches the control's listeners on the
 * event loop, once, as the SWT event of the same name; a report that arrives after the control is gone is
 * dropped; and the mouse is not heard while a modal shell blocks the control's shell.
 */
@Tag("flutter-it")
class ControlClientEventsFlutterTest {

    private static final Object[][] REPORTS = {
            {"Control", "Move", SWT.Move},
            {"Control", "Resize", SWT.Resize},
            {"Gesture", "Gesture", SWT.Gesture},
            {"Help", "Help", SWT.Help},
            {"Key", "KeyUp", SWT.KeyUp},
            {"Touch", "Touch", SWT.Touch},
            {"Traverse", "Traverse", SWT.Traverse},
            {"MouseTrack", "MouseEnter", SWT.MouseEnter},
            {"MouseTrack", "MouseExit", SWT.MouseExit},
            {"MouseTrack", "MouseHover", SWT.MouseHover},
            {"Mouse", "MouseDown", SWT.MouseDown},
            {"Mouse", "MouseUp", SWT.MouseUp},
            {"Mouse", "MouseDoubleClick", SWT.MouseDoubleClick},
            {"MouseMove", "MouseMove", SWT.MouseMove},
            {"DragDetect", "DragDetect", SWT.DragDetect},
            {"MenuDetect", "MenuDetect", SWT.MenuDetect},
    };

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;

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
        display = new Display();
        shell = new Shell(display);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void drain() {
        while (display.readAndDispatch()) {
        }
    }

    private void report(Control control, Object[] report) {
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) control.getImpl(), (String) report[0], (String) report[1]), new Event());
    }

    private static List<String> listen(Control control) {
        List<String> heard = new ArrayList<>();
        for (Object[] report : REPORTS) {
            int type = (Integer) report[2];
            control.addListener(type, e -> heard.add(report[1] + (e.widget == control ? "" : " on another widget")));
        }
        return heard;
    }

    @Test
    void eachReportReachesTheListenersOnTheEventLoopOnce() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setSize(50, 50);
        List<String> heard = listen(canvas);
        List<String> expected = new ArrayList<>();
        for (Object[] report : REPORTS) {
            report(canvas, report);
            assertThat(heard).as("%s waits for the event loop", report[1]).isEqualTo(expected);
            drain();
            expected.add((String) report[1]);
            assertThat(heard).isEqualTo(expected);
        }
    }

    @Test
    void aReportForAControlDisposedMeanwhileIsDropped() {
        for (Object[] report : REPORTS) {
            Canvas canvas = new Canvas(shell, SWT.NONE);
            List<String> heard = listen(canvas);
            report(canvas, report);
            canvas.dispose();
            drain();
            assertThat(heard).as((String) report[1]).isEmpty();
        }
    }

    /** The reports the control answers itself are dropped as well once the control is gone. */
    @Test
    void theOtherReportsForADisposedControlAreDropped() {
        String[][] reports = {{"Focus", "FocusIn"}, {"Focus", "FocusOut"}, {"Key", "KeyDown"}, {"MouseWheel", "MouseWheel"},
                {"Paint", "Paint"}};
        for (String[] report : reports) {
            Canvas canvas = new Canvas(shell, SWT.NONE);
            canvas.setSize(20, 20);
            List<Integer> heard = new ArrayList<>();
            for (int type : new int[] {SWT.FocusIn, SWT.FocusOut, SWT.KeyDown, SWT.MouseWheel, SWT.Paint, SWT.Activate})
                canvas.addListener(type, e -> heard.add(e.type));
            bridge.comm.fireContaining(FlutterBridge.event((DartWidget) canvas.getImpl(), report[0], report[1]), new Event());
            canvas.dispose();
            drain();
            assertThat(heard).as(report[1]).isEmpty();
        }
    }

    /** The client's request for a paint is not answered while drawing is off or while a paint is already on its way. */
    @Test
    void aPaintRequestIsNotAnsweredWhileDrawingIsOffOrAPaintIsQueued() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setSize(20, 20);
        List<Integer> paints = new ArrayList<>();
        canvas.addListener(SWT.Paint, e -> paints.add(e.type));
        drain();
        paints.clear();
        canvas.setRedraw(false);
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) canvas.getImpl(), "Paint", "Paint"), new Event());
        drain();
        assertThat(paints).isEmpty();
        canvas.setRedraw(true);
        drain();
        paints.clear();
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) canvas.getImpl(), "Paint", "Paint"), new Event());
        canvas.redraw();
        drain();
        assertThat(paints).hasSize(1);
    }

    /** A FocusOut report waits two turns of the event loop, so a FocusIn reported with it lands first. */
    @Test
    void aFocusOutForAControlDisposedBetweenTheTwoTurnsIsDropped() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        List<Integer> heard = new ArrayList<>();
        canvas.addListener(SWT.FocusOut, e -> heard.add(e.type));
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) canvas.getImpl(), "Focus", "FocusOut"), new Event());
        display.readAndDispatch();
        canvas.dispose();
        drain();
        assertThat(heard).isEmpty();
    }

    @Test
    void theMouseIsNotHeardWhileAModalShellBlocks() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        shell.open();
        Shell modal = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
        modal.open();
        List<String> heard = listen(canvas);
        for (Object[] report : REPORTS) {
            if ("Mouse".equals(report[0]) || "DragDetect".equals(report[0]) || "MenuDetect".equals(report[0]))
                report(canvas, report);
        }
        drain();
        assertThat(heard).isEmpty();
        modal.dispose();
        report(canvas, REPORTS[10]);
        drain();
        assertThat(heard).containsExactly("MouseDown");
    }
}
