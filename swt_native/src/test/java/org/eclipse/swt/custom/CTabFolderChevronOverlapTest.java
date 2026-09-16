package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * The chevron is packed right-to-left together with the other trailing tab controls and is then
 * moved to sit just after the last visible tab item. That move must not push it past the slot it
 * was packed into: {@code getRightItemEdge()} reserves no room for a {@code SWT.WRAP} topRight
 * control (it may wrap away instead), so the tab items are free to grow into the space such a
 * control occupies when it is laid out inline, and hugging the last item then puts the chevron
 * on top of it.
 *
 * The decisive inputs are that budget, which sums the item widths, and the move, which reads the
 * last item's position. The two are set directly here: which folder widths drive them apart
 * depends on the platform's font metrics, and the invariant does not.
 */
@ExtendWith(Mocks.class)
class CTabFolderChevronOverlapTest {

    private static final int FOLDER_WIDTH = 400;

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
    void theChevronStopsAtItsPackedSlotWhenTheLastTabReachesUnderTheTrailingControls() {
        CTabFolder folder = folderShowingAChevron();
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        // A narrow last tab sitting far to the right: the folder still considers the topRight
        // control to fit inline, while the move wants the chevron out where the tab ends.
        onlyVisibleTabAt(folder, FOLDER_WIDTH, 10);

        assertThat(chevronX(impl, folder.getSize()))
                .as("the chevron must not follow the last tab past the slot it was packed into")
                .isEqualTo(packedChevronX(impl, folder.getSize()));
    }

    @Test
    void theChevronStillFollowsTheLastVisibleTab() {
        CTabFolder folder = folderShowingAChevron();
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        onlyVisibleTabAt(folder, 0, 10);

        int justAfterTheTab = 0 + 10 + DartCTabFolder.SPACING;
        assertThat(justAfterTheTab)
                .as("this scenario needs room between the last tab and the packed slot")
                .isLessThan(packedChevronX(impl, folder.getSize()));
        assertThat(chevronX(impl, folder.getSize()))
                .as("with room to spare the chevron still sits right after the last tab")
                .isEqualTo(justAfterTheTab);
    }

    /** No tab control may overlap another once the folder has laid its header out. */
    @Test
    void theLaidOutHeaderHasNoOverlappingControls() {
        CTabFolder folder = folderShowingAChevron();
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        for (int width = 120; width <= 420; width++) {
            folder.setBounds(0, 0, width, 200);
            impl.updateItems();
            impl.setButtonBounds();
            if (!impl.showChevron) {
                continue;
            }
            assertThat(chevronX(impl, folder.getSize()))
                    .as("folder %dpx wide: the chevron must stay at or left of its packed slot", width)
                    .isLessThanOrEqualTo(packedChevronX(impl, folder.getSize()));
        }
    }

    private CTabFolder folderShowingAChevron() {
        Display display = Mocks.swtDisplay();
        when(display.getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        Shell shell = Mocks.swtShell(display);
        CTabFolder folder = new CTabFolder(shell, SWT.CLOSE);
        folder.setBounds(0, 0, FOLDER_WIDTH, 200);
        for (int i = 1; i <= 5; i++) {
            new CTabItem(folder, SWT.NONE).setText("Item" + i);
        }
        folder.setSelection(0);
        Label topRight = new Label(folder, SWT.BORDER);
        topRight.setText("TopRight");
        folder.setTopRight(topRight, SWT.RIGHT | SWT.WRAP);
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        impl.updateItems();
        impl.setButtonBounds();
        return folder;
    }

    /** Leaves exactly one tab showing, at the given position and width. */
    private void onlyVisibleTabAt(CTabFolder folder, int x, int width) {
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        impl.showChevron = true;
        CTabItem[] items = folder.getItems();
        for (int i = 0; i < items.length; i++) {
            DartCTabItem item = (DartCTabItem) items[i].getImpl();
            item.showing = i == 0;
            item.x = i == 0 ? x : 0;
            item.width = i == 0 ? width : 0;
        }
    }

    private int chevronX(DartCTabFolder impl, Point size) {
        Rectangle[] rects = impl.computeControlBounds(size, new boolean[1][]);
        return rects[rects.length - 1].x;
    }

    /** Where the right-to-left packing puts the chevron, i.e. before the move is applied. */
    private int packedChevronX(DartCTabFolder impl, Point size) {
        impl.showChevron = false;
        try {
            return chevronX(impl, size);
        } finally {
            impl.showChevron = true;
        }
    }
}
