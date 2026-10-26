package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Win32 and GTK send Show before the control counts as visible, and Hide after it stopped counting. */
@ExtendWith(Mocks.class)
class ControlShowEventVisibilityTest {

    private Mocks.Delivery delivery;

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
        FlutterBridge.set(new RecordingBridge());
        delivery = Mocks.deliverEvents(Mocks.display());
    }

    @AfterEach
    void tearDown() {
        delivery.close();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("Show is sent while getVisible() is still false, Hide once it is false")
    void showIsSentBeforeTheControlIsVisible() {
        Canvas canvas = new Canvas(Mocks.shell(), SWT.NONE);
        List<String> log = new ArrayList<>();
        canvas.addListener(SWT.Hide, e -> log.add("Hide visible=" + canvas.getVisible()));
        canvas.addListener(SWT.Show, e -> log.add("Show visible=" + canvas.getVisible()));

        canvas.setVisible(false);
        canvas.setVisible(false);
        canvas.setVisible(true);
        canvas.setVisible(true);

        assertThat(canvas.getVisible()).isTrue();
        assertThat(log).containsExactly("Hide visible=false", "Show visible=false");
    }
}
