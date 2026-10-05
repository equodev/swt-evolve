package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A tool item's bounds are where the client draws it: a horizontal bar taller than its items centres
 * them, so a popup placed under an item with {@code toDisplay} lands under what the user sees.
 */
@Tag("native-unit")
class ToolItemBoundsInTallerBarNativeTest {

    /** A 16x16 image item: the image plus 7x6 of padding. */
    private static final int ITEM_H = 22;

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

    private ToolItem itemIn(int barHeight) {
        Shell shell = DartMocks.dartShell();
        org.mockito.Mockito.when(shell.getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        ToolBar bar = new ToolBar(shell, SWT.HORIZONTAL);
        ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setImage(new Image(DartMocks.dartDisplay(), 16, 16));
        bar.setBounds(0, 0, 200, barHeight);
        return item;
    }

    @Test
    void anItemInABarTallerThanItIsCentredAcrossTheBar() {
        Rectangle bounds = itemIn(40).getBounds();

        assertThat(bounds.y).isEqualTo((40 - ITEM_H) / 2);
        assertThat(bounds.height).isEqualTo(ITEM_H);
    }

    @Test
    void anItemInABarAsTallAsItStaysAtTheTop() {
        assertThat(itemIn(ITEM_H).getBounds().y).isZero();
    }
}
