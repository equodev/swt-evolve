package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An arrow key on a radio button moves the focus to the previous or next radio among its siblings,
 * as {@code Control.traverseItem} does on every native once the arrow's Traverse keeps doit=true.
 * The selection stays where it was, as on GTK and Cocoa.
 */
@Tag("flutter-it")
class RadioArrowFocusFlutterTest {

    private Display display;
    private Shell shell;
    private Button first;
    private Button second;
    private Button third;

    @BeforeEach
    void setUp() {
        display = TestWebBridge.newDisplay().display;
        shell = new Shell(display);
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        first = new Button(shell, SWT.RADIO);
        second = new Button(shell, SWT.RADIO);
        third = new Button(shell, SWT.RADIO);
        first.setSelection(true);
        shell.setSize(200, 120);
        shell.open();
        first.setFocus();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private static void press(Control control, int keyCode) {
        Event key = new Event();
        key.keyCode = keyCode;
        ControlHelper.routeKeyDown((DartControl) control.getImpl(), key);
    }

    @Test
    @DisplayName("Down and Right move the focus to the next radio, Up and Left back, wrapping at the ends")
    void arrowsMoveTheFocusBetweenTheRadios() {
        assertThat(display.getFocusControl()).as("precondition").isSameAs(first);

        press(first, SWT.ARROW_DOWN);
        assertThat(display.getFocusControl()).isSameAs(second);

        press(second, SWT.ARROW_RIGHT);
        assertThat(display.getFocusControl()).isSameAs(third);

        press(third, SWT.ARROW_DOWN);
        assertThat(display.getFocusControl()).isSameAs(first);

        press(first, SWT.ARROW_UP);
        assertThat(display.getFocusControl()).isSameAs(third);

        press(third, SWT.ARROW_LEFT);
        assertThat(display.getFocusControl()).isSameAs(second);
    }

    @Test
    @DisplayName("an arrow that moves the focus is not delivered as a KeyDown and leaves the selection")
    void anArrowThatMovesTheFocusIsConsumed() {
        java.util.List<String> keys = new ArrayList<>();
        first.addListener(SWT.KeyDown, e -> keys.add("KeyDown"));

        press(first, SWT.ARROW_DOWN);

        assertThat(keys).isEmpty();
        assertThat(first.getSelection()).isTrue();
        assertThat(second.getSelection()).isFalse();
    }
}
