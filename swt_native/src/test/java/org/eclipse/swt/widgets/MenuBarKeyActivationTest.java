package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/** F10 or a lone Alt makes the shell's menu bar take the keyboard, as on Windows. */
class MenuBarKeyActivationTest extends KeyRoutingTestBase {

    private RecordingBridge bridge;
    private Shell shell;
    private Text focus;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        shell = shell();
        focus = new Text(shell, SWT.BORDER);
        when(shell.getChildren()).thenReturn(new Control[] {focus});
        // The lone Alt is remembered in the Display's data, which the mocked display does not keep.
        Map<String, Object> displayData = new HashMap<>();
        Display display = shell.getDisplay();
        doAnswer(inv -> displayData.put(inv.getArgument(0), inv.getArgument(1))).when(display).setData(anyString(), any());
        doAnswer(inv -> displayData.get(inv.getArgument(0))).when(display).getData(anyString());
    }

    @Test
    @DisplayName("F10 is delivered as a KeyDown and then activates the menu bar")
    void f10ActivatesTheBar() {
        Menu bar = menuBar();
        int[] keyDowns = {0};
        focus.addListener(SWT.KeyDown, e -> keyDowns[0]++);

        press(focus, SWT.F10);

        assertThat(keyDowns[0]).isEqualTo(1);
        assertThat(activations()).containsExactly(FlutterBridge.eventName(bar.getImpl(), "activate"));
    }

    @Test
    @DisplayName("an F10 a KeyDown listener vetoes does not activate the menu bar")
    void vetoedF10DoesNotActivate() {
        menuBar();
        focus.addListener(SWT.KeyDown, e -> e.doit = false);

        press(focus, SWT.F10);

        assertThat(activations()).isEmpty();
    }

    @Test
    @DisplayName("Shift+F10 does not activate the menu bar")
    void shiftF10DoesNotActivate() {
        menuBar();

        press(focus, SWT.F10, SWT.SHIFT);

        assertThat(activations()).isEmpty();
    }

    @Test
    @DisplayName("Alt pressed and released alone activates the menu bar on its release")
    void loneAltActivatesOnRelease() {
        Menu bar = menuBar();

        press(focus, SWT.ALT);
        assertThat(activations()).isEmpty();
        release(focus, SWT.ALT);

        assertThat(activations()).containsExactly(FlutterBridge.eventName(bar.getImpl(), "activate"));
    }

    @Test
    @DisplayName("Alt released after another key was pressed with it does not activate the menu bar")
    void altWithAnotherKeyDoesNotActivate() {
        menuBar();

        press(focus, SWT.ALT);
        press(focus, SWT.TAB, SWT.ALT);
        release(focus, SWT.ALT);

        assertThat(activations()).isEmpty();
    }

    @Test
    @DisplayName("Alt+F after a lone Alt activated the bar and Escape left it opens File")
    void altLetterAfterLoneAltAndEscapeOpensTheMenu() {
        Menu bar = menuBar();

        press(focus, SWT.ALT);
        release(focus, SWT.ALT);
        press(focus, SWT.ESC);
        release(focus, SWT.ESC);
        press(focus, SWT.ALT);
        MnemonicActivationTest.pressAlt(focus, 'f');
        release(focus, 'f');
        release(focus, SWT.ALT);

        assertThat(openRequests()).containsExactly(FlutterBridge.eventName(bar.getItem(0).getImpl(), "openMenu"));
        assertThat(activations()).hasSize(1);
    }

    @Test
    @DisplayName("Alt+F on a control whose Traverse listener rejects the mnemonic is delivered and opens File")
    void altLetterRejectedByTheFocusedControlOpensTheMenu() {
        Menu bar = menuBar();
        // As a CTabFolder with no tab carrying the letter does.
        focus.addListener(SWT.Traverse, e -> {
            if (e.detail == SWT.TRAVERSE_MNEMONIC)
                e.doit = false;
        });
        int[] keyDowns = {0};
        focus.addListener(SWT.KeyDown, e -> keyDowns[0]++);

        MnemonicActivationTest.pressAlt(focus, 'f');

        assertThat(keyDowns[0]).isEqualTo(1);
        assertThat(openRequests()).containsExactly(FlutterBridge.eventName(bar.getItem(0).getImpl(), "openMenu"));
    }

    @Test
    @DisplayName("an Alt+F a KeyDown listener vetoes does not open File")
    void vetoedAltLetterDoesNotOpenTheMenu() {
        menuBar();
        focus.addListener(SWT.Traverse, e -> {
            if (e.detail == SWT.TRAVERSE_MNEMONIC)
                e.doit = false;
        });
        focus.addListener(SWT.KeyDown, e -> e.doit = false);

        MnemonicActivationTest.pressAlt(focus, 'f');

        assertThat(openRequests()).isEmpty();
    }

    @Test
    @DisplayName("a shell without a menu bar has nothing to activate")
    void noMenuBarNoActivation() {
        press(focus, SWT.F10);
        press(focus, SWT.ALT);
        release(focus, SWT.ALT);

        assertThat(activations()).isEmpty();
    }

    @Test
    @DisplayName("a disabled menu bar is not activated")
    void disabledMenuBarIsNotActivated() {
        menuBar().setEnabled(false);

        press(focus, SWT.F10);

        assertThat(activations()).isEmpty();
    }

    @Test
    @DisplayName("a lone Alt whose KeyUp a listener vetoes does not activate the menu bar")
    void vetoedAltReleaseDoesNotActivate() {
        menuBar();
        focus.addListener(SWT.KeyUp, e -> e.doit = false);

        press(focus, SWT.ALT);
        release(focus, SWT.ALT);

        assertThat(activations()).isEmpty();
    }

    @Test
    @DisplayName("a mouse press between Alt's press and release keeps the menu bar inactive")
    void mousePressEndsTheLoneAlt() {
        menuBar();

        press(focus, SWT.ALT);
        Event click = new Event();
        click.button = 1;
        ControlHelper.pressed((DartControl) focus.getImpl(), click);
        release(focus, SWT.ALT);

        assertThat(activations()).isEmpty();
    }

    @Test
    @DisplayName("a KeyUp listener that disposes the focused control ends the release there")
    void releaseDisposingTheFocusedControl() {
        menuBar();
        focus.addListener(SWT.KeyUp, e -> focus.dispose());

        press(focus, SWT.ALT);
        release(focus, SWT.ALT);

        assertThat(focus.isDisposed()).isTrue();
        assertThat(activations()).isEmpty();
    }

    @Test
    @DisplayName("a menu bar with no title is not activated")
    void emptyMenuBarIsNotActivated() {
        Menu bar = new Menu(shell, SWT.BAR);
        when(shell.getMenuBar()).thenReturn(bar);

        press(focus, SWT.F10);
        press(focus, SWT.ALT);
        release(focus, SWT.ALT);

        assertThat(activations()).isEmpty();
    }

    private Menu menuBar() {
        Menu bar = new Menu(shell, SWT.BAR);
        when(shell.getMenuBar()).thenReturn(bar);
        MenuItem file = new MenuItem(bar, SWT.CASCADE);
        file.setText("&File");
        file.setMenu(new Menu(shell, SWT.DROP_DOWN));
        return bar;
    }

    private List<String> activations() {
        return bridge.comm.sent.stream().map(f -> f.event).filter(e -> e.endsWith("/activate")).collect(Collectors.toList());
    }

    private List<String> openRequests() {
        return bridge.comm.sent.stream().map(f -> f.event).filter(e -> e.endsWith("/openMenu")).collect(Collectors.toList());
    }

    private static void release(Control control, int keyCode) {
        Event key = new Event();
        key.keyCode = keyCode;
        ControlHelper.routeKeyUp((DartControl) control.getImpl(), key);
    }
}
