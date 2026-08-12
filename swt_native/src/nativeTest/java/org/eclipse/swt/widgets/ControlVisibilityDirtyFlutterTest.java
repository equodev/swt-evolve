package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("flutter-it")
class ControlVisibilityDirtyFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private Composite openMessageArea(CapturingBridge bridge) {
        FlutterBridge.set(bridge);
        display = new Display();
        Shell shell = new Shell(display);
        Composite messageArea = new Composite(shell, SWT.NONE);
        shell.open();
        return messageArea;
    }

    @Test
    void firstHideOfAFreshlyCreatedControlIsPushedToTheClient() {
        CapturingBridge bridge = new CapturingBridge();
        Composite messageArea = openMessageArea(bridge);

        assertThat(messageArea.getVisible())
                .as("sanity: a freshly created control is visible")
                .isTrue();

        bridge.dirtied.clear();
        messageArea.setVisible(false);

        assertThat(messageArea.getVisible())
                .as("sanity: the control really did become hidden")
                .isFalse();
        assertThat(bridge.dirtied)
                .as("hiding a control for the first time must reach the client; nothing else pushes "
                        + "it, so a missed dirty() leaves the control rendered as visible")
                .contains(messageArea.getImpl());
    }

    @Test
    void redundantShowOfAnAlreadyVisibleControlIsNotPushed() {
        CapturingBridge bridge = new CapturingBridge();
        Composite messageArea = openMessageArea(bridge);

        bridge.dirtied.clear();
        messageArea.setVisible(true);

        assertThat(bridge.dirtied)
                .as("a setVisible() that changes nothing must not re-push the control")
                .doesNotContain(messageArea.getImpl());
    }

    @Test
    void everyVisibilityFlipOfAMessageAreaIsPushedToTheClient() {
        CapturingBridge bridge = new CapturingBridge();
        Composite messageArea = openMessageArea(bridge);

        assertVisibilityFlipIsPushed(bridge, messageArea, false);
        assertVisibilityFlipIsPushed(bridge, messageArea, true);
        assertVisibilityFlipIsPushed(bridge, messageArea, false);
    }

    private static void assertVisibilityFlipIsPushed(CapturingBridge bridge, Control control, boolean visible) {
        bridge.dirtied.clear();
        control.setVisible(visible);

        assertThat(control.getVisible())
                .as("setVisible(%s) must flip the real visibility state", visible)
                .isEqualTo(visible);
        assertThat(bridge.dirtied)
                .as("setVisible(%s) flipped the state, so it must reach the client", visible)
                .contains(control.getImpl());
    }

    private static final class CapturingBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();
        final List<Object> dirtied = new CopyOnWriteArrayList<>();

        CapturingBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void dirty(DartWidget widget) {
            dirtied.add(widget);
            super.dirty(widget);
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }
}
