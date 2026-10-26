package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/** A Text pastes what the Clipboard API wrote, through _paste(boolean), which SWT has from 3.114.100 on. */
@ExtendWith(Mocks.class)
class TextClipboardPasteTest {

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
}
