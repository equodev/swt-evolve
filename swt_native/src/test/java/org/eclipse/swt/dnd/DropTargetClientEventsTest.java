package org.eclipse.swt.dnd;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.AbstractBinaryCommService;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A drop target is never sent to the client itself, its control draws it, and still the client's
 * drag events for it reach it: through a comm that dispatches each type's events by the id their
 * channel names, as the real ones do.
 */
@ExtendWith(Mocks.class)
class DropTargetClientEventsTest {

    private final Feeder comm = new Feeder();

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
        FlutterBridge.set(new RecordingBridge(comm));
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void aDragOverFromTheClientReachesTheDropTarget() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        List<Runnable> handed = new ArrayList<>();
        doAnswer(inv -> handed.add(inv.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        Tree tree = new Tree(shell, SWT.NONE);
        DropTarget target = new DropTarget(tree, DND.DROP_MOVE);

        comm.deliver("DropTarget/" + target.hashCode() + "/Drop/dragOver");

        assertThat(handed).as("the drop target took the drag over and handed it to the display").hasSize(1);
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
