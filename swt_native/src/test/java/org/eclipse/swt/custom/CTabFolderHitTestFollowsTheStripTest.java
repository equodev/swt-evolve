package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * {@code getItem(Point)} is how the workbench decides which view a drag picked up, so it has to
 * answer from where the tabs are drawn.
 *
 * <p>This side cannot work that out. It lays the strip out itself, without a scroll and with
 * whatever does not fit parked off screen, and it sizes a tab from its own copy of the render
 * side's theme; against a running workbench both were wrong by more than a tab's width, which is
 * how a drag came to move a view nobody had grabbed. So the render side reports where it draws each
 * tab, and this answers from that.
 */
@ExtendWith(Mocks.class)
class CTabFolderHitTestFollowsTheStripTest {

    private static final int STRIP_WIDTH = 200;

    private static final int TAB_HEIGHT = 32;

    /** A strip of five tabs drawn from the left edge, as the render side would report it. */
    private static final int[] FROM_THE_START = {0, 100, 100, 80, 180, 120, 300, 90, 390, 110};

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
    @DisplayName("a point finds the tab drawn there")
    void findsTheTabUnderThePoint() {
        CTabFolder folder = crowdedFolder();
        drawsTabsAt(folder, FROM_THE_START);

        assertThat(hitAt(folder, 1)).isSameAs(folder.getItem(0));
        assertThat(hitAt(folder, 150)).isSameAs(folder.getItem(1));
    }

    @Test
    @DisplayName("once the strip is scrolled, the same point finds the tab now drawn there")
    void followsTheStripAsItScrolls() {
        CTabFolder folder = crowdedFolder();
        drawsTabsAt(folder, FROM_THE_START);
        assertThat(hitAt(folder, 1)).isSameAs(folder.getItem(0));

        // Scrolled by the first tab's width, so everything is drawn that much further left.
        drawsTabsAt(folder, -100, 100, 0, 80, 80, 120, 200, 90, 290, 110);

        assertThat(hitAt(folder, 1))
                .as("the first tab has scrolled off, so the second one starts at the strip's left")
                .isSameAs(folder.getItem(1));
    }

    @Test
    @DisplayName("a tab this side parked off screen can still be hit")
    void reachesATabTheLayoutWouldHaveHidden() {
        CTabFolder folder = crowdedFolder();
        CTabItem last = folder.getItem(folder.getItemCount() - 1);
        assertThat(last.getBounds().x)
                .as("the premise: this side's own layout has it nowhere near the strip")
                .isGreaterThan(STRIP_WIDTH);

        // Scrolled to the end, so the last tab is the one drawn at the left.
        drawsTabsAt(folder, -390, 100, -290, 80, -210, 120, -90, 90, 0, 110);

        assertThat(hitAt(folder, 1))
                .as("the render side has scrolled it into view, so it is what a point there hits")
                .isSameAs(last);
    }

    @Test
    @DisplayName("a point outside the tab strip hits nothing")
    void missesBelowTheStrip() {
        CTabFolder folder = crowdedFolder();
        drawsTabsAt(folder, FROM_THE_START);

        assertThat(folder.getItem(new Point(1, folder.getTabHeight() + 10))).isNull();
    }

    @Test
    @DisplayName("before the render side has said anything, this side's own layout still answers")
    void fallsBackToTheLayoutItHas() {
        CTabFolder folder = crowdedFolder();

        CTabItem first = folder.getItem(0);
        Rectangle bounds = first.getBounds();
        assertThat(bounds.width).as("the premise: this side did lay the first tab out").isPositive();

        assertThat(hitAt(folder, bounds.x + 1)).isSameAs(first);
    }

    @Test
    @DisplayName("the tab under a point is found without going through the application's renderer")
    void neverAsksTheApplicationsRenderer() {
        CTabFolder folder = crowdedFolder();
        drawsTabsAt(folder, FROM_THE_START);
        folder.setRenderer(new RendererThatPaints(folder));

        assertThat(hitAt(folder, 1))
                .as("a renderer draws with the GC it is handed, and a hit test has none to give it")
                .isSameAs(folder.getItem(0));
    }

    /**
     * An application's renderer, as e4 installs one: it paints, so it uses the GC it is handed and
     * there is nothing to hand it here.
     */
    static class RendererThatPaints extends CTabFolderRenderer {
        RendererThatPaints(CTabFolder parent) {
            super(parent);
        }

        @Override
        protected Point computeSize(int part, int state, GC gc, int wHint, int hHint) {
            gc.getClipping();
            return super.computeSize(part, state, gc, wHint, hHint);
        }
    }

    // ---- harness ----

    private CTabFolder crowdedFolder() {
        Shell shell = Mocks.shell();
        // setItemBounds parks whatever does not fit at the display's right edge.
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        folder.setBounds(0, 0, STRIP_WIDTH, 300);
        folder.setTabHeight(TAB_HEIGHT);
        for (String label : new String[]{"Section 3 (TVDSS)", "Data Operations", "Stratigraphic Modeling",
                "Facies Trend Modeling", "Interpretation Set"}) {
            new CTabItem(folder, SWT.NONE).setText(label);
        }
        // The strip is laid out on first use, as upstream lays it out inside getItem.
        folder.getItem(new Point(0, 0));
        return folder;
    }

    private CTabItem hitAt(CTabFolder folder, int x) {
        return folder.getItem(new Point(x, folder.getTabHeight() / 2));
    }

    /** What the render side reports: where it draws each tab, as x,width pairs. */
    private void drawsTabsAt(CTabFolder folder, int... xAndWidth) {
        Event e = new Event();
        StringBuilder text = new StringBuilder();
        for (int value : xAndWidth) {
            if (text.length() > 0) text.append(',');
            text.append(value);
        }
        e.text = text.toString();
        CTabFolderHelper.handleStripLaidOut((DartCTabFolder) folder.getImpl(), e);
    }
}
