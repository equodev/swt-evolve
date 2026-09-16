package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.swtShell;

/**
 * A CTabFolder draws a frame around the page it shows, so its client area is inset from its own
 * left, right and bottom edges — and a layout puts the page, and everything the page positions from
 * its own client area, at that inset. Reporting the folder's full width as client area lays every
 * page one border too far left and one border too wide, and a chain of nested folders adds that up.
 *
 * <p>The numbers are what stock SWT 3.124.200 reports on Windows for a folder of the same bounds:
 * 2 px on each of left/right/bottom, 3 px with {@code SWT.BORDER}.
 */
@DisabledOnOs(OS.LINUX)
class CTabFolderClientAreaBorderTest extends SerializeTestBase {

    private static final int W = 600;
    private static final int H = 400;

    /** Native: left == right == bottom, 2 without SWT.BORDER. */
    private static final int BODY_BORDER = 2;
    private static final int BODY_BORDER_STYLED = 3;

    private CTabFolder folder(Shell shell, int style) {
        CTabFolder folder = new CTabFolder(shell, style);
        folder.setBounds(0, 0, W, H);
        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText("Section");
        Composite page = new Composite(folder, SWT.NONE);
        item.setControl(page);
        folder.setSelection(item);
        return folder;
    }

    @Test
    void thePageIsInsetFromTheFoldersLeftEdge() {
        Rectangle client = folder(swtShell(), SWT.NONE).getClientArea();

        assertThat(client.x)
                .as("a page laid out at x = 0 sits on top of the frame the folder draws, and every "
                        + "control the page positions from its client area is that much too far left")
                .isEqualTo(BODY_BORDER);
    }

    @Test
    void thePageIsInsetFromBothSidesEqually() {
        Rectangle client = folder(swtShell(), SWT.NONE).getClientArea();

        assertThat(W - client.x - client.width)
                .as("the right inset must match the left one, or the page runs under the frame")
                .isEqualTo(BODY_BORDER);
    }

    @Test
    void thePageIsInsetFromTheFoldersBottomEdge() {
        CTabFolder folder = folder(swtShell(), SWT.NONE);
        Rectangle client = folder.getClientArea();

        assertThat(H - client.y - client.height).isEqualTo(BODY_BORDER);
    }

    @Test
    void aStyledBorderWidensTheFrame() {
        Rectangle client = folder(swtShell(), SWT.BORDER).getClientArea();

        assertThat(client.x).isEqualTo(BODY_BORDER_STYLED);
        assertThat(W - client.x - client.width).isEqualTo(BODY_BORDER_STYLED);
        assertThat(H - client.y - client.height).isEqualTo(BODY_BORDER_STYLED);
    }

    @Test
    void tabsOnTheBottomKeepTheSameSideInsets() {
        CTabFolder folder = folder(swtShell(), SWT.NONE);
        folder.setTabPosition(SWT.BOTTOM);
        Rectangle client = folder.getClientArea();

        assertThat(client.x).isEqualTo(BODY_BORDER);
        assertThat(W - client.x - client.width).isEqualTo(BODY_BORDER);
        assertThat(client.y)
                .as("with the strip below, the top edge carries the frame instead")
                .isEqualTo(BODY_BORDER);
    }

    @Test
    void aFolderSizedToItsPageLeavesRoomForTheFrame() {
        // The other direction of the same number: a folder laid out at its preferred size has to be
        // wide enough that its page still gets the width the page asked for, frame included.
        CTabFolder folder = folder(swtShell(), SWT.NONE);
        Composite page = (Composite) folder.getItem(0).getControl();
        int pageWidth = page.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

        folder.setSize(folder.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        assertThat(folder.getClientArea().width)
                .as("a folder sized to its page must still be able to give the page that width")
                .isGreaterThanOrEqualTo(pageWidth);
    }

    @Test
    void computeTrimIsTheInverseOfGetClientArea() {
        // A layout that sizes a folder to its page round-trips through these two. If they disagree
        // the folder grows or shrinks by the difference every time it is laid out.
        for (int style : new int[] { SWT.NONE, SWT.BORDER }) {
            CTabFolder folder = folder(swtShell(), style);
            Rectangle client = folder.getClientArea();
            Rectangle trim = folder.computeTrim(client.x, client.y, client.width, client.height);

            assertThat(new int[] { trim.x, trim.y, trim.width, trim.height })
                    .as("computeTrim must give back the folder's own bounds")
                    .containsExactly(0, 0, W, H);
        }
    }
}
