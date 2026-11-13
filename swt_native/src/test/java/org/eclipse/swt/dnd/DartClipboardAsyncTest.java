package org.eclipse.swt.dnd;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.widgets.DartDisplay;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** The asynchronous read the Clipboard API answers itself, from SWT 3.132 on. */
@ExtendWith(Mocks.class)
class DartClipboardAsyncTest {

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
        ((DartDisplay) Mocks.display().getImpl()).clipboards = null;
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void theImplementationAnswersTheShortAsyncOverloadItself() throws Exception {
        Clipboard clipboard = new Clipboard(Mocks.display());
        try {
            clipboard.getImpl().setContents(new Object[] {"async"}, new Transfer[] {TextTransfer.getInstance()});
            assertThat(clipboard.getImpl().getContentsAsync(TextTransfer.getInstance()).get(5, TimeUnit.SECONDS)).isEqualTo("async");
        } finally {
            clipboard.dispose();
        }
    }
}
