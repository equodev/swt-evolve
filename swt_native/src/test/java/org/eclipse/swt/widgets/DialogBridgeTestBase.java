package org.eclipse.swt.widgets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;

/**
 * The mock {@link DisplayBridge} a dialog test drives its client through. {@link DartMocks}' display outlives
 * every test in the fork, so whatever a test puts on its bridge is put back when the test ends.
 */
abstract class DialogBridgeTestBase {

    private DartDisplay bridged;
    private DisplayBridge previousBridge;

    /** Remembers {@code display}'s bridge, so {@link #restoreTheSharedBridge()} can put it back. */
    void rememberBridge(Display display) {
        if (bridged != null)
            return;
        bridged = (DartDisplay) display.getImpl();
        previousBridge = bridged.displayBridge;
    }

    /** Puts {@code bridge} on {@code display} for the rest of this test. */
    void installBridge(Display display, DisplayBridge bridge) {
        rememberBridge(display);
        ((DartDisplay) display.getImpl()).displayBridge = bridge;
    }

    /** Stands in for Flutter answering the dialog's close channel with {@code closePayload}. */
    Shell shellAnswering(String closePayload) {
        Shell shell = DartMocks.dartShell();
        DisplayBridge bridge = mock(DisplayBridge.class);
        doAnswer(invocation -> {
            Consumer<String> handler = invocation.getArgument(3);
            handler.accept(closePayload);
            return null;
        }).when(bridge).onChannel(anyInt(), anyLong(), anyInt(), any());
        installBridge(shell.getDisplay(), bridge);
        return shell;
    }

    @AfterEach
    void restoreTheSharedBridge() {
        if (bridged == null)
            return;
        bridged.displayBridge = previousBridge;
        bridged = null;
        previousBridge = null;
    }
}
