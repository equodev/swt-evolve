package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code Control.dragDetect} on a first-button press follows the pointer the client reports, as every native does:
 * a move past the hysteresis or a press held past the timeout is a drag, a release is not. The reports it reads reach
 * the listeners after it answers, and the event loop keeps running meanwhile.
 */
@Tag("flutter-it")
class DragDetectFlutterTest {

    private static final int PRESS_X = 20;
    private static final int PRESS_Y = 30;

    private RecordingBridge bridge;
    private Display display;
    private Canvas canvas;
    private final List<String> heard = new ArrayList<>();

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
        Shell shell = new Shell(display);
        canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 60, 60);
        // A cold report path can take longer than the timeout on a slow machine: load it before any test reports.
        report("MouseMove", "MouseMove", PRESS_X, PRESS_Y);
        drain();
        canvas.addListener(SWT.MouseMove, e -> heard.add("MouseMove " + e.x + "," + e.y));
        canvas.addListener(SWT.MouseUp, e -> heard.add("MouseUp"));
        canvas.addListener(SWT.DragDetect, e -> heard.add("DragDetect " + e.x + "," + e.y));
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

    private void report(String eventName, String eventType, int x, int y) {
        report(eventName, eventType, x, y, 1);
    }

    private void report(String eventName, String eventType, int x, int y, int button) {
        report(canvas, eventName, eventType, x, y, button);
    }

    private void report(Control target, String eventName, String eventType, int x, int y, int button) {
        Event report = new Event();
        report.button = button;
        report.x = x;
        report.y = y;
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) target.getImpl(), eventName, eventType), report);
    }

    private boolean dragDetect() {
        Event press = new Event();
        press.button = 1;
        press.count = 1;
        press.x = PRESS_X;
        press.y = PRESS_Y;
        return canvas.dragDetect(press);
    }

    @Test
    void aPressHeldWithoutMovingIsADragOnceTheTimeoutPasses() {
        assertThat(dragDetect()).isTrue();
        drain();
        assertThat(heard).containsExactly("DragDetect 20,30");
    }

    @Test
    void aMovePastTheHysteresisIsADragAndTheMoveIsHeardAfterTheAnswer() {
        report("MouseMove", "MouseMove", PRESS_X + 6, PRESS_Y);

        assertThat(dragDetect()).isTrue();
        assertThat(heard).isEmpty();
        drain();
        assertThat(heard).containsExactly("MouseMove 26,30", "DragDetect 20,30");
    }

    @Test
    void aReleaseIsNotADragAndTheReportsAreHeardAfterTheAnswerInOrder() {
        report("MouseMove", "MouseMove", PRESS_X + 5, PRESS_Y - 5);
        report("Mouse", "MouseUp", PRESS_X + 5, PRESS_Y - 5);

        assertThat(dragDetect()).isFalse();
        assertThat(heard).isEmpty();
        drain();
        assertThat(heard).containsExactly("MouseMove 25,25", "MouseUp");
    }

    @Test
    void aVerticalMovePastTheHysteresisIsADrag() {
        report("MouseMove", "MouseMove", PRESS_X, PRESS_Y + 6);

        assertThat(dragDetect()).isTrue();
        drain();
        assertThat(heard).containsExactly("MouseMove 20,36", "DragDetect 20,30");
    }

    @Test
    void aReportQueuedAfterTheDecidingOneIsLeftForAfterTheDragDetect() {
        report("MouseMove", "MouseMove", PRESS_X + 6, PRESS_Y);
        report("Mouse", "MouseUp", PRESS_X + 6, PRESS_Y);

        assertThat(dragDetect()).isTrue();
        assertThat(heard).isEmpty();
        drain();
        assertThat(heard).containsExactly("MouseMove 26,30", "DragDetect 20,30", "MouseUp");
    }

    @Test
    void aReportANestedEventLoopReadsAfterTheDecidingOneIsHeldAndHeardBeforeTheDragDetect() {
        display.asyncExec(this::drain);
        report("MouseMove", "MouseMove", PRESS_X + 6, PRESS_Y);
        report("Mouse", "MouseUp", PRESS_X + 6, PRESS_Y);

        assertThat(dragDetect()).isTrue();
        assertThat(heard).isEmpty();
        drain();
        assertThat(heard).containsExactly("MouseMove 26,30", "MouseUp", "DragDetect 20,30");
    }

    @Test
    void theClientsDragDetectForTheDetectedPressIsNotASecondOne() {
        report("DragDetect", "DragDetect", PRESS_X, PRESS_Y);
        report("MouseMove", "MouseMove", PRESS_X + 6, PRESS_Y);

        assertThat(dragDetect()).isTrue();
        assertThat(heard).isEmpty();
        drain();
        assertThat(heard).containsExactly("MouseMove 26,30", "DragDetect 20,30");
    }

    @Test
    void anotherControlsDragDetectIsHeardWhileThePointerIsFollowed() {
        Canvas other = new Canvas(canvas.getShell(), SWT.NONE);
        other.addListener(SWT.DragDetect, e -> heard.add("other DragDetect " + e.x + "," + e.y));
        report(other, "DragDetect", "DragDetect", 5, 5, 1);
        report("MouseMove", "MouseMove", PRESS_X + 6, PRESS_Y);

        assertThat(dragDetect()).isTrue();
        drain();
        assertThat(heard).containsExactly("other DragDetect 5,5", "MouseMove 26,30", "DragDetect 20,30");
    }

    @Test
    void theClientsDragDetectForAPressHeldPastTheTimeoutIsNotASecondOne() {
        assertThat(dragDetect()).isTrue();
        report("DragDetect", "DragDetect", PRESS_X, PRESS_Y);
        report("MouseMove", "MouseMove", PRESS_X + 30, PRESS_Y);
        drain();

        assertThat(heard).containsExactly("DragDetect 20,30", "MouseMove 50,30");
    }

    @Test
    void theClientsDragDetectForAPressReleasedBeforeTheAnswerIsNotHeard() {
        report("DragDetect", "DragDetect", PRESS_X, PRESS_Y);
        report("Mouse", "MouseUp", PRESS_X, PRESS_Y);

        assertThat(dragDetect()).isFalse();
        drain();
        assertThat(heard).containsExactly("MouseUp");
    }

    @Test
    void thePressAfterTheReleaseGetsTheClientsDragDetect() {
        assertThat(dragDetect()).isTrue();
        report("Mouse", "MouseUp", PRESS_X, PRESS_Y);
        drain();
        report("DragDetect", "DragDetect", PRESS_X, PRESS_Y);
        drain();

        assertThat(heard).containsExactly("DragDetect 20,30", "MouseUp", "DragDetect 20,30");
    }

    @Test
    void aNewPressGetsTheClientsDragDetectWhenTheLastOneWasNeverReleased() {
        assertThat(dragDetect()).isTrue();
        drain();
        report("Mouse", "MouseDown", PRESS_X, PRESS_Y);
        report("DragDetect", "DragDetect", PRESS_X, PRESS_Y);
        drain();

        assertThat(heard).containsExactly("DragDetect 20,30", "DragDetect 20,30");
    }

    @Test
    void anotherButtonComingUpIsHeardAtOnceAndEndsNothing() {
        report("Mouse", "MouseUp", PRESS_X, PRESS_Y, 3);

        assertThat(dragDetect()).isTrue();
        assertThat(heard).containsExactly("MouseUp");
        drain();
        assertThat(heard).containsExactly("MouseUp", "DragDetect 20,30");
    }

    @Test
    void aControlDisposedWhileThePointerIsFollowedIsNotADrag() {
        display.asyncExec(canvas::dispose);

        assertThat(dragDetect()).isFalse();
        assertThat(display.getData("dev.equo.swt.dragDetect")).isNull();
    }

    @Test
    void aDisplayDisposedWhileThePointerIsFollowedEndsTheDetection() {
        display.asyncExec(display::dispose);

        assertThat(dragDetect()).isFalse();
        assertThat(display.isDisposed()).isTrue();
    }

    @Test
    void aDisplayDisposedWithAReleaseHeldEndsTheDetectionWithoutCompletingIt() {
        display.asyncExec(() -> {
            drain();
            display.dispose();
        });
        report("MouseMove", "MouseMove", PRESS_X + 6, PRESS_Y);
        report("Mouse", "MouseUp", PRESS_X + 6, PRESS_Y);

        assertThat(dragDetect()).isFalse();
        assertThat(display.isDisposed()).isTrue();
    }

    @Test
    void aControlDisposedWithReportsHeldIsNotADragAndHearsNone() {
        display.asyncExec(() -> {
            drain();
            canvas.dispose();
        });
        report("MouseMove", "MouseMove", PRESS_X + 6, PRESS_Y);
        report("Mouse", "MouseUp", PRESS_X + 6, PRESS_Y);

        assertThat(dragDetect()).isFalse();
        drain();
        assertThat(heard).isEmpty();
    }

    @Test
    void aControlItsMouseUpListenerDisposesIsNotReleasedAfterwards() {
        canvas.addListener(SWT.MouseUp, e -> canvas.dispose());

        report("Mouse", "MouseUp", PRESS_X, PRESS_Y);
        drain();

        assertThat(canvas.isDisposed()).isTrue();
        assertThat(heard).containsExactly("MouseUp");
    }

    @Test
    void theEventLoopKeepsRunningWhileThePointerIsFollowed() {
        boolean[] ran = {false};
        display.asyncExec(() -> ran[0] = true);

        dragDetect();

        assertThat(ran[0]).isTrue();
    }

    @Test
    void aSecondButtonPressIsNeverADrag() {
        Event press = new Event();
        press.button = 3;
        press.count = 1;

        assertThat(canvas.dragDetect(press)).isFalse();
    }

    @Test
    void aListClickTheDetectionFollowedReportsItsSelectionAfterTheMouseUp() {
        org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(canvas.getShell(), SWT.SINGLE);
        list.setBounds(0, 0, 60, 60);
        list.add("item 0");
        list.add("item 1");
        list.addListener(SWT.MouseDown, e -> heard.add("MouseDown dragDetect=" + list.dragDetect(e)));
        list.addListener(SWT.MouseUp, e -> heard.add("MouseUp selection=" + list.getSelectionIndex()));
        list.addListener(SWT.Selection, e -> heard.add("Selection"));
        list.addListener(SWT.DragDetect, e -> heard.add("DragDetect"));
        Event press = new Event();
        press.button = 1;
        press.count = 1;
        press.x = PRESS_X;
        press.y = PRESS_Y;
        Event pick = new Event();
        pick.segments = new int[] {1};

        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) list.getImpl(), "Mouse", "MouseDown"), press);
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) list.getImpl(), "Selection", "Selection"), pick);
        report(list, "Mouse", "MouseUp", PRESS_X, PRESS_Y, 1);
        drain();

        assertThat(heard).containsExactly("MouseDown dragDetect=false", "MouseUp selection=1", "Selection");
    }
}
