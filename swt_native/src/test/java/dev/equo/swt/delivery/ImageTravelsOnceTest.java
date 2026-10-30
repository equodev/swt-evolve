package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireValues;
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
import java.util.Map;

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
            List<Object> described = values(flushOf(audit, label));
            assertThat(WireValues.mentions(described, "imageData"))
                    .as("the client was never given the image at all")
                    .isTrue();

            List<Object> named = values(flushOf(audit, label));
            assertThat(WireValues.mentions(named, "imageData"))
                    .as("the image was described again to a client that already had it")
                    .isFalse();
            assertThat(WireValues.mentions(named, "_r"))
                    .as("and what replaces it has to say which image it is")
                    .isTrue();
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
            flushOf(audit, label);
            assertThat(describes(flushOf(audit, label), empty))
                    .as("an image the far side does not keep was named, so it has nothing to draw")
                    .isTrue();
        }

        empty.dispose();
    }

    @Test
    void anImageTheRenderSideIsDrawingCarriesNoPixelsBeforeItsRefArrives() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        Image drawn = new Image(shell.getDisplay(), 1, 28);
        // A GC bound it to the render side. Its ref exists only once that render answers, and until
        // then this side holds what init() allocated: all zeros, which draws as solid black.
        ((org.eclipse.swt.graphics.DartImage) drawn.getImpl())._renderOwnsPixels();
        label.setImage(drawn);
        settle(shell);

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            assertThat(WireValues.find(values(flushOf(audit, label)), m -> m.get("imageData") instanceof Map))
                    .as("a blank buffer reached the client, which keeps it and draws it black")
                    .isEmpty();
        }

        drawn.dispose();
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
            assertThat(WireValues.mentions(values(flushOf(audit, label)), "imageData")).isTrue();

            // About to become a render, so the client's copy is stale.
            dev.equo.swt.Serializer.forgetResource(((org.eclipse.swt.graphics.DartImage) icon.getImpl()).getValue());

            assertThat(WireValues.mentions(values(flushOf(audit, label)), "imageData"))
                    .as("an image whose pixels changed was named, so the client keeps the old ones")
                    .isTrue();
        }

        icon.dispose();
    }

    /** Everything one flush of {@code widget}, scheduled whole, sends: an image can travel as its own record. */
    private List<DeliveryAudit.Frame> flushOf(DeliveryAudit audit, Widget widget) {
        int before = audit.frames().size();
        bridge.dirty((DartWidget) widget.getImpl());
        FlutterBridge.update().join();
        List<DeliveryAudit.Frame> frames = audit.frames().subList(before, audit.frames().size());
        String channel = widget.getClass().getSimpleName() + "/" + widget.hashCode();
        assertThat(frames).as("nothing was sent on " + channel).anyMatch(f -> f.channel().equals(channel));
        return List.copyOf(frames);
    }

    /** What each frame of {@code flush} carries. */
    private static List<Object> values(List<DeliveryAudit.Frame> flush) {
        return flush.stream().map(DeliveryAudit.Frame::value).toList();
    }

    /** Whether {@code flush} describes {@code image} rather than only naming it. */
    private static boolean describes(List<DeliveryAudit.Frame> flush, Image image) {
        String channel = "Image/" + image.hashCode();
        return flush.stream().anyMatch(f -> f.channel().equals(channel));
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
