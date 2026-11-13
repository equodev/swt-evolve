package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Where a bar's items sit when the bar wraps, and how wide and tall they are next to each other:
 * a wrapping horizontal bar starts a new row where the next item has no room left, a vertical bar
 * keeps one column, and every item of a run takes the extent of the largest one across the bar.
 */
@Tag("native-unit")
@ExtendWith(ImageMeasuredToolItems.class)
class ToolBarLayoutBoundsNativeTest {

    private static final int ITEM_W = 23;
    private static final int ITEM_H = 22;
    private static final int ARROW_W = 15;

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

    private ToolBar bar(int style) {
        Shell shell = DartMocks.dartShell();
        when(shell.getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        return new ToolBar(shell, style);
    }

    private ToolItem item(ToolBar bar, int style, int imageSize) {
        ToolItem item = new ToolItem(bar, style);
        item.setImage(new Image(DartMocks.dartDisplay(), imageSize, imageSize));
        return item;
    }

    @Test
    void aWrappingBarStartsANewRunWhereTheNextItemHasNoRoom() {
        ToolBar bar = bar(SWT.HORIZONTAL | SWT.WRAP);
        bar.setBounds(0, 0, 2 * ITEM_W + 4, 200);
        ToolItem[] items = { item(bar, SWT.PUSH, 16), item(bar, SWT.PUSH, 16), item(bar, SWT.PUSH, 16), item(bar, SWT.PUSH, 16) };

        assertThat(items[0].getBounds()).isEqualTo(new Rectangle(0, 0, ITEM_W, ITEM_H));
        assertThat(items[1].getBounds()).isEqualTo(new Rectangle(ITEM_W, 0, ITEM_W, ITEM_H));
        assertThat(items[2].getBounds()).isEqualTo(new Rectangle(0, ITEM_H + 2, ITEM_W, ITEM_H));
        assertThat(items[3].getBounds()).isEqualTo(new Rectangle(ITEM_W, ITEM_H + 2, ITEM_W, ITEM_H));
        assertThat(bar.getItem(new Point(3, ITEM_H + 5))).isSameAs(items[2]);
    }

    @Test
    void aVerticalWrappingBarKeepsOneColumnWhereTheNextItemHasNoRoom() {
        ToolBar bar = bar(SWT.VERTICAL | SWT.WRAP);
        bar.setBounds(0, 0, 200, 2 * ITEM_H + 4);
        ToolItem[] items = { item(bar, SWT.PUSH, 16), item(bar, SWT.PUSH, 16), item(bar, SWT.PUSH, 16) };

        assertThat(items[1].getBounds()).isEqualTo(new Rectangle(0, ITEM_H, ITEM_W, ITEM_H));
        assertThat(items[2].getBounds()).isEqualTo(new Rectangle(0, 2 * ITEM_H, ITEM_W, ITEM_H));
        assertThat(bar.getRowCount()).isEqualTo(1);
        assertThat(bar.computeSize(SWT.DEFAULT, 2 * ITEM_H + 4).x).isEqualTo(ITEM_W);
    }

    @Test
    void aBarKeepsSmooth() {
        assertThat(bar(SWT.SMOOTH).getStyle() & SWT.SMOOTH).isEqualTo(SWT.SMOOTH);
    }

    @Test
    void theItemsOfAVerticalBarAreAsWideAsTheWidest() {
        ToolBar bar = bar(SWT.VERTICAL);
        ToolItem narrow = item(bar, SWT.PUSH, 16);
        ToolItem wide = item(bar, SWT.DROP_DOWN, 16);

        assertThat(narrow.getBounds().width).isEqualTo(ITEM_W + ARROW_W);
        assertThat(wide.getBounds().width).isEqualTo(ITEM_W + ARROW_W);
    }

    @Test
    void theItemsOfAHorizontalBarAreAsTallAsTheTallest() {
        ToolBar bar = bar(SWT.HORIZONTAL);
        ToolItem small = item(bar, SWT.PUSH, 16);
        ToolItem big = item(bar, SWT.PUSH, 40);

        assertThat(small.getBounds().height).isEqualTo(big.getBounds().height);
        assertThat(big.getBounds().height).isGreaterThan(ITEM_H);
    }

    @Test
    void aSeparatorWithoutAWidthOfItsOwnAnswersTheWidthItIsLaidOutAt() {
        ToolBar bar = bar(SWT.HORIZONTAL);
        ToolItem separator = new ToolItem(bar, SWT.SEPARATOR);

        assertThat(separator.getWidth()).isPositive().isEqualTo(separator.getBounds().width);
        separator.setWidth(40);
        separator.setWidth(SWT.DEFAULT);
        assertThat(separator.getWidth()).isPositive().isEqualTo(separator.getBounds().width);
    }

    @Test
    void theItemsOfAVerticalBarAnswerTheColumnsWidth() {
        ToolBar bar = bar(SWT.VERTICAL);
        ToolItem push = item(bar, SWT.PUSH, 16);
        item(bar, SWT.DROP_DOWN, 16);
        ToolItem separator = new ToolItem(bar, SWT.SEPARATOR);

        assertThat(push.getWidth()).isEqualTo(ITEM_W + ARROW_W);
        assertThat(separator.getWidth()).isEqualTo(ITEM_W + ARROW_W);
    }
}
