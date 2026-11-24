package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.RecordingDisplayBridge.drainEventLoop;

/**
 * A press in an ON_TOP popup the editor showed without activating it. Natively the press activates
 * the popup before the focus moves into it (Cocoa makes the panel key, Win32 answers WM_MOUSEACTIVATE
 * with an activation), so the editor's FocusOut already sees the popup as the active shell. JFace's
 * hover closers rely on that: they close the popup on the editor's FocusOut unless the popup is
 * active, as {@code MultipleHyperlinkPresenter}'s list does.
 */
@Tag("native-unit")
class OnTopPopupPressActivationNativeTest {

    private Display display;
    private RecordingDisplayBridge bridge;
    private final List<String> heard = new ArrayList<>();

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a press in an ON_TOP tool popup activates it before the editor loses the focus")
    void aPressActivatesTheOnTopPopupBeforeTheFocusMoves() {
        Shell popup = editorWithPopup(SWT.ON_TOP | SWT.TOOL);
        Table table = table(popup);

        press(table);

        assertThat(popup.isDisposed()).as("the popup survives the press").isFalse();
        assertThat(heard).containsExactly(
                "Deactivate editor", "Activate popup", "FocusOut editor active=popup", "FocusIn table", "MouseDown table");
        assertThat(display.getActiveShell()).isSameAs(popup);
    }

    @Test
    @DisplayName("a press in an ON_TOP NO_FOCUS popup leaves the editor active and focused")
    void aPressInANoFocusPopupDoesNotActivateIt() {
        Shell popup = editorWithPopup(SWT.ON_TOP | SWT.TOOL | SWT.NO_FOCUS);
        Canvas canvas = new Canvas(popup, SWT.NO_FOCUS);
        canvas.setBounds(0, 0, 200, 100);
        canvas.addListener(SWT.MouseDown, e -> heard.add("MouseDown canvas"));

        press(canvas);

        assertThat(heard).containsExactly("MouseDown canvas");
        assertThat(display.getActiveShell()).isNotSameAs(popup);
    }

    @Test
    @DisplayName("a press back in the editor makes the editor's shell active before the popup's table loses the focus")
    void aPressBackInTheEditorActivatesItBeforeTheTableLosesTheFocus() {
        Shell popup = editorWithPopup(SWT.ON_TOP | SWT.TOOL);
        Table table = table(popup);
        Shell editorShell = (Shell) popup.getParent();
        Control editor = editorShell.getChildren()[0];
        press(table);
        heard.clear();
        table.addListener(SWT.FocusOut, e -> {
            Shell active = display.getActiveShell();
            heard.add("FocusOut table active=" + (active == editorShell ? "editor" : active == popup ? "popup" : active));
        });

        press(editor);

        assertThat(heard).contains("FocusOut table active=editor");
        assertThat(display.getActiveShell()).isSameAs(editorShell);
        assertThat(display.getFocusControl()).isSameAs(editor);
    }

    @Test
    @DisplayName("a Tab within one shell keeps that shell active during the FocusOut")
    void aTabWithinOneShellKeepsItActiveDuringTheFocusOut() {
        bridge = RecordingDisplayBridge.install(RecordingDisplayBridge::new);
        display = Display.getCurrent();
        Shell shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setBounds(0, 0, 400, 200);
        Text first = new Text(shell, SWT.SINGLE);
        first.setBounds(0, 0, 200, 30);
        Text second = new Text(shell, SWT.SINGLE);
        second.setBounds(0, 40, 200, 30);
        shell.open();
        first.setFocus();
        drainEventLoop(display);
        first.addListener(SWT.FocusOut, e -> heard.add("FocusOut first active=" + (display.getActiveShell() == shell)));

        first.traverse(SWT.TRAVERSE_TAB_NEXT);
        drainEventLoop(display);

        assertThat(heard).containsExactly("FocusOut first active=true");
        assertThat(display.getFocusControl()).isSameAs(second);
    }

    @Test
    @DisplayName("a press in a two-shell palette's list moves the activation into it and the palette stays open")
    void aPressInAPaletteListKeepsThePaletteOpen() {
        bridge = RecordingDisplayBridge.install(RecordingDisplayBridge::new);
        display = Display.getCurrent();
        Shell workbench = new Shell(display, SWT.SHELL_TRIM);
        workbench.setBounds(0, 0, 800, 600);
        workbench.open();
        drainEventLoop(display);

        Shell fieldShell = new Shell(workbench, SWT.ON_TOP);
        fieldShell.setBounds(100, 50, 400, 30);
        Text field = new Text(fieldShell, SWT.SINGLE);
        field.setBounds(0, 0, 400, 30);
        fieldShell.open();
        field.setFocus();
        drainEventLoop(display);
        Shell listShell = new Shell(workbench, SWT.ON_TOP);
        listShell.setBounds(100, 80, 400, 200);
        Table list = table(listShell);
        listShell.setVisible(true);
        drainEventLoop(display);
        assertThat(display.getActiveShell()).as("the palette's field is active").isSameAs(fieldShell);
        assertThat(display.getFocusControl()).isSameAs(field);

        // The palette closes once the activation leaves both of its shells, read after the switch
        // as native code must: Deactivate comes before the other shell's Activate.
        Listener closer = e -> display.asyncExec(() -> {
            Shell active = display.getActiveShell();
            if (active != fieldShell && active != listShell) {
                heard.add("palette closed");
                fieldShell.dispose();
                listShell.dispose();
            }
        });
        fieldShell.addListener(SWT.Deactivate, e -> heard.add("Deactivate field"));
        fieldShell.addListener(SWT.Deactivate, closer);
        listShell.addListener(SWT.Deactivate, closer);
        listShell.addListener(SWT.Activate, e -> heard.add("Activate list"));
        field.addListener(SWT.FocusOut, e -> heard.add("FocusOut field"));

        press(list);

        assertThat(heard).containsExactly(
                "Deactivate field", "Activate list", "FocusOut field", "FocusIn table", "MouseDown table");
        assertThat(fieldShell.isDisposed()).as("the field survives the press").isFalse();
        assertThat(listShell.isDisposed()).as("the list survives the press").isFalse();
        assertThat(display.getActiveShell()).isSameAs(listShell);
        assertThat(display.getFocusControl()).isSameAs(list);
    }

    /**
     * The editor's shell, focused on a StyledText, and the popup it showed with {@code setVisible}, which
     * JFace's closer disposes on the editor's FocusOut unless the popup is the active shell.
     */
    private Shell editorWithPopup(int popupStyle) {
        bridge = RecordingDisplayBridge.install(RecordingDisplayBridge::new);
        display = Display.getCurrent();
        Shell editorShell = new Shell(display, SWT.SHELL_TRIM);
        editorShell.setBounds(0, 0, 600, 400);
        StyledText editor = new StyledText(editorShell, SWT.MULTI);
        editor.setBounds(0, 0, 600, 400);
        editorShell.open();
        editor.setFocus();
        drainEventLoop(display);
        assertThat(display.getFocusControl()).as("the editor holds the focus").isSameAs(editor);

        Shell popup = new Shell(editorShell, popupStyle);
        popup.setBounds(40, 80, 200, 100);
        popup.setVisible(true);
        drainEventLoop(display);
        assertThat(display.getActiveShell()).as("showing the popup does not activate it").isSameAs(editorShell);

        editorShell.addListener(SWT.Deactivate, e -> heard.add("Deactivate editor"));
        popup.addListener(SWT.Activate, e -> heard.add("Activate popup"));
        editor.addListener(SWT.FocusOut, e -> {
            Shell active = display.getActiveShell();
            heard.add("FocusOut editor active=" + (active == popup ? "popup" : active == editorShell ? "editor" : active));
            if (active != popup) popup.dispose();
        });
        return popup;
    }

    private Table table(Shell popup) {
        Table table = new Table(popup, SWT.SINGLE);
        table.setBounds(0, 0, 200, 100);
        new TableItem(table, SWT.NONE).setText("Go to last line");
        table.addListener(SWT.FocusIn, e -> heard.add("FocusIn table"));
        table.addListener(SWT.MouseDown, e -> heard.add("MouseDown table"));
        return table;
    }

    private void press(Control control) {
        Event event = new Event();
        event.button = 1;
        event.x = 10;
        event.y = 10;
        bridge.comm.fireContaining(control.getClass().getSimpleName() + "/" + control.hashCode() + "/Mouse/MouseDown", event);
        drainEventLoop(display);
    }
}
