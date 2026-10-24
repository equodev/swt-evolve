package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.FrameRefs;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a client gets back when it asks for a widget it no longer holds.
 *
 * <p>The client lets go of a widget together with everything under it, once the last thing carrying
 * it stops doing so. Whatever the answer names instead of describing is something the client does
 * not hold either, and costs it one more request.
 */
class RefreshSubtreeDeliveryTest {

    private RecordingBridge bridge;
    private Display display;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("asking for a widget describes what it holds, so one request brings its subtree back")
    void refreshDescribesTheSubtree() {
        Shell shell = new Shell(display);
        Composite part = new Composite(shell, SWT.NONE);
        Composite at = part;
        for (int level = 0; level < 3; level++) at = new Composite(at, SWT.NONE);
        new Label(at, SWT.NONE).setText("leaf");
        shell.open();
        FlutterBridge.update();
        // Described to the client, everything under it included.
        part.setBackground(display.getSystemColor(SWT.COLOR_RED));
        FlutterBridge.update();
        assertThat(bridge.comm.sent).anyMatch(f -> f.json.contains("\"text\":\"leaf\""));

        int requests = 0;
        Set<Long> missing = new LinkedHashSet<>(Set.of((long) FlutterBridge.id(part)));
        while (!missing.isEmpty() && requests < 10) {
            for (long id : missing) FlutterBridge.resendWidget(id);
            requests++;
            bridge.comm.sent.clear();
            FlutterBridge.update();
            missing = FrameRefs.of(bridge.comm.sent).unresolved;
        }

        assertThat(requests)
                .as("requests to rebuild a part four composites deep that the client let go of")
                .isEqualTo(1);
    }
}
