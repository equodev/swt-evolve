package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * A narrow bar wraps its items onto rows of their own, and the rows reach the render only as the bounds
 * of the items' controls: the wrap indices carry the wraps the application asked for and nothing else.
 */
@Tag("flutter-it")
class CoolBarRowsNativeTest {

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

    private static CoolItem item(CoolBar bar, int minimum, int preferred, int thickness) {
        CoolItem item = new CoolItem(bar, SWT.NONE);
        item.setControl(new Composite(bar, SWT.NONE));
        boolean vertical = (bar.getStyle() & SWT.VERTICAL) != 0;
        item.setMinimumSize(vertical ? 0 : minimum, vertical ? minimum : 0);
        item.setPreferredSize(vertical ? thickness : preferred, vertical ? preferred : thickness);
        item.setSize(item.getPreferredSize());
        return item;
    }

    @Test
    void aBarNarrowerThanItsItemsPutsTheSecondControlOnARowOfItsOwn() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.FLAT);
        bar.setSize(220, 100);
        CoolItem first = item(bar, 150, 180, 24);
        CoolItem second = item(bar, 150, 180, 31);

        Rectangle a = first.getControl().getBounds();
        Rectangle b = second.getControl().getBounds();
        int gripper = second.computeSize(0, 0).x;

        assertThat(b.y).as("the second control starts below the first row").isGreaterThanOrEqualTo(a.y + a.height);
        assertThat(b.x).as("and at the same edge").isEqualTo(a.x);
        assertThat(b.x).isEqualTo(gripper);
        assertThat(b.width).as("at the width of its item, not squeezed to what the first row leaves")
                .isEqualTo(second.getBounds().width - gripper)
                .isGreaterThan(100);
    }

    @Test
    void theRowsAWidthMakesAreNotWrapIndices() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.FLAT);
        bar.setSize(220, 100);
        CoolItem first = item(bar, 150, 180, 24);
        CoolItem second = item(bar, 150, 180, 31);

        assertThat(second.getBounds().y).isGreaterThan(first.getBounds().y);
        assertThat(bar.getWrapIndices()).isEmpty();
        assertThat(bar.getItemOrder()).containsExactly(0, 1);
    }

    @Test
    void anExplicitWrapIsAWrapIndexAndPutsTheItemOnAnotherRow() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.FLAT);
        bar.setSize(900, 100);
        CoolItem first = item(bar, 50, 80, 24);
        CoolItem second = item(bar, 50, 80, 31);
        assertThat(second.getBounds().y).isEqualTo(first.getBounds().y);

        bar.setWrapIndices(new int[] {1});

        assertThat(bar.getWrapIndices()).containsExactly(1);
        assertThat(second.getBounds().y).isGreaterThan(first.getBounds().y);
        assertThat(second.getControl().getBounds().y).isGreaterThanOrEqualTo(first.getControl().getBounds().y + first.getControl().getBounds().height);
    }

    @Test
    void aVerticalBarStacksTheControlsDownTheBar() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.VERTICAL | SWT.FLAT);
        bar.setSize(100, 400);
        CoolItem first = item(bar, 100, 150, 24);
        CoolItem second = item(bar, 100, 150, 24);

        Rectangle a = first.getControl().getBounds();
        Rectangle b = second.getControl().getBounds();

        assertThat(b.x).isEqualTo(a.x);
        assertThat(b.y).isGreaterThanOrEqualTo(a.y + a.height);
        assertThat(a.width).isGreaterThan(0);
    }

    @Test
    void aBarJustAsTallAsItsRowsGrowsByTheFrameAroundThem() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.FLAT);
        bar.setSize(300, 24);
        CoolItem item = new CoolItem(bar, SWT.NONE);
        item.setSize(100, 24);

        assertThat(bar.getClientArea().height).as("the rows still fit inside the frame").isEqualTo(24);
        assertThat(bar.getSize().y).isGreaterThan(24);
    }

    @Test
    void aVerticalBarJustAsWideAsItsColumnsGrowsByTheFrameAroundThem() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.VERTICAL | SWT.FLAT);
        bar.setSize(24, 300);
        CoolItem item = new CoolItem(bar, SWT.NONE);
        item.setSize(24, 100);

        assertThat(bar.getClientArea().width).isEqualTo(24);
        assertThat(bar.getSize().x).isGreaterThan(24);
    }

    private static void mouse(CoolBar bar, int type, int x, int y) {
        Event event = new Event();
        event.button = 1;
        event.x = x;
        event.y = y;
        bar.notifyListeners(type, event);
    }

    @Test
    void aGripperDraggedToTheEndLeavesTheLastItemInsideTheFrame() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.NONE);
        bar.setSize(600, 100);
        item(bar, 40, 100, 20);
        CoolItem second = item(bar, 40, 100, 20);
        CoolItem last = item(bar, 40, 100, 20);
        int width = bar.getClientArea().width;
        assertThat(width).as("the frame takes room from the client area").isLessThan(bar.getSize().x);

        Rectangle grip = second.getBounds();
        mouse(bar, SWT.MouseDown, grip.x + 2, grip.y + 2);
        mouse(bar, SWT.MouseMove, width - 2, grip.y + 2);
        mouse(bar, SWT.MouseUp, width - 2, grip.y + 2);

        Rectangle end = last.getBounds();
        assertThat(end.x + end.width).isEqualTo(width);
    }

    @Test
    void aSecondDoubleClickOnTheWidestLastItemNarrowsIt() {
        Shell shell = new Shell(display);
        CoolBar bar = new CoolBar(shell, SWT.NONE);
        bar.setSize(600, 100);
        item(bar, 40, 100, 20);
        CoolItem last = item(bar, 40, 100, 20);
        int width = bar.getClientArea().width;

        Rectangle grip = last.getBounds();
        mouse(bar, SWT.MouseDoubleClick, grip.x + 2, grip.y + 2);
        grip = last.getBounds();
        mouse(bar, SWT.MouseDoubleClick, grip.x + 2, grip.y + 2);

        int narrowest = last.getMinimumSize().x + last.computeSize(0, 0).x;
        assertThat(last.getBounds().width).as("at its widest it fills the client area, so it narrows").isEqualTo(narrowest);
        assertThat(last.getBounds().x + last.getBounds().width).isEqualTo(width);
    }
}
