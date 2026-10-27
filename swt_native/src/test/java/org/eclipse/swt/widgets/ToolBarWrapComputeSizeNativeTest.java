package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/** A {@link SWT#WRAP} bar measured at a given extent answers with the runs its layout wraps it into. */
@Tag("native-unit")
class ToolBarWrapComputeSizeNativeTest {

    /** A 16x16 image item: the image plus 7x6 of padding. */
    private static final int ITEM_W = 23;

    private static final int ITEM_H = 22;

    /** The gap the layout leaves between two wrapped runs. */
    private static final int RUN_SPACING = 2;

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

    private ToolBar bar(int style, int items) {
        Shell shell = DartMocks.dartShell();
        org.mockito.Mockito.when(shell.getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        ToolBar bar = new ToolBar(shell, style);
        for (int i = 0; i < items; i++) {
            new ToolItem(bar, SWT.PUSH).setImage(new Image(DartMocks.dartDisplay(), 16, 16));
        }
        return bar;
    }

    @Test
    void aHorizontalBarTooNarrowForItsItemsGrowsARow() {
        ToolBar bar = bar(SWT.HORIZONTAL | SWT.WRAP, 4);

        assertThat(bar.computeSize(2 * ITEM_W + 10, SWT.DEFAULT))
                .isEqualTo(new Point(2 * ITEM_W + 10, 2 * ITEM_H + RUN_SPACING));
    }

    @Test
    void aVerticalBarTooShortForItsItemsGrowsAColumn() {
        ToolBar bar = bar(SWT.VERTICAL | SWT.WRAP, 4);

        assertThat(bar.computeSize(SWT.DEFAULT, 3 * ITEM_H))
                .isEqualTo(new Point(2 * ITEM_W + RUN_SPACING, 3 * ITEM_H));
    }

    @Test
    void aBarWideEnoughForItsItemsStaysOneRow() {
        ToolBar bar = bar(SWT.HORIZONTAL | SWT.WRAP, 4);

        assertThat(bar.computeSize(SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(4 * ITEM_W, ITEM_H));
        assertThat(bar.computeSize(4 * ITEM_W, SWT.DEFAULT)).isEqualTo(new Point(4 * ITEM_W, ITEM_H));
    }
}
