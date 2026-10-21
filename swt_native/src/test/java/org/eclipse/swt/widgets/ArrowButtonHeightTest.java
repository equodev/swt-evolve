package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An ARROW button usually sits beside a single-line field (a date picker, a spinner-like control),
 * and on every native platform it is no taller than that field. A taller arrow stretches the
 * composite around both, which then stands out of a top-aligned row of single-line controls.
 */
@ExtendWith(Mocks.class)
class ArrowButtonHeightTest {

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
    void arrowButtonIsNoTallerThanASingleLineText() {
        Shell shell = Mocks.shell();
        Text text = new Text(shell, SWT.SINGLE);
        text.setText("21.10.2026");
        Button arrow = new Button(shell, SWT.ARROW | SWT.DOWN);

        assertThat(arrow.computeSize(SWT.DEFAULT, SWT.DEFAULT).y)
                .isLessThanOrEqualTo(text.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
    }
}
