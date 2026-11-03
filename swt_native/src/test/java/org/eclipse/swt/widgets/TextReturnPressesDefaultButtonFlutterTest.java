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
 * Return in a single-line field runs the platforms' Return traversal: up from the field to its
 * shell, which presses the default button. A dialog's search field relies on it to open what it
 * found (the default button), and the field itself has no Traverse handling of its own.
 */
@Tag("flutter-it")
class TextReturnPressesDefaultButtonFlutterTest {

    private RecordingBridge bridge;
    private Display display;

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
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    private void pump() {
        for (int i = 0; i < 100 && display.readAndDispatch(); i++) {
        }
    }

    /** What the client sends when Return is pressed in the field. */
    private void pressReturnIn(Text field) {
        bridge.comm.fireContaining("Text/" + field.hashCode() + "/Selection/DefaultSelection", new Event());
        pump();
    }

    private Shell dialogOver(Shell parent) {
        Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setSize(300, 200);
        return dialog;
    }

    @Test
    void returnInADialogFieldPressesTheDefaultButton() {
        Shell main = new Shell(display);
        main.open();
        Shell dialog = dialogOver(main);
        Text pattern = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        Button open = new Button(dialog, SWT.PUSH);
        open.setText("Open");
        dialog.setDefaultButton(open);
        int[] pressed = {0};
        int[] keyDowns = {0};
        int[] defaultSelections = {0};
        open.addListener(SWT.Selection, e -> pressed[0]++);
        pattern.addListener(SWT.KeyDown, e -> keyDowns[0]++);
        pattern.addListener(SWT.DefaultSelection, e -> defaultSelections[0]++);
        dialog.open();
        pattern.setFocus();
        pump();

        pressReturnIn(pattern);

        assertThat(pressed[0]).as("Return presses the shell's default button").isEqualTo(1);
        assertThat(keyDowns[0]).as("the traversal consumed the key, so the field gets no KeyDown").isZero();
        assertThat(defaultSelections[0])
                .as("the traversal consumed the key, so the field reports no DefaultSelection")
                .isZero();
    }

    @Test
    void returnWithNoDefaultButtonIsTheFieldsDefaultSelection() {
        Shell main = new Shell(display);
        main.open();
        Shell dialog = dialogOver(main);
        Text pattern = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        int[] defaultSelections = {0};
        pattern.addListener(SWT.DefaultSelection, e -> defaultSelections[0]++);
        dialog.open();
        pattern.setFocus();
        pump();

        pressReturnIn(pattern);

        assertThat(defaultSelections[0]).as("nothing traversed, so the field gets the key").isEqualTo(1);
    }

    /** The client forwards the keystroke to the key route, then submits the field on it. */
    private void typeReturnIn(Text field) {
        Event key = new Event();
        key.keyCode = SWT.CR;
        key.character = '\r';
        ControlHelper.routeKeyDown((DartControl) field.getImpl(), key);
        pressReturnIn(field);
    }

    @Test
    void returnTheKeyRouteRanPressesTheDefaultButtonOnce() {
        Shell main = new Shell(display);
        main.open();
        Shell dialog = dialogOver(main);
        Text pattern = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        Button open = new Button(dialog, SWT.PUSH);
        dialog.setDefaultButton(open);
        int[] pressed = {0};
        int[] keyDowns = {0};
        int[] defaultSelections = {0};
        open.addListener(SWT.Selection, e -> pressed[0]++);
        pattern.addListener(SWT.KeyDown, e -> keyDowns[0]++);
        pattern.addListener(SWT.DefaultSelection, e -> defaultSelections[0]++);
        dialog.open();
        pattern.setFocus();
        pump();

        typeReturnIn(pattern);

        assertThat(pressed[0]).as("one keystroke presses the default button once").isEqualTo(1);
        assertThat(keyDowns[0]).as("the traversal consumed the key").isZero();
        assertThat(defaultSelections[0]).as("the traversal consumed the key").isZero();
    }

    @Test
    void returnTheKeyRouteRanWithNoDefaultButtonReachesTheFieldOnce() {
        Shell main = new Shell(display);
        main.open();
        Shell dialog = dialogOver(main);
        Text pattern = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        int[] traverses = {0};
        int[] keyDowns = {0};
        int[] defaultSelections = {0};
        pattern.addListener(SWT.Traverse, e -> traverses[0]++);
        pattern.addListener(SWT.KeyDown, e -> keyDowns[0]++);
        pattern.addListener(SWT.DefaultSelection, e -> defaultSelections[0]++);
        dialog.open();
        pattern.setFocus();
        pump();

        typeReturnIn(pattern);

        assertThat(traverses[0]).as("one Traverse for the keystroke").isEqualTo(1);
        assertThat(keyDowns[0]).as("one KeyDown for the keystroke").isEqualTo(1);
        assertThat(defaultSelections[0]).as("one DefaultSelection for the keystroke").isEqualTo(1);
    }

    @Test
    void shiftReturnTheKeyRouteRanReachesTheFieldOnceWithItsModifiers() {
        Shell shell = new Shell(display);
        Text field = new Text(shell, SWT.SINGLE | SWT.BORDER);
        List<Event> keyDowns = new ArrayList<>();
        field.addListener(SWT.KeyDown, e -> {
            if (e.keyCode == SWT.CR)
                keyDowns.add(e);
        });
        shell.open();
        field.setFocus();
        pump();

        Event key = new Event();
        key.keyCode = SWT.CR;
        key.character = '\r';
        key.stateMask = SWT.SHIFT;
        ControlHelper.routeKeyDown((DartControl) field.getImpl(), key);
        pressReturnIn(field);

        assertThat(keyDowns).as("one KeyDown for the keystroke").hasSize(1);
        assertThat(keyDowns.get(0).stateMask).as("the KeyDown carries the Shift held down").isEqualTo(SWT.SHIFT);
    }

    @Test
    void aKeyDownListenerVetoingReturnStopsTheDefaultSelection() {
        Shell main = new Shell(display);
        main.open();
        Shell dialog = dialogOver(main);
        Text pattern = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        int[] defaultSelections = {0};
        pattern.addListener(SWT.KeyDown, e -> e.doit = false);
        pattern.addListener(SWT.DefaultSelection, e -> defaultSelections[0]++);
        dialog.open();
        pattern.setFocus();
        pump();

        pressReturnIn(pattern);

        assertThat(defaultSelections[0]).as("a vetoed Return is no DefaultSelection").isZero();
    }

    @Test
    void aFieldTheReturnTraversalDisposesGetsNoKey() {
        Shell main = new Shell(display);
        main.open();
        Shell dialog = dialogOver(main);
        Composite form = new Composite(dialog, SWT.NONE);
        Text pattern = new Text(form, SWT.SINGLE | SWT.BORDER);
        form.addListener(SWT.Traverse, e -> {
            if (e.detail == SWT.TRAVERSE_RETURN)
                pattern.dispose();
        });
        List<Event> keysToPattern = new ArrayList<>();
        display.addFilter(SWT.KeyDown, e -> {
            if (e.widget == pattern)
                keysToPattern.add(e);
        });
        dialog.open();
        pattern.setFocus();
        pump();

        pressReturnIn(pattern);

        assertThat(pattern.isDisposed()).as("the form's Traverse listener disposed the field").isTrue();
        assertThat(keysToPattern).as("the traversal took nothing, but the field is gone: no KeyDown").isEmpty();
    }
}
