package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Text and a Combo copy to, and paste from, the clipboard the Clipboard API reads and writes, not a
 * private one: what an application puts there is what paste inserts, and what copy takes is what the
 * application reads back.
 */
@ExtendWith(Mocks.class)
class TextClipboardTest {

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
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static void put(Display display, String text) {
        Clipboard clipboard = new Clipboard(display);
        try {
            clipboard.setContents(new Object[] {text}, new Transfer[] {TextTransfer.getInstance()});
        } finally {
            clipboard.dispose();
        }
    }

    private static String read(Display display) {
        Clipboard clipboard = new Clipboard(display);
        try {
            return (String) clipboard.getContents(TextTransfer.getInstance());
        } finally {
            clipboard.dispose();
        }
    }

    @Test
    void copyOfATextCanBeReadThroughTheClipboardApi() {
        for (int style : new int[] {SWT.SINGLE, SWT.MULTI}) {
            Shell shell = Mocks.shell();
            Text text = new Text(shell, style);
            text.setText("copy me");
            text.setSelection(0, 4);
            text.copy();
            assertThat(read(shell.getDisplay())).as("style " + style).isEqualTo("copy");
        }
    }

    @Test
    void cutOfATextCanBeReadThroughTheClipboardApi() {
        Shell shell = Mocks.shell();
        Text text = new Text(shell, SWT.SINGLE);
        text.setText("cut me");
        text.setSelection(3, 6);
        text.cut();
        assertThat(text.getText()).isEqualTo("cut");
        assertThat(read(shell.getDisplay())).isEqualTo(" me");
    }

    @Test
    void pasteInsertsWhatTheClipboardApiWrote() {
        for (int style : new int[] {SWT.SINGLE, SWT.MULTI}) {
            Shell shell = Mocks.shell();
            Text text = new Text(shell, style);
            text.setText("ab");
            put(shell.getDisplay(), "from outside");
            text.setSelection(1);
            text.paste();
            assertThat(text.getText()).as("style " + style).isEqualTo("afrom outsideb");
        }
    }

    @Test
    void pasteOfAClipboardWithoutTextInsertsNothing() {
        Shell shell = Mocks.shell();
        Text text = new Text(shell, SWT.SINGLE);
        text.setText("keep");
        Clipboard clipboard = new Clipboard(shell.getDisplay());
        try {
            clipboard.setContents(new Object[] {"{\\rtf1 rich}"}, new Transfer[] {RTFTransfer.getInstance()});
        } finally {
            clipboard.dispose();
        }
        text.setSelection(1, 2);

        text.paste();

        assertThat(text.getText()).isEqualTo("keep");
        assertThat(text.getSelection()).isEqualTo(new org.eclipse.swt.graphics.Point(1, 2));
    }

    @Test
    void aComboCopiesAndPastesThroughTheSameClipboard() {
        Shell shell = Mocks.shell();
        Combo combo = new Combo(shell, SWT.DROP_DOWN);
        combo.setText("combo text");
        combo.setSelection(new org.eclipse.swt.graphics.Point(0, 5));
        combo.copy();
        assertThat(read(shell.getDisplay())).isEqualTo("combo");
        put(shell.getDisplay(), "X");
        combo.setSelection(new org.eclipse.swt.graphics.Point(0, 0));
        combo.paste();
        assertThat(combo.getText()).startsWith("X");
    }
}
