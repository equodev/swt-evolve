package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A Sash is 64 long and as thick as the theme draws it, the same on every platform; a negative hint counts as 0.
 */
@ExtendWith(Mocks.class)
class SashComputeSizeTest {

    private Composite parent;

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
        parent = new Composite(Mocks.shell(), SWT.NONE);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private Point size(int style, int wHint, int hHint) {
        return new Sash(parent, style).computeSize(wHint, hHint);
    }

    @Test
    @DisplayName("a horizontal sash is 64 x 5 and a vertical one 5 x 64")
    void defaults() {
        assertThat(size(SWT.HORIZONTAL, SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(64, 5));
        assertThat(size(SWT.VERTICAL, SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(5, 64));
    }

    @Test
    @DisplayName("a hint replaces its dimension, and a negative one counts as 0")
    void hints() {
        assertThat(size(SWT.HORIZONTAL, 100, 40)).isEqualTo(new Point(100, 40));
        assertThat(size(SWT.HORIZONTAL, 100, SWT.DEFAULT)).isEqualTo(new Point(100, 5));
        assertThat(size(SWT.VERTICAL, SWT.DEFAULT, 90)).isEqualTo(new Point(5, 90));
        assertThat(size(SWT.HORIZONTAL, -7, -9)).isEqualTo(new Point(0, 0));
        assertThat(size(SWT.VERTICAL, -7, SWT.DEFAULT)).isEqualTo(new Point(0, 64));
    }
}
