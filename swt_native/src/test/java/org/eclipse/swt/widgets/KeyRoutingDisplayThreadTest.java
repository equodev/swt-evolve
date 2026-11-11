package org.eclipse.swt.widgets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The key-routing fixture holds whichever thread built the JVM's shared display mock. */
class KeyRoutingDisplayThreadTest extends KeyRoutingTestBase {

    private static Thread builtOn;

    @BeforeAll
    static void displayBuiltOnAnotherThread() {
        DartDisplay impl = (DartDisplay) Mocks.display().getImpl();
        builtOn = impl.thread;
        impl.thread = new Thread();
    }

    @AfterAll
    static void restoreDisplayThread() {
        ((DartDisplay) Mocks.display().getImpl()).thread = builtOn;
    }

    @Test
    @DisplayName("the client's platform reaches the display when another thread built the shared mock")
    void clientPlatformIsKept() {
        Display display = shell().getDisplay();

        DisplayHelper.clientReady(display, "windows");

        assertThat(DisplayHelper.keyboardPlatform(display)).isEqualTo("win32");
    }
}
