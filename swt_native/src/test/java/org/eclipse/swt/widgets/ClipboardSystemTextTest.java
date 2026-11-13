package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The text of a Clipboard is the system clipboard's as well, through the Display's bridge: a write puts the
 * text there (or empties it when there is none), a clear empties it, and a text read takes what another
 * application copied.
 */
class ClipboardSystemTextTest {

    private Display display;
    private SystemClipboardBridge bridge;
    private Clipboard clipboard;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dartDisplay = (DartDisplay) display.getImpl();
        bridge = new SystemClipboardBridge(dartDisplay);
        dartDisplay.setBridge(bridge);
        clipboard = new Clipboard(display);
        clipboard.clearContents();
        bridge.written.clear();
    }

    @AfterEach
    void tearDown() {
        if (!clipboard.isDisposed() && !display.isDisposed())
            clipboard.dispose();
        if (!display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void set(Object datum, Transfer transfer) {
        clipboard.setContents(new Object[] {datum}, new Transfer[] {transfer});
    }

    @Test
    void aWriteOfTextPutsItOnTheSystemClipboard() {
        clipboard.setContents(new Object[] {"{\\rtf1 rich}", "plain"}, new Transfer[] {RTFTransfer.getInstance(), TextTransfer.getInstance()});
        assertThat(bridge.written).containsExactly("plain");
    }

    @Test
    void aWriteWithoutTextEmptiesTheSystemClipboard() {
        set("{\\rtf1 rich}", RTFTransfer.getInstance());
        assertThat(bridge.written).containsExactly("");
    }

    @Test
    void clearingEmptiesTheSystemClipboard() {
        set("text", TextTransfer.getInstance());
        clipboard.clearContents();
        assertThat(bridge.written).containsExactly("text", "");
    }

    @Test
    void textAnotherApplicationCopiedIsWhatATextReadReturns() {
        set("{\\rtf1 mine}", RTFTransfer.getInstance());
        bridge.systemText = "from another application";
        assertThat(clipboard.getContents(TextTransfer.getInstance())).isEqualTo("from another application");
        assertThat(clipboard.getContents(RTFTransfer.getInstance())).isNull();
    }

    @Test
    void aSystemClipboardThatCannotBeReadChangesNothing() {
        set("mine", TextTransfer.getInstance());
        bridge.systemText = null;
        assertThat(clipboard.getContents(TextTransfer.getInstance())).isEqualTo("mine");
        assertThat(bridge.reads).isEqualTo(1);
    }

    @Test
    void onlyATextReadOfTheClipboardAsksTheSystem() {
        set("mine", TextTransfer.getInstance());
        bridge.systemText = "theirs";
        clipboard.getContents(RTFTransfer.getInstance());
        clipboard.getContents(TextTransfer.getInstance(), DND.SELECTION_CLIPBOARD);
        clipboard.getAvailableTypes();
        assertThat(bridge.reads).isZero();
    }

    @Test
    void aClipboardOfADisposedDisplayIsRefused() {
        display.dispose();
        assertThatThrownBy(() -> clipboard.getContents(TextTransfer.getInstance()))
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_DEVICE_DISPOSED));
    }

    /** A bridge whose system clipboard is a field: what was written, and what another application copied. */
    private static final class SystemClipboardBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final List<String> written = new ArrayList<>();
        String systemText;
        int reads;

        SystemClipboardBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
        }

        @Override
        public void writeClipboardText(String text) {
            written.add(text);
        }

        @Override
        public String readClipboardText() {
            reads++;
            return systemText;
        }
    }
}
