package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.size.CoolBarTheme;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("flutter-it")
class CoolBarComputeSizeFlutterTest {

    private Display display;

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
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    private static CoolItem itemWithPreferredSize(CoolBar bar, int width, int height) {
        CoolItem item = new CoolItem(bar, SWT.NONE);
        item.setControl(new ToolBar(bar, SWT.FLAT));
        item.setPreferredSize(width, height);
        return item;
    }

    private static int frameWidth() {
        CoolBarTheme frame = CoolBarTheme.get();
        return frame.frameLeft() + frame.frameRight();
    }

    private static int frameHeight() {
        CoolBarTheme frame = CoolBarTheme.get();
        return frame.frameTop() + frame.frameBottom();
    }

    @Test
    void aSingleRowIsAsWideAsItsItemsPreferredWidths() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.FLAT);
        CoolItem first = itemWithPreferredSize(bar, 94, 24);
        CoolItem second = itemWithPreferredSize(bar, 1349, 31);
        int rowWidth = first.getPreferredSize().x + second.getPreferredSize().x;

        Point size = bar.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        assertThat(size.x).as("the row's preferred widths, inside the frame").isEqualTo(rowWidth + frameWidth());
        assertThat(size.y).as("the row's tallest item, inside the frame").isEqualTo(31 + frameHeight());
    }

    @Test
    void anExplicitWrapStacksTheRows() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.FLAT);
        CoolItem first = itemWithPreferredSize(bar, 94, 24);
        CoolItem second = itemWithPreferredSize(bar, 300, 31);
        bar.setWrapIndices(new int[] {1});

        Point size = bar.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        assertThat(size.x).isEqualTo(Math.max(first.getPreferredSize().x, second.getPreferredSize().x) + frameWidth());
        assertThat(size.y).isEqualTo(24 + 31 + frameHeight());
    }

    @Test
    void theSizeDoesNotDependOnTheCurrentBounds() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.FLAT);
        itemWithPreferredSize(bar, 94, 24);
        itemWithPreferredSize(bar, 1349, 31);
        Point natural = bar.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        bar.setSize(40, 55);

        assertThat(bar.computeSize(SWT.DEFAULT, SWT.DEFAULT))
                .as("a bar squeezed into two rows must still report its one-row size")
                .isEqualTo(natural);
    }

    @Test
    void theHintsSizeTheClientArea() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.FLAT);
        itemWithPreferredSize(bar, 94, 24);

        assertThat(bar.computeSize(500, 40)).isEqualTo(new Point(500 + frameWidth(), 40 + frameHeight()));
    }

    @Test
    void theClientAreaIsWhatTheFrameLeaves() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.FLAT);
        bar.setSize(300, 40);
        CoolBarTheme frame = CoolBarTheme.get();

        Rectangle client = bar.getClientArea();

        assertThat(client).isEqualTo(new Rectangle(frame.frameLeft(), frame.frameTop(),
                300 - frameWidth(), 40 - frameHeight()));
        assertThat(bar.computeTrim(client.x, client.y, client.width, client.height))
                .as("computeTrim puts back exactly what getClientArea took away")
                .isEqualTo(new Rectangle(0, 0, 300, 40));
    }

    @Test
    void aBannerGivesItsCoolBarTheRoomItsItemsNeed() {
        Shell shell = new Shell(display);
        shell.setSize(1920, 1080);
        shell.setLayout(new org.eclipse.swt.layout.FillLayout());
        CBanner banner = new CBanner(shell, SWT.NONE);
        CoolBar bar = new CoolBar(banner, SWT.FLAT);
        itemWithPreferredSize(bar, 94, 24);
        CoolItem second = itemWithPreferredSize(bar, 1349, 31);
        banner.setLeft(bar);
        banner.setRight(new Composite(banner, SWT.NONE));

        shell.layout(true, true);

        assertThat(bar.getSize().x)
                .as("the banner lays its left CoolBar out at the CoolBar's computed width")
                .isGreaterThanOrEqualTo(second.getPreferredSize().x + 94);
    }
}
