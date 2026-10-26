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

/** Win32 and GTK hide and show the bar of any scrollable and tell the bar; only a canvas is resized. */
@ExtendWith(Mocks.class)
class ScrollBarShowHideEventTest {

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
    @DisplayName("a Text's bar gets Hide and Show once, and the Text no Resize")
    void aTextBarIsToldItsVisibility() {
        Text text = new Text(Mocks.shell(), SWT.MULTI | SWT.V_SCROLL);
        ScrollBar bar = text.getVerticalBar();
        List<String> log = new ArrayList<>();
        bar.addListener(SWT.Hide, e -> log.add("bar Hide"));
        bar.addListener(SWT.Show, e -> log.add("bar Show"));
        text.addListener(SWT.Resize, e -> log.add("text Resize"));

        bar.setVisible(false);
        bar.setVisible(false);
        bar.setVisible(true);
        bar.setVisible(true);

        assertThat(log).containsExactly("bar Hide", "bar Show");
    }
}
