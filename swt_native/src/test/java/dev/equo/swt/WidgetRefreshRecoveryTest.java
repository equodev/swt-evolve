package dev.equo.swt;

import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The client's way back when it holds a state an update does not fit.
 *
 * <p>Part of the delivery work, and in this package because the request handler it exercises is
 * package-private. Once updates carry only what changed, an update is only meaningful applied to the
 * exact state it was computed from — so a client that finds it holds some other state must be able
 * to ask for the widget whole rather than guess. That request is the one recovery path in the
 * design, which makes "does it actually produce a frame" worth pinning before anything depends on
 * it.
 *
 * <p>The point is not that recovery works often. It should never happen at all; the suites assert
 * the counter stays at zero. The point is that when it does, it is a repair and not a silence.
 */
@ExtendWith(Mocks.class)
class WidgetRefreshRecoveryTest {

    private RecordingBridge bridge;

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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("asking for a widget by id sends that widget's state")
    void refreshResendsTheWidget() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        label.setText("current");
        settle(parent);

        FlutterBridge.handleWidgetRefresh(String.valueOf(label.hashCode()));
        FlutterBridge.update();

        assertThat(bridge.comm.sent)
                .as("a client that cannot use an update has exactly one way back, and it has to work")
                .anyMatch(f -> f.event.equals("Label/" + label.hashCode())
                        && f.json.contains("\"text\":\"current\""));
    }

    @Test
    @DisplayName("asking for a widget that no longer exists is quiet")
    void refreshForUnknownIdIsIgnored() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        settle(parent);

        FlutterBridge.handleWidgetRefresh("999999999");
        FlutterBridge.handleWidgetRefresh("not a number");
        FlutterBridge.handleWidgetRefresh(null);
        FlutterBridge.update();

        assertThat(bridge.comm.sent)
                .as("the request names an id the client saw; by the time it arrives the widget may "
                        + "be disposed, and a race is not an error worth throwing over")
                .isEmpty();
    }

    @Test
    @DisplayName("asking for a disposed widget is quiet")
    void refreshForDisposedWidgetIsIgnored() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        label.setText("gone");
        settle(parent);
        int id = label.hashCode();
        label.dispose();
        bridge.comm.sent.clear();

        FlutterBridge.handleWidgetRefresh(String.valueOf(id));
        FlutterBridge.update();

        assertThat(bridge.comm.sent).noneMatch(f -> f.event.equals("Label/" + id));
    }

    private void settle(Widget root) {
        FlutterBridge.update();
        markSent(root);
        bridge.comm.sent.clear();
    }

    private void markSent(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (widget instanceof Composite composite)
            for (var child : composite.getChildren()) markSent(child);
    }
}
