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

/**
 * Where each item sits inside its toolbar, and the hit test an application runs against it.
 *
 * <p>A {@code MenuDetect} listener on a {@code ToolBar} is handed a point and has no other way to
 * learn which button was pressed than {@code ToolBar.getItem(Point)}, which walks the items
 * comparing {@code ToolItem.getBounds()}. Items that all report the origin collapse that walk: the
 * first item answers for everything inside its own box and nothing answers past it, so a listener
 * that returns early on a null item does nothing at all — no menu, not even an empty one.
 *
 * <p>The expectations are read off native Win32 SWT 3.124.200, running the same toolbars against the
 * stock fragment and printing every item's bounds. Native lays a bar out as one run: an item starts
 * at the running total of the items before it, separators included.
 */
@Tag("native-unit")
class ToolBarItemBoundsNativeTest {

    /** Native's size for a 16x16 image item: the image plus an asymmetric 7x6 of padding. */
    private static final int ITEM_W = 23;

    private static final int ITEM_H = 22;

    /** A separator's thickness along a horizontal bar when the application set no width. */
    private static final int SEPARATOR_W = 8;

    /** The room a drop-down's arrow takes beyond the item's own padded width. */
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

    /**
     * An item's measured size is clamped against the room its container leaves, so the bar needs a
     * container with a size. It is deliberately larger than any bar built here, so the clamp never
     * masks the geometry under test.
     */
    private ToolBar bar(int style) {
        Shell shell = DartMocks.dartShell();
        org.mockito.Mockito.when(shell.getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        return new ToolBar(shell, style);
    }

    private Image image(int size) {
        return new Image(DartMocks.dartDisplay(), size, size);
    }

    private ToolItem push(ToolBar bar) {
        ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setImage(image(16));
        return item;
    }

    /** Centre of an item, in toolbar coordinates — what a MenuDetect listener converts a press to. */
    private static Point centreOf(ToolItem item) {
        Rectangle b = item.getBounds();
        return new Point(b.x + b.width / 2, b.y + b.height / 2);
    }

    @Test
    void aHorizontalBarPlacesEachItemAfterTheOneBeforeIt() {
        // native: 23-wide items at x = 0, 23, 46, 69.
        ToolBar bar = bar(SWT.HORIZONTAL);
        ToolItem[] items = { push(bar), push(bar), push(bar), push(bar) };

        for (int i = 0; i < items.length; i++) {
            assertThat(items[i].getBounds())
                    .as("item " + i)
                    .isEqualTo(new Rectangle(i * ITEM_W, 0, ITEM_W, ITEM_H));
        }
    }

    @Test
    void aVerticalBarPlacesEachItemBelowTheOneBeforeIt() {
        // native: 22-tall items at y = 0, 22, 44, 66.
        ToolBar bar = bar(SWT.VERTICAL);
        ToolItem[] items = { push(bar), push(bar), push(bar), push(bar) };

        for (int i = 0; i < items.length; i++) {
            assertThat(items[i].getBounds())
                    .as("item " + i)
                    .isEqualTo(new Rectangle(0, i * ITEM_H, ITEM_W, ITEM_H));
        }
    }

    @Test
    void aSeparatorTakesItsOwnPlaceInTheRun() {
        // native: PUSH at 0, SEPARATOR at 23 (8 wide), PUSH at 31.
        ToolBar bar = bar(SWT.HORIZONTAL);
        push(bar);
        ToolItem separator = new ToolItem(bar, SWT.SEPARATOR);
        ToolItem last = push(bar);

        assertThat(separator.getBounds().x).as("the separator starts after the first item").isEqualTo(ITEM_W);
        assertThat(separator.getBounds().width).as("separator width").isEqualTo(SEPARATOR_W);
        assertThat(last.getBounds().x)
                .as("the item after a separator is pushed by the separator's width")
                .isEqualTo(ITEM_W + SEPARATOR_W);
    }

    @Test
    void aDropDownPushesTheItemAfterItByItsArrow() {
        // native: PUSH at 0, DROP_DOWN at 23 (38 wide), PUSH at 61.
        ToolBar bar = bar(SWT.HORIZONTAL);
        push(bar);
        ToolItem dropDown = new ToolItem(bar, SWT.DROP_DOWN);
        dropDown.setImage(image(16));
        ToolItem last = push(bar);

        assertThat(dropDown.getBounds().width).as("drop-down width").isEqualTo(ITEM_W + ARROW_W);
        assertThat(last.getBounds().x)
                .as("the item after a drop-down clears its arrow")
                .isEqualTo(ITEM_W + ITEM_W + ARROW_W);
    }

    @Test
    void aPointOnAnItemResolvesThatItem() {
        ToolBar bar = bar(SWT.HORIZONTAL);
        ToolItem[] items = { push(bar), push(bar), push(bar) };

        for (int i = 0; i < items.length; i++) {
            assertThat(bar.getItem(centreOf(items[i]))).as("item " + i).isSameAs(items[i]);
        }
    }

    @Test
    void aPointPastTheLastItemResolvesNone() {
        ToolBar bar = bar(SWT.HORIZONTAL);
        push(bar);
        ToolItem last = push(bar);

        Rectangle b = last.getBounds();
        assertThat(bar.getItem(new Point(b.x + b.width + 40, b.y + b.height / 2)))
                .as("past the last item")
                .isNull();
    }
}
