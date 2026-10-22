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
 * An ARROW button is the size of a scroll bar arrow, as on Win32 (SM_CXVSCROLL x SM_CYVSCROLL); Cocoa
 * hardcodes a similar 14x14. Natively it is never taller than the single-line field it usually sits
 * beside, so the composite around both keeps the field's height.
 */
@ExtendWith(Mocks.class)
class ArrowButtonSizeTest {

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
    void arrowButtonIsTheSizeOfAScrollBarArrow() {
        Button arrow = new Button(Mocks.shell(), SWT.ARROW | SWT.DOWN);

        assertThat(arrow.computeSize(SWT.DEFAULT, SWT.DEFAULT))
                .isEqualTo(new Point(Sizes.SCROLL_BAR_SIZE, Sizes.SCROLL_BAR_SIZE));
    }
}
