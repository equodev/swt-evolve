package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Disposing the control that holds the focus gives the focus to its nearest ancestor still alive,
 * which then receives FocusIn, as on Win32. Nothing reports the move from the client: the disposed
 * control is gone before its FocusOut would reach Java.
 */
@Tag("flutter-it")
class DisposedFocusControlFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void disposingTheFocusControlGivesTheFocusToItsShell() {
        install();
        Shell shell = openShell();
        Button button = new Button(shell, SWT.PUSH);
        assertThat(button.setFocus()).as("the button takes the focus").isTrue();
        List<String> events = record(shell, button, "Button");

        button.dispose();

        assertThat(events).as("the shell takes the focus back once the button is gone, with no event to the button")
                .containsExactly("Button.Dispose", "Shell.FocusIn");
        assertThat(display.getFocusControl()).as("the focus control afterwards").isSameAs(shell);
    }

    @Test
    void disposingTheFocusControlInACompositeGivesTheFocusToTheComposite() {
        install();
        Shell shell = openShell();
        Composite group = new Composite(shell, SWT.NONE);
        Button button = new Button(group, SWT.PUSH);
        new Button(group, SWT.PUSH);
        assertThat(button.setFocus()).as("the button takes the focus").isTrue();
        List<String> events = record(shell, button, "Button");
        group.addListener(SWT.FocusIn, e -> events.add("Composite.FocusIn"));

        button.dispose();

        assertThat(events).as("the Composite takes the focus, not its other button nor the shell")
                .containsExactly("Button.Dispose", "Composite.FocusIn");
        assertThat(display.getFocusControl()).as("the focus control afterwards").isSameAs(group);
    }

    @Test
    void disposingTheParentOfTheFocusControlGivesTheFocusToTheShell() {
        install();
        Shell shell = openShell();
        Composite group = new Composite(shell, SWT.NONE);
        Button button = new Button(group, SWT.PUSH);
        assertThat(button.setFocus()).as("the button takes the focus").isTrue();
        List<String> events = record(shell, button, "Button");

        group.dispose();

        assertThat(events).as("the shell takes the focus back once the button is gone")
                .containsExactly("Button.Dispose", "Shell.FocusIn");
        assertThat(display.getFocusControl()).as("the focus control afterwards").isSameAs(shell);
    }

    @Test
    void disposingTheShellOfTheFocusControlSendsTheShellNoFocusIn() {
        install();
        Shell shell = openShell();
        Button button = new Button(shell, SWT.PUSH);
        assertThat(button.setFocus()).as("the button takes the focus").isTrue();
        List<String> events = record(shell, button, "Button");

        shell.dispose();

        assertThat(events).as("a shell being disposed does not take the focus").doesNotContain("Shell.FocusIn");
    }

    @Test
    void aListenerDisposingTheControlTakingTheFocusGivesTheFocusToItsShell() {
        install();
        Shell shell = openShell();
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        for (int i = 0; i < 3; i++)
            new CTabItem(folder, SWT.NONE).setText("Tab " + i);
        folder.addListener(SWT.Selection, e -> folder.dispose());
        List<String> events = record(shell, folder, "CTabFolder");

        // CTabFolder's own FocusIn listener reaches its Accessible after the Selection listener
        // disposed it. Win32 and Cocoa throw the same; GTK does not.
        assertThatThrownBy(folder::setFocus).isInstanceOf(SWTException.class).hasMessageContaining("disposed");
        assertThat(folder.isDisposed()).as("the Selection listener disposed the folder").isTrue();
        assertThat(events).as("the shell takes the focus back once the folder is gone")
                .containsExactly("Shell.FocusOut", "CTabFolder.Dispose", "Shell.FocusIn");
        assertThat(display.getFocusControl()).as("the focus control afterwards").isSameAs(shell);
    }

    @Test
    void disposingAFocusedStyledTextSendsNothingForIt() {
        install();
        Shell shell = openShell();
        StyledText text = new StyledText(shell, SWT.MULTI);
        text.setText("one\ntwo");
        assertThat(text.setFocus()).as("the StyledText takes the focus").isTrue();
        FlutterBridge.update();
        java.io.ByteArrayOutputStream err = new java.io.ByteArrayOutputStream();
        java.io.PrintStream stderr = System.err;
        System.setErr(new java.io.PrintStream(err, true));
        try {
            text.dispose();
            FlutterBridge.update();
        } finally {
            System.setErr(stderr);
        }

        assertThat(err.toString()).as("what the handover to the shell logged").doesNotContain("Exception");
        assertThat(display.getFocusControl()).as("the focus control afterwards").isSameAs(shell);
    }

    /** The shell's focus events, and the control's Dispose with any FocusOut or Deactivate it gets. */
    private List<String> record(Shell shell, Control control, String name) {
        List<String> events = new ArrayList<>();
        shell.addListener(SWT.FocusIn, e -> events.add("Shell.FocusIn"));
        shell.addListener(SWT.FocusOut, e -> events.add("Shell.FocusOut"));
        control.addListener(SWT.FocusOut, e -> events.add(name + ".FocusOut"));
        control.addListener(SWT.Deactivate, e -> events.add(name + ".Deactivate"));
        control.addListener(SWT.Dispose, e -> events.add(name + ".Dispose"));
        return events;
    }

    private Shell openShell() {
        Shell shell = new Shell(display);
        shell.setSize(400, 300);
        shell.open();
        return shell;
    }

    /** A Display whose bridge is a {@link TestWebBridge}, as in {@code ControlFocusRequestFlutterTest}. */
    private void install() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestWebBridge bridge = new TestWebBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
    }
}
