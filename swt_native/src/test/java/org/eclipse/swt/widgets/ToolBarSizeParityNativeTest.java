package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Dart-backed widget has no native handle to measure, so {@code Sizes} computes its preferred size
 * by hand. An application lays a toolbar out from that number, so a toolbar that measures differently
 * than the platform's is placed somewhere the platform would not have placed it — and everything the
 * application then derives from its position inherits the difference.
 *
 * <p>The expectations here are not invented: they were read off native Win32 SWT 3.124.200 by
 * building the same toolbars against the stock fragment and printing {@code computeSize} and each
 * item's bounds. The numbers are asymmetric and not guessable, which is the point of measuring them.
 */
@Tag("native-unit")
class ToolBarSizeParityNativeTest {

    /** Native adds this much around an item's image. Asymmetric — it is not one inset twice. */
    private static final int NATIVE_PAD_W = 7;
    private static final int NATIVE_PAD_H = 6;

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
        return new ToolBar(DartMocks.dartShell(), style);
    }

    private Image image(int size) {
        return new Image(DartMocks.dartDisplay(), size, size);
    }

    @Test
    void anImageItemMatchesTheNativePadding() {
        // native: 20x20 image -> item 27x26.
        ToolBar bar = bar(SWT.VERTICAL);
        ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setImage(image(20));

        Point size = bar.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        assertThat(size.x).as("width around the image").isEqualTo(20 + NATIVE_PAD_W);
        assertThat(size.y).as("height around the image").isEqualTo(20 + NATIVE_PAD_H);
    }

    @Test
    void aDropDownItemLeavesTheRoomNativeLeaves() {
        // native: 16x16 image, DROP_DOWN -> item 38x22, i.e. 15 more than the 23 a PUSH gets.
        ToolBar bar = bar(SWT.VERTICAL);
        ToolItem item = new ToolItem(bar, SWT.DROP_DOWN);
        item.setImage(image(16));

        Point size = bar.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        assertThat(size.x)
                .as("a drop-down's arrow: native leaves 15px beyond the image's own padded width")
                .isEqualTo(38);
    }

    @Test
    void aSeparatorInAVerticalBarClaimsNoWidth() {
        // native: a separator in a vertical bar is 0 wide and 8 tall. Claiming a full item width
        // widens the whole bar, because a vertical bar is as wide as its widest item.
        ToolBar bar = bar(SWT.VERTICAL);
        ToolItem first = new ToolItem(bar, SWT.PUSH);
        first.setImage(image(16));
        new ToolItem(bar, SWT.SEPARATOR);
        ToolItem last = new ToolItem(bar, SWT.PUSH);
        last.setImage(image(16));

        Point size = bar.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        assertThat(size.x)
                .as("a separator must not make the bar wider than its real items")
                .isEqualTo(16 + NATIVE_PAD_W);
        assertThat(size.y)
                .as("two 22-tall items plus an 8-tall separator")
                .isEqualTo(22 + 8 + 22);
    }

    @Test
    void aVerticalBarIsAsWideAsOneItemAndAsTallAsTheirSum() {
        // native: five 16x16 PUSH items, vertical -> 23 x 110.
        ToolBar bar = bar(SWT.VERTICAL);
        for (int i = 0; i < 5; i++) {
            new ToolItem(bar, SWT.PUSH).setImage(image(16));
        }

        Point size = bar.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        assertThat(size.x).isEqualTo(16 + NATIVE_PAD_W);
        assertThat(size.y).isEqualTo(5 * (16 + NATIVE_PAD_H));
    }
}
