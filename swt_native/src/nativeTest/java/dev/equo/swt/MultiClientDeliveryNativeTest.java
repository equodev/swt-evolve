package dev.equo.swt;

import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a widget may be written as while several clients share one comm.
 *
 * <p>Delivery is recorded against a connection id and every client on a comm shares that id, so a
 * widget described to one client is recorded as delivered for all of them. The next client is then
 * handed a name for something it was never given. It recovers — it asks for the widget — but only
 * after a round trip, and with a window per shell it does that for most of the tree, which is what
 * the "unresolved reference: asking for it" traffic on a multi-window session was.
 *
 * <p>So the rule is: while more than one client is connected, nothing travels as a name.
 */
@Tag("native-unit")
class MultiClientDeliveryNativeTest {

    private Display display;
    private final Object comm = new Object();

    @AfterEach
    void tearDown() {
        Serializer.clientsConnected(comm, 1);
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("with one client a delivered widget may be described by what changed")
    void singleClientKeepsDiffing() {
        DartWidget widget = deliveredWidget();

        Serializer.clientsConnected(comm, 1);

        assertThat(Serializer.canDiff(widget, 1))
                .as("one client owns the connection outright, so what it was sent is known")
                .isTrue();
    }

    @Test
    @DisplayName("with a second client nothing is described by what changed")
    void secondClientStopsDiffing() {
        DartWidget widget = deliveredWidget();
        assertThat(Serializer.canDiff(widget, 1)).isTrue();

        Serializer.clientsConnected(comm, 2);

        assertThat(Serializer.canDiff(widget, 1))
                .as("a change is relative to a state the other client may never have been sent")
                .isFalse();
    }

    @Test
    @DisplayName("the last client leaving restores it")
    void dropingBackToOneClientRestoresDiffing() {
        DartWidget widget = deliveredWidget();
        Serializer.clientsConnected(comm, 2);
        assertThat(Serializer.canDiff(widget, 1)).isFalse();

        Serializer.clientsConnected(comm, 1);

        assertThat(Serializer.canDiff(widget, 1))
                .as("closing the extra window costs nothing once it is gone")
                .isTrue();
    }

    @Test
    @DisplayName("one comm with a second client is enough to stop it for every comm")
    void anyCommWithTwoClientsStopsIt() {
        DartWidget widget = deliveredWidget();
        Object otherComm = new Object();
        try {
            Serializer.clientsConnected(otherComm, 2);

            assertThat(Serializer.canDiff(widget, 1))
                    .as("a comm serving one client says nothing about another, and describing too "
                            + "much is the safe direction to be wrong in")
                    .isFalse();
        } finally {
            Serializer.clientsConnected(otherComm, 1);
        }
    }

    /** A widget the client has been told about in full, which is what makes a change describable. */
    private DartWidget deliveredWidget() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        Shell shell = new Shell(display);
        Label label = new Label(shell, SWT.NONE);
        label.setText("delivered");
        DartWidget impl = (DartWidget) label.getImpl();
        // Stand in for a send: the value carries a write stamp for connection 1, and something has
        // changed since, which together are the only conditions a change is written under.
        impl.getValue().sent(1, 1L);
        label.setText("changed");
        return impl;
    }

    private static final class NoopBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();

        NoopBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }
}
