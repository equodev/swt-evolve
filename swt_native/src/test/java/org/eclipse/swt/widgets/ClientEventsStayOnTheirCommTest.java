package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.AbstractBinaryCommService;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A widget's client events are dispatched by the id their channel names, and ids are looked up
 * across the JVM. A client reaches only the widgets of its own comm: one JVM serves several
 * sessions, and a client naming another session's widget must not reach it.
 */
@ExtendWith(Mocks.class)
class ClientEventsStayOnTheirCommTest {

    private final Feeder first = new Feeder();
    private final Feeder second = new Feeder();

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void aClientReachesOnlyTheWidgetsOfItsOwnComm() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        List<Runnable> handed = new ArrayList<>();
        doAnswer(inv -> handed.add(inv.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        FlutterBridge.set(new RecordingBridge(first));
        Button mine = new Button(shell, SWT.PUSH);
        FlutterBridge.set(new RecordingBridge(second));
        Button theirs = new Button(shell, SWT.PUSH);

        second.deliver("Button/" + mine.hashCode() + "/Selection/Selection");
        assertThat(handed).as("another comm's client names this comm's button").isEmpty();

        first.deliver("Button/" + mine.hashCode() + "/Selection/Selection");
        second.deliver("Button/" + theirs.hashCode() + "/Selection/Selection");
        assertThat(handed).as("each comm's client selects its own button").hasSize(2);
    }

    /** The real dispatcher, fed frames directly instead of over a socket. */
    private static final class Feeder extends AbstractBinaryCommService {
        void deliver(String channel) {
            byte[] header = dev.equo.swt.harness.Channels.header(channel);
            byte[] frame = Arrays.copyOf(header, header.length);
            onBinaryMessage(frame, 0, frame.length);
        }

        @Override
        protected void broadcast(byte[] frame, int offset, int length) {
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public void stop() {
        }
    }
}
