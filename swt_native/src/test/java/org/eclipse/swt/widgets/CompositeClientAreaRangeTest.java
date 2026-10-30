package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/** A visible bar takes its width from the client area only while it has a range to scroll: the client draws no other. */
@ExtendWith(Mocks.class)
class CompositeClientAreaRangeTest {

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
    @DisplayName("a bar with a range to scroll takes its width from the client area")
    void barWithARangeTakesItsWidth() {
        Composite composite = new Composite(Mocks.shell(), SWT.V_SCROLL);
        composite.setBounds(0, 0, 100, 80);
        composite.getVerticalBar().setValues(0, 0, 100, 10, 1, 10);
        Rectangle area = composite.getClientArea();
        assertThat(area.width).isEqualTo(100 - Sizes.SCROLL_BAR_SIZE);
        assertThat(area.height).isEqualTo(80);
    }

    @Test
    @DisplayName("a visible bar with nothing to scroll leaves the client area whole")
    void barWithNoRangeLeavesTheAreaWhole() {
        Composite composite = new Composite(Mocks.shell(), SWT.V_SCROLL);
        composite.setBounds(0, 0, 100, 80);
        composite.getVerticalBar().setValues(0, 0, 10, 10, 1, 10);
        assertThat(composite.getVerticalBar().getVisible()).isTrue();
        assertThat(composite.getClientArea().width).isEqualTo(100);
    }
}
