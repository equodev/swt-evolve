package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** An image travels once, and is named after that. */
@ExtendWith(Mocks.class)
class ImageTravelsOnceTest {

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
        FlutterBridge.setSendObserver(null);
    }

    @Test
    void anImageIsDescribedOnceAndNamedAfterThat() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        Image icon = new Image(shell.getDisplay(), 16, 16);
        label.setImage(icon);
        settle(shell);

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            // Nothing named: described in full, as a full description of any ancestor is.
            bridge.dirty((DartWidget) label.getImpl());
            FlutterBridge.update().join();
            String described = latestOn(audit, label);
            assertThat(described)
                    .as("the client was never given the image at all")
                    .contains("imageData");

            bridge.dirty((DartWidget) label.getImpl());
            FlutterBridge.update().join();
            String named = latestOn(audit, label);

            assertThat(named)
                    .as("the image was described again to a client that already had it")
                    .doesNotContain("imageData");
            assertThat(named)
                    .as("and what replaces it has to say which image it is")
                    .contains("\"_r\"");
        }

        icon.dispose();
    }


    @Test
    void anImageWithNothingToDrawIsNeverNamed() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        Image empty = new Image(shell.getDisplay(), 8, 8);
        // Render-backed: its pixels live on the far side under a ref, so the client keeps nothing.
        ((org.eclipse.swt.graphics.DartImage) empty.getImpl())._setRemoteRef(7L);
        label.setImage(empty);
        settle(shell);

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            bridge.dirty((DartWidget) label.getImpl());
            FlutterBridge.update().join();
            bridge.dirty((DartWidget) label.getImpl());
            FlutterBridge.update().join();

            assertThat(latestOn(audit, label))
                    .as("an image the far side does not keep was named, so it has nothing to draw")
                    .doesNotContain("\"_r\"");
        }

        empty.dispose();
    }

    @Test
    void anImageRedrawnByAGcIsDescribedAgain() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        Image icon = new Image(shell.getDisplay(), 16, 16);
        label.setImage(icon);
        settle(shell);

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            bridge.dirty((DartWidget) label.getImpl());
            FlutterBridge.update().join();
            assertThat(latestOn(audit, label)).contains("imageData");

            // About to become a render, so the client's copy is stale.
            dev.equo.swt.Serializer.forgetResource(((org.eclipse.swt.graphics.DartImage) icon.getImpl()).getValue());

            bridge.dirty((DartWidget) label.getImpl());
            FlutterBridge.update().join();
            assertThat(latestOn(audit, label))
                    .as("an image whose pixels changed was named, so the client keeps the old ones")
                    .contains("imageData");
        }

        icon.dispose();
    }

    private static String latestOn(DeliveryAudit audit, Widget widget) {
        String channel = widget.getClass().getSimpleName() + "/" + widget.hashCode();
        List<DeliveryAudit.Frame> frames = audit.framesOn(channel);
        assertThat(frames).as("nothing was sent on " + channel).isNotEmpty();
        return frames.get(frames.size() - 1).json();
    }

    private void settle(Widget root) {
        FlutterBridge.update();
        markSent(root);
        bridge.comm.sent.clear();
    }

    private void markSent(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (widget instanceof Composite composite) {
            Control[] children = composite.getChildren();
            if (children == null) return;
            for (Control child : children) markSent(child);
        }
    }
}
