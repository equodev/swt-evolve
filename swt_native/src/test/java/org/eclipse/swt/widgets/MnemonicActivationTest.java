package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Alt+letter activates what carries that mnemonic in the focused control's shell. */
class MnemonicActivationTest extends KeyRoutingTestBase {

    @BeforeEach
    void windowsClient() {
        DisplayHelper.clientReady(shell().getDisplay(), "windows");
    }

    @Test
    @DisplayName("Alt+letter selects the CTabItem carrying it and notifies the folder")
    void altLetterSelectsCTabItem() {
        Shell shell = shell();
        Text focus = new Text(shell, SWT.BORDER);
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        new CTabItem(folder, SWT.NONE).setText("&General");
        CTabItem advanced = new CTabItem(folder, SWT.NONE);
        advanced.setText("&Advanced");
        List<Widget> selected = new ArrayList<>();
        folder.addListener(SWT.Selection, e -> selected.add(e.item));
        when(shell.getChildren()).thenReturn(new Control[] {focus, folder});

        pressAlt(focus, 'a');

        assertThat(folder.getSelection()).isSameAs(advanced);
        assertThat(selected).containsExactly(advanced);
    }

    @Test
    @DisplayName("Alt+letter no control claims asks the client to open the menu bar menu carrying it")
    void altLetterOpensMenuBarMenu() {
        RecordingBridge bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        Shell shell = shell();
        Text focus = new Text(shell, SWT.BORDER);
        when(shell.getChildren()).thenReturn(new Control[] {focus});
        MenuItem edit = menuBar(shell, "&File", "&Edit");

        pressAlt(focus, 'e');

        assertThat(sent(bridge)).containsExactly(FlutterBridge.eventName(edit.getImpl(), "openMenu"));
    }

    @Test
    @DisplayName("a control carrying the letter wins over the menu bar, as on Win32")
    void controlWinsOverMenuBar() {
        RecordingBridge bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        Shell shell = shell();
        Text focus = new Text(shell, SWT.BORDER);
        Button find = new Button(shell, SWT.PUSH);
        find.setText("&Find");
        List<Event> selections = new ArrayList<>();
        find.addListener(SWT.Selection, selections::add);
        when(shell.getChildren()).thenReturn(new Control[] {focus, find});
        menuBar(shell, "&File");

        pressAlt(focus, 'f');

        assertThat(selections).hasSize(1);
        assertThat(sent(bridge)).isEmpty();
    }

    @Test
    @DisplayName("a disabled menu bar title does not open")
    void disabledMenuBarTitleDoesNotOpen() {
        RecordingBridge bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        Shell shell = shell();
        Text focus = new Text(shell, SWT.BORDER);
        when(shell.getChildren()).thenReturn(new Control[] {focus});
        menuBar(shell, "&File").setEnabled(false);

        pressAlt(focus, 'f');

        assertThat(sent(bridge)).isEmpty();
    }

    /** A menu bar on the shell with a drop-down under each title; returns the last title. */
    private static MenuItem menuBar(Shell shell, String... titles) {
        Menu bar = new Menu(shell, SWT.BAR);
        when(shell.getMenuBar()).thenReturn(bar);
        MenuItem item = null;
        for (String title : titles) {
            item = new MenuItem(bar, SWT.CASCADE);
            item.setText(title);
            item.setMenu(new Menu(shell, SWT.DROP_DOWN));
        }
        return item;
    }

    private static List<String> sent(RecordingBridge bridge) {
        return bridge.comm.sent.stream().map(f -> f.event).filter(e -> e.endsWith("/openMenu")).collect(Collectors.toList());
    }

    /** Alt+{@code letter} routed into the control, as the client delivers it. */
    static void pressAlt(Control control, char letter) {
        Event key = new Event();
        key.keyCode = letter;
        key.character = letter;
        key.stateMask = SWT.ALT;
        ControlHelper.routeKeyDown((DartControl) control.getImpl(), key);
    }
}
