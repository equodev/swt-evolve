package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * {@code CTabItem.isShowing()} is how the workbench's part list decides which tabs to list in bold:
 * the ones the strip does not show. The render side scrolls the strip, so a tab is showing when it
 * is drawn inside the strip, not where this side's own layout would have put it.
 */
@ExtendWith(Mocks.class)
class CTabFolderShowingFollowsTheStripTest {

    private static final int STRIP_WIDTH = 200;

    private static final int TAB_HEIGHT = 32;

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
    @DisplayName("a tab scrolled into the strip is showing")
    void aTabScrolledIntoViewIsShowing() {
        CTabFolder folder = crowdedFolder();
        CTabItem last = folder.getItem(folder.getItemCount() - 1);

        // Scrolled to the end, so the last tab is the one drawn at the left.
        drawsTabsAt(folder, -390, 100, -290, 80, -210, 120, -90, 90, 0, 110);

        assertThat(last.isShowing()).isTrue();
    }

    @Test
    @DisplayName("a tab scrolled out of the strip is not showing")
    void aTabScrolledOutOfViewIsNotShowing() {
        CTabFolder folder = crowdedFolder();
        CTabItem first = folder.getItem(0);

        drawsTabsAt(folder, -390, 100, -290, 80, -210, 120, -90, 90, 0, 110);

        assertThat(first.isShowing()).isFalse();
    }

    @Test
    @DisplayName("a tab cut off where the shown part of the strip ends is not showing")
    void aTabCutOffAtTheRightIsNotShowing() {
        CTabFolder folder = crowdedFolder();

        // At the start of the strip, which ends at 150 where the chevron begins.
        reportsStrip(folder, "0,100,100,80,180,120,300,90,390,110;;0,150");

        assertThat(folder.getItem(0).isShowing()).isTrue();
        assertThat(folder.getItem(1).isShowing()).isFalse();
        assertThat(folder.getItem(4).isShowing()).isFalse();
    }

    @Test
    @DisplayName("a tab partly scrolled out at the left is not showing")
    void aTabPartlyScrolledOutAtTheLeftIsNotShowing() {
        CTabFolder folder = crowdedFolder();

        reportsStrip(folder, "-330,100,-230,80,-150,120,-30,90,60,90;;0,150");

        assertThat(folder.getItem(3).isShowing()).isFalse();
        assertThat(folder.getItem(4).isShowing()).isTrue();
    }

    @Test
    @DisplayName("no tab of a strip squeezed to nothing is showing")
    void noTabOfASqueezedStripIsShowing() {
        CTabFolder folder = crowdedFolder();

        // A folder in a slot smaller than its margins is drawn at scale zero.
        reportsStrip(folder, "0,0,0,0,0,0,0,0,0,0;;0,0");

        assertThat(folder.getItem(0).isShowing()).isFalse();
    }

    @Test
    @DisplayName("a strip drawn at another folder width does not say which tabs show")
    void aStripDrawnAtAnotherWidthDoesNotDecide() {
        CTabFolder folder = crowdedFolder();
        folder.setBounds(0, 0, 600, 300);
        // Every tab fits the wide folder.
        reportsStrip(folder, "0,100,100,80,180,120,300,90,390,110;;0,600;0,0,0,0,0;600");

        folder.setBounds(0, 0, STRIP_WIDTH, 300);

        for (CTabItem item : folder.getItems()) {
            boolean laidOutOnScreen = item.getBounds().x < STRIP_WIDTH;
            assertThat(item.isShowing()).as(item.getText()).isEqualTo(laidOutOnScreen);
        }
    }

    @Test
    @DisplayName("a strip drawn before the tabs were laid out again does not say which tabs show")
    void aStripDrawnForAnOlderLayoutDoesNotDecide() {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        folder.setBounds(0, 0, 400, 300);
        folder.setTabHeight(TAB_HEIGHT);
        for (String label : new String[]{"One", "Two", "Three"}) new CTabItem(folder, SWT.NONE).setText(label);
        Rectangle last = folder.getItem(2).getBounds();
        int width = last.x + last.width + 4;
        folder.setBounds(0, 0, width, 300);
        assertThat(folder.getItem(2).getBounds().x).as("sanity: every tab fits").isLessThan(width);
        int seq = ((DartCTabFolder) folder.getImpl()).stripLayoutSeq;
        reportsStrip(folder, drawnAtJavaBounds(folder) + ";;0," + width + ";0,0,0;" + width + ";" + seq);

        // At the same width, a top-right control takes the room of the last tab.
        Label topRight = new Label(folder, SWT.NONE);
        topRight.setText("TopRight");
        folder.setTopRight(topRight, SWT.RIGHT);
        folder.getItem(new Point(0, 0));

        assertThat(folder.getItem(2).getBounds().x).as("sanity: the last tab is parked").isGreaterThanOrEqualTo(width);
        for (CTabItem item : folder.getItems()) {
            boolean laidOutOnScreen = item.getBounds().x < width;
            assertThat(item.isShowing()).as(item.getText()).isEqualTo(laidOutOnScreen);
        }
    }

    private static String drawnAtJavaBounds(CTabFolder folder) {
        StringBuilder text = new StringBuilder();
        for (CTabItem item : folder.getItems()) {
            Rectangle bounds = item.getBounds();
            if (text.length() > 0) text.append(',');
            text.append(bounds.x).append(',').append(bounds.width);
        }
        return text.toString();
    }

    @Test
    @DisplayName("a strip drawn at the folder's width decides which tabs show")
    void aStripDrawnAtTheFolderWidthDecides() {
        CTabFolder folder = crowdedFolder();

        reportsStrip(folder, "-390,100,-290,80,-210,120,-90,90,0,110;;0,150;0,0,0,0,0;" + STRIP_WIDTH);

        assertThat(folder.getItem(0).isShowing()).isFalse();
        assertThat(folder.getItem(4).isShowing()).isTrue();
    }

    @Test
    @DisplayName("a tab added after the strip reported follows this side's layout until the next report")
    void aTabTheStripHasNotDrawnYetFollowsThisSidesLayout() {
        CTabFolder folder = crowdedFolder();
        reportsStrip(folder, "0,100,100,80,180,120,300,90,390,110;;0,150");

        CTabItem added = new CTabItem(folder, SWT.NONE);
        added.setText("Added");
        folder.setSelection(added);
        folder.getItem(new Point(0, 0));

        assertThat(added.isShowing()).isTrue();
    }

    @Test
    @DisplayName("a selected tab being closed answers isShowing from the selection event its closing fires")
    void aClosingSelectedTabAnswersFromTheSelectionItsClosingFires() {
        CTabFolder folder = crowdedFolder();
        CTabItem closing = folder.getItem(2);
        folder.setSelection(closing);
        reportsStrip(folder, "0,100,100,80,180,120,300,90,390,110;;0,150");

        // The folder has already dropped the tab from its items when it selects the next one.
        List<Object> answers = new ArrayList<>();
        folder.addListener(SWT.Selection, e -> {
            try {
                answers.add(closing.isShowing());
            } catch (RuntimeException failure) {
                answers.add(failure);
            }
        });
        try (Mocks.Delivery delivery = Mocks.deliverEvents(Mocks.display())) {
            closing.dispose();
        }

        assertThat(answers).containsExactly(false);
    }

    // ---- harness ----

    private CTabFolder crowdedFolder() {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        folder.setBounds(0, 0, STRIP_WIDTH, 300);
        folder.setTabHeight(TAB_HEIGHT);
        for (String label : new String[]{"Section 3 (TVDSS)", "Data Operations", "Stratigraphic Modeling",
                "Facies Trend Modeling", "Interpretation Set"}) {
            new CTabItem(folder, SWT.NONE).setText(label);
        }
        folder.getItem(new Point(0, 0));
        return folder;
    }

    private void reportsStrip(CTabFolder folder, String report) {
        Event e = new Event();
        e.text = report;
        CTabFolderHelper.handleStripLaidOut((DartCTabFolder) folder.getImpl(), e);
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
