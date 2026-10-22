package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * On every native platform the frame of a Text, and the room around the text it brings, is part of
 * its trim only with SWT.BORDER. A borderless single-line Text is as tall as its line.
 */
@ExtendWith(Mocks.class)
class TextBorderSizeTest {

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
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void borderlessTextIsSmallerThanABorderedOne() {
        Shell shell = Mocks.shell();
        Point borderless = sizeOf(new Text(shell, SWT.SINGLE));
        Point bordered = sizeOf(new Text(shell, SWT.SINGLE | SWT.BORDER));

        assertThat(borderless.y).isLessThan(bordered.y);
        assertThat(borderless.x).isLessThan(bordered.x);
    }

    private static Point sizeOf(Text text) {
        text.setText("21.10.2026");
        return text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }
}
