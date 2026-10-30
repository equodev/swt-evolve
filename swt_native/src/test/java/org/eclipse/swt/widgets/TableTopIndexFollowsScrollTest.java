package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.WidgetWire;
import dev.equo.swt.comm.AbstractBinaryCommService;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A Table's rows are scrolled on the client, which tells Java how far a scroll moved them: getTopIndex
 * and getItem(Point) answer from it, as they answer what is on screen natively.
 */
@ExtendWith(Mocks.class)
class TableTopIndexFollowsScrollTest {

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
    void getTopIndexAnswersTheRowAScrollLeftFirst() {
        Shell shell = Mocks.shell();
        List<Runnable> handed = new ArrayList<>();
        Display display = shell.getDisplay();
        doAnswer(inv -> handed.add(inv.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        Table table = new Table(shell, SWT.NONE);
        for (int i = 0; i < 100; i++) new TableItem(table, SWT.NONE).setText("Row " + i);

        comm.deliver("Table/" + table.hashCode() + "/Scroll/Scroll",
                WidgetWire.fieldsOf(clientScroll(42 * table.getItemHeight())));
        handed.forEach(Runnable::run);

        assertThat(table.getTopIndex()).isEqualTo(42);
        assertThat(table.getItem(new org.eclipse.swt.graphics.Point(1, table.getHeaderHeight() + 1)))
                .as("the row under the viewport's first pixel is the one the scroll left first")
                .isSameAs(table.getItem(42));
    }

    /** The event the client sends after a scroll: how far the rows moved, in pixels, its only field. */
    private static dev.equo.swt.WireObject clientScroll(int offset) {
        return new dev.equo.swt.WireObject() {
            @Override
            public void writeWireFields(WidgetWire w) {
                w.key(EVENT_Y);
                w.write(offset);
            }

            @Override
            public int wireTypeId() {
                return 0;
            }
        };
    }

    /** Event.y, as wire-ids.txt numbers it. */
    private static final int EVENT_Y = 362;

    /** The real dispatcher, fed frames directly instead of over a socket. */
    private static final class Feeder extends AbstractBinaryCommService {
        void deliver(String channel, byte[] payload) {
            byte[] header = CommService.frameHeader(channel);
            byte[] frame = java.util.Arrays.copyOf(header, header.length + payload.length);
            System.arraycopy(payload, 0, frame, header.length, payload.length);
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
