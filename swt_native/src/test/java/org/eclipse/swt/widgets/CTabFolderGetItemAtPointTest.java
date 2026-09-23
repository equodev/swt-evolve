package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;
import static org.eclipse.swt.widgets.Mocks.display;

/**
 * The lookup the Eclipse workbench needs in order to reorder a view within its own stack.
 *
 * <p>Moving a view to a <em>different</em> stack only needs the folder under the pointer, and that
 * works. Dropping a tab back onto its own strip needs one thing more: which {@code CTabItem} the
 * pointer is over, so the workbench knows where in the row the view lands
 * ({@code DnDInfo#setItemInfo}). This pins the half of that lookup a unit test can reach — the tab
 * bounds and the folder-relative hit test. The other half is {@code display.map(null, folder,
 * cursor)}, which cannot be checked here: the harness's Display is a mock, so asserting on it would
 * be asserting on Mockito.
 *
 * <p>The folder is deliberately placed away from the origin, so geometry that quietly ignores where
 * the folder sits cannot pass.
 */
class CTabFolderGetItemAtPointTest extends SerializeTestBase {

    private static final int FOLDER_X = 254;
    private static final int FOLDER_Y = 657;
    private static final int FOLDER_WIDTH = 749;
    private static final int FOLDER_HEIGHT = 218;

    private CTabFolder folder;
    private CTabItem first;
    private CTabItem second;

    /**
     * The folder parks the tabs it cannot show past the right edge of the screen, so laying tabs out
     * at all reads the display's bounds — which the mocked Display does not answer on its own.
     */
    @BeforeEach
    void giveTheDisplayAScreen() {
        org.mockito.Mockito.when(display().getBounds())
                .thenReturn(new Rectangle(0, 0, 1920, 1080));
    }

    private void createFolder() {
        folder = new CTabFolder(shell(), SWT.NONE);
        folder.setBounds(FOLDER_X, FOLDER_Y, FOLDER_WIDTH, FOLDER_HEIGHT);

        first = new CTabItem(folder, SWT.NONE);
        first.setText("Problems");
        second = new CTabItem(folder, SWT.NONE);
        second.setText("Target Platform State");
        folder.setSelection(0);
    }

    /** Centre of a tab, in folder coordinates. */
    private static Point centreOf(CTabItem item) {
        Rectangle b = item.getBounds();
        return new Point(b.x + b.width / 2, b.y + b.height / 2);
    }

    @Test
    void every_tab_is_laid_out_with_a_width_of_its_own() {
        createFolder();

        assertThat(first.getBounds().width).as("first tab width").isGreaterThan(0);
        assertThat(second.getBounds().width).as("second tab width").isGreaterThan(0);
        assertThat(second.getBounds().x)
                .as("the second tab starts after the first")
                .isGreaterThanOrEqualTo(first.getBounds().x + first.getBounds().width);
    }

    @Test
    void a_point_on_a_tab_resolves_that_tab() {
        createFolder();

        assertThat(folder.getItem(centreOf(first))).as("first tab").isSameAs(first);
        assertThat(folder.getItem(centreOf(second))).as("second tab").isSameAs(second);
    }

    @Test
    void a_point_outside_every_tab_resolves_none() {
        createFolder();

        Rectangle last = second.getBounds();
        assertThat(folder.getItem(new Point(last.x + last.width + 20, last.y + last.height / 2)))
                .as("past the last tab")
                .isNull();
    }
}
