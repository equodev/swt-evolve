package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The order of the events a dialog's keys and a click into another shell fire, as Win32 and GTK
 * fire them: a focus change the keyboard makes announces the focus before the activation, a press in
 * an inactive shell moves the activation before its MouseDown, a closed dialog's parent is activated
 * before it is focused, and a mnemonic is offered to every control of the shell in turn.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*ShellInputOrderFlutterTest'</pre>
 */
@Tag("flutter-it")
class ShellInputOrderFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell main;
    private Shell dialog;
    private Text field;
    private Button ok;
    private Button apply;
    private final Map<Widget, String> names = new IdentityHashMap<>();
    private final List<String> log = new ArrayList<>();

    @BeforeEach
    void openMainAndDialog() {
        web = install(TestWebBridge::new);
        main = named(new Shell(display), "Main");
        main.setSize(400, 300);
        main.open();
        dialog = named(new Shell(main, SWT.DIALOG_TRIM), "Dialog");
        named(new Label(dialog, SWT.NONE), "Label").setText("&Name:");
        field = named(new Text(dialog, SWT.BORDER), "Field");
        ok = named(new Button(dialog, SWT.PUSH), "Ok");
        ok.setText("OK");
        apply = named(new Button(dialog, SWT.PUSH), "Apply");
        apply.setText("&Apply");
        dialog.setBounds(20, 20, 250, 120);
        dialog.open();
        clientReports(field, "Focus/FocusIn");
        assertThat(display.getFocusControl()).as("precondition: the field holds the focus").isSameAs(field);
        int[] types = {SWT.Activate, SWT.Deactivate, SWT.FocusIn, SWT.FocusOut, SWT.MouseDown, SWT.Traverse, SWT.Selection};
        String[] typeNames = {"Activate", "Deactivate", "FocusIn", "FocusOut", "MouseDown", "Traverse", "Selection"};
        for (int i = 0; i < types.length; i++) {
            String type = typeNames[i];
            display.addFilter(types[i], e -> {
                String name = names.get(e.widget);
                if (name == null) return;
                log.add(name + "." + type + (e.type == SWT.Traverse ? "(" + e.doit + ")" : ""));
            });
        }
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void tabFocusIsAnnouncedBeforeActivation() {
        pressKey(field, SWT.TAB, '\t', 0);
        log.clear();
        // The client moves its focus once it has the Tab's verdict, and reports where it went.
        clientReports(ok, "Focus/FocusIn");

        assertThat(log).containsExactly("Field.FocusOut", "Ok.FocusIn", "Field.Deactivate", "Ok.Activate");
    }

    @Test
    void clientFocusWithoutTabIsActivatedFirst() {
        // A press Java saw no MouseDown for, as on a Table's header, reaches it as the focus alone.
        clientReports(ok, "Focus/FocusIn");

        assertThat(log).containsExactly("Field.Deactivate", "Ok.Activate", "Field.FocusOut", "Ok.FocusIn");
    }

    @Test
    void pressInInactiveShellActivatesItBeforeMouseDown() {
        Event press = new Event();
        press.button = 1;
        press.x = 330;
        press.y = 30;

        clientReports(main, "Mouse/MouseDown", press);

        assertThat(log).containsSubsequence("Dialog.Deactivate", "Main.Activate", "Main.MouseDown");
        assertThat(log.indexOf("Field.FocusOut")).as("the dialog's focus goes before the press").isLessThan(log.indexOf("Main.MouseDown"));
        assertThat(display.getActiveShell()).isSameAs(main);
    }

    @Test
    void pressOutsidePopupHidingOnDeactivateActivatesOnce() {
        dialog.dispose();
        Shell popup = named(new Shell(main, SWT.ON_TOP | SWT.NO_TRIM), "Popup");
        named(new Composite(popup, SWT.NONE), "Pad").setBounds(0, 0, 140, 60);
        popup.setBounds(40, 40, 140, 60);
        popup.open();
        popup.addListener(SWT.Deactivate, e -> popup.setVisible(false));
        drain();
        assertThat(display.getActiveShell()).as("precondition: the popup is active").isSameAs(popup);
        log.clear();
        Event press = new Event();
        press.button = 1;

        clientReports(main, "Mouse/MouseDown", press);

        assertThat(log).containsSubsequence("Popup.Deactivate", "Main.Activate", "Main.MouseDown");
        assertThat(log).containsOnlyOnce("Popup.Deactivate", "Main.Activate");
        assertThat(popup.isVisible()).isFalse();
        assertThat(display.getActiveShell()).isSameAs(main);
    }

    @Test
    void escapeClosingDialogActivatesParentBeforeFocusingIt() {
        pressKey(field, SWT.ESC, (char) SWT.ESC, 0);

        assertThat(dialog.isDisposed()).isTrue();
        assertThat(log).containsSubsequence("Main.Activate", "Main.FocusIn");
        assertThat(log.indexOf("Main.FocusIn")).isGreaterThan(log.indexOf("Main.Activate"));
    }

    @Test
    void mnemonicIsOfferedToEachControlUntilOneTakesIt() {
        pressKey(field, 'a', 'a', SWT.ALT);

        assertThat(log).containsSubsequence("Field.Traverse(false)", "Dialog.Traverse(false)", "Label.Traverse(false)",
                "Ok.Traverse(false)", "Apply.Traverse(true)", "Apply.Selection");
        assertThat(log).doesNotContain("Field.Traverse(true)");
        assertThat(display.getFocusControl()).isSameAs(apply);
    }

    @Test
    void mnemonicTheFocusControlLetsThroughButDoesNotHoldGoesToTheShell() {
        field.addListener(SWT.Traverse, e -> e.doit = true);

        pressKey(field, 'a', 'a', SWT.ALT);

        assertThat(log).containsSubsequence("Field.Traverse(false)", "Dialog.Traverse(false)", "Apply.Traverse(true)", "Apply.Selection");
        assertThat(display.getFocusControl()).isSameAs(apply);
    }

    @Test
    void tabWithNowhereElseToGoLeavesNoTraversalWaiting() {
        ok.dispose();
        apply.dispose();
        for (Control child : dialog.getChildren()) if (child instanceof Button) child.dispose();

        pressKey(field, SWT.TAB, '\t', 0);

        assertThat(web.takeKeyboardTraversal()).as("a later FocusIn is no Tab's").isFalse();
    }

    @Test
    void controlItsActivateListenerDisposesTakesNoFocus() {
        ok.addListener(SWT.Activate, e -> ok.dispose());

        clientReports(ok, "Focus/FocusIn");

        assertThat(ok.isDisposed()).isTrue();
        assertThat(log).doesNotContain("Ok.FocusIn");
        assertThat(display.getFocusControl()).isNotNull();
    }

    @Test
    void tabFromTheShellItselfWaitsForTheFocusItMoves() {
        pressKey(dialog, SWT.TAB, '\t', 0);

        assertThat(web.takeKeyboardTraversal()).as("the Tab lands on a control of the shell").isTrue();
    }

    @Test
    void labelMnemonicFocusesTheControlAfterIt() {
        clientReports(ok, "Focus/FocusIn");
        log.clear();

        pressKey(ok, 'n', 'n', SWT.ALT);

        assertThat(log).containsSubsequence("Ok.Traverse(false)", "Dialog.Traverse(false)", "Label.Traverse(true)", "Field.FocusIn");
        assertThat(display.getFocusControl()).isSameAs(field);
    }

    // ---- harness ----------------------------------------------------------------------------------

    private <W extends Widget> W named(W widget, String name) {
        names.put(widget, name);
        return widget;
    }

    private void pressKey(Control control, int keyCode, char character, int stateMask) {
        Event key = new Event();
        key.keyCode = keyCode;
        key.character = character;
        key.stateMask = stateMask;
        ControlHelper.routeKeyDown((DartControl) control.getImpl(), key);
        drain();
    }

    private void clientReports(Widget widget, String suffix) {
        clientReports(widget, suffix, new Event());
    }

    private void clientReports(Widget widget, String suffix, Event event) {
        web.comm.fireContaining("/" + widget.hashCode() + "/" + suffix, event);
        drain();
    }

    private void drain() {
        while (display.readAndDispatch()) {
        }
    }

    /** See {@code ControlFocusRequestFlutterTest.install} — a Display whose bridge is the test bridge. */
    private <B extends DisplayBridge> B install(java.util.function.Function<DartDisplay, B> factory) {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }
}
