package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** The input the Display synthesizes itself: the control under a point, and what {@link Display#post} delivers. */
@Tag("flutter-it")
class DisplayInputFlutterTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (!display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void drain() {
        while (display.readAndDispatch()) {
        }
    }

    private Shell shell(int x, int y, int width, int height) {
        Shell shell = new Shell(display);
        shell.setBounds(x, y, width, height);
        shell.setVisible(true);
        return shell;
    }

    private static Event mouse(int type, int button) {
        Event event = new Event();
        event.type = type;
        event.button = button;
        return event;
    }

    @Test
    void noControlIsUnderAPointOutsideEveryShell() {
        assertThat(DisplayInput.controlAt(display, 10, 10)).isNull();
        shell(100, 100, 200, 150);
        assertThat(DisplayInput.controlAt(display, 10, 10)).isNull();
    }

    @Test
    void theDeepestVisibleControlUnderAPointIsFound() {
        Shell shell = shell(100, 100, 300, 200);
        Composite box = new Composite(shell, SWT.NONE);
        box.setBounds(0, 0, 200, 100);
        Button button = new Button(box, SWT.PUSH);
        button.setBounds(0, 0, 80, 30);
        Label hidden = new Label(box, SWT.NONE);
        hidden.setBounds(100, 0, 80, 30);
        hidden.setVisible(false);

        Point onButton = button.toDisplay(5, 5);
        assertThat(DisplayInput.controlAt(display, onButton.x, onButton.y)).isSameAs(button);
        Point onHidden = box.toDisplay(110, 5);
        assertThat(DisplayInput.controlAt(display, onHidden.x, onHidden.y)).isSameAs(box);
        Point onShell = shell.toDisplay(250, 150);
        assertThat(DisplayInput.controlAt(display, onShell.x, onShell.y)).isSameAs(shell);
    }

    @Test
    void theTopmostShellUnderAPointWins() {
        Shell below = shell(100, 100, 300, 200);
        Shell above = shell(150, 150, 300, 200);
        assertThat(DisplayInput.controlAt(display, 200, 200)).isIn(below, above);
        assertThat(DisplayInput.controlAt(display, 120, 120)).isSameAs(below);
        assertThat(DisplayInput.controlAt(display, 420, 320)).isSameAs(above);
    }

    @Test
    void onlyKeysAndTheFiveMouseButtonsArePosted() {
        assertThat(DisplayInput.post(display, mouse(SWT.MouseDown, 0))).isFalse();
        assertThat(DisplayInput.post(display, mouse(SWT.MouseUp, 6))).isFalse();
        assertThat(DisplayInput.post(display, mouse(SWT.Paint, 1))).isFalse();
        assertThat(DisplayInput.post(display, mouse(SWT.MouseDown, 5))).isTrue();
        drain();
    }

    @Test
    void aKeyWithNoFocusControlGoesNowhere() {
        Event key = new Event();
        key.type = SWT.KeyDown;
        key.character = 'a';
        assertThat(display.getFocusControl()).isNull();
        assertThat(DisplayInput.post(display, key)).isTrue();
        drain();
    }

    @Test
    void aClickWithNoMoveBeforeGoesWhereThePointerIs() {
        Shell shell = shell(0, 0, 300, 200);
        List<Event> downs = new ArrayList<>();
        shell.addListener(SWT.MouseDown, downs::add);
        Point pointer = display.getCursorLocation();
        boolean onShell = shell.getBounds().contains(pointer);
        assertThat(DisplayInput.post(display, mouse(SWT.MouseDown, 1))).isTrue();
        drain();
        assertThat(downs).hasSize(onShell ? 1 : 0);
    }

    @Test
    void aClickWhereNoControlIsGoesNowhere() {
        List<Event> downs = new ArrayList<>();
        Shell shell = shell(100, 100, 200, 150);
        shell.addListener(SWT.MouseDown, downs::add);
        Event move = mouse(SWT.MouseMove, 0);
        move.x = 10;
        move.y = 10;
        assertThat(DisplayInput.post(display, move)).isTrue();
        assertThat(DisplayInput.post(display, mouse(SWT.MouseDown, 1))).isTrue();
        drain();
        assertThat(downs).isEmpty();
    }

    @Test
    void anEventPostedOnADisplayThatIsThenDisposedGoesNowhere() {
        Shell shell = shell(0, 0, 300, 200);
        List<Event> downs = new ArrayList<>();
        shell.addListener(SWT.MouseDown, downs::add);
        boolean[] ran = {false};
        display.disposeExec(() -> ran[0] = true);
        assertThat(DisplayInput.post(display, mouse(SWT.MouseDown, 1))).isTrue();
        display.dispose();
        assertThat(ran[0]).isTrue();
        assertThat(downs).isEmpty();
    }
}
