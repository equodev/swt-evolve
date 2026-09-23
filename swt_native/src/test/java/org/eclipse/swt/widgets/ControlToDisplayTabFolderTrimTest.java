package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * {@code Control.toDisplay} — which is what {@code Display.map(control, null, …)} resolves to — has
 * to answer where a control's point lands in the coordinate space a top-level shell's bounds are
 * expressed in. An application that parks a shell over the window and reshapes it to draw its own
 * drag feedback maps a container's rectangle through this and hands the result to
 * {@code Shell.setBounds}; anything this adds that the placement does not lands the feedback that
 * far off whatever it is marking.
 *
 * <p>The walk adds a correction for a parent's top trim, for a {@code Group}: a Group's child is
 * laid out inside the frame the Group draws, and the title height above it is in nobody's bounds.
 * A {@code CTabFolder} is not in that position — its child's bounds already start below the tab
 * strip — so adding the strip a second time double-counts it.
 */
class ControlToDisplayTabFolderTrimTest extends SerializeTestBase {

    /** What Sizes.computeTrim reports above a CTabFolder's client area. */
    private static final int TAB_STRIP = 32;

    private static final int FOLDER_X = 40;
    private static final int FOLDER_Y = 100;

    /**
     * A shell whose own toDisplay is the identity, so what these assert is the offset the walk up
     * from the control accumulates -- not the mock's answer for where the shell sits.
     */
    private static Shell echoingShell() {
        Shell shell = shell();
        when(shell.toDisplay(anyInt(), anyInt()))
                .thenAnswer(call -> new Point(call.getArgument(0), call.getArgument(1)));
        return shell;
    }

    @Test
    void aChildOfATabFolderMapsToItsOwnPositionNotOneStripLower() {
        Shell shell = echoingShell();
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        folder.setBounds(FOLDER_X, FOLDER_Y, 400, 300);

        Composite page = new Composite(folder, SWT.NONE);
        // Where the folder's own layout puts a page: below the tab strip, in folder coordinates.
        page.setBounds(0, TAB_STRIP, 400, 300 - TAB_STRIP);

        Point mapped = page.toDisplay(0, 0);

        assertThat(mapped.y)
                .as("the page already sits a strip below the folder's origin, so its display "
                        + "position is the folder's y plus that strip — counting the strip twice "
                        + "puts everything mapped through the folder one strip too low")
                .isEqualTo(FOLDER_Y + TAB_STRIP);
        assertThat(mapped.x).isEqualTo(FOLDER_X);
    }

    @Test
    void nestingTwoTabFoldersDoesNotCompoundTheError() {
        Shell shell = echoingShell();
        CTabFolder outer = new CTabFolder(shell, SWT.NONE);
        outer.setBounds(FOLDER_X, FOLDER_Y, 600, 500);

        Composite outerPage = new Composite(outer, SWT.NONE);
        outerPage.setBounds(0, TAB_STRIP, 600, 500 - TAB_STRIP);

        CTabFolder inner = new CTabFolder(outerPage, SWT.NONE);
        inner.setBounds(0, 0, 600, 500 - TAB_STRIP);

        Composite innerPage = new Composite(inner, SWT.NONE);
        innerPage.setBounds(0, TAB_STRIP, 600, 500 - 2 * TAB_STRIP);

        Point mapped = innerPage.toDisplay(0, 0);

        assertThat(mapped.y)
                .as("a stack of folders must not add its strip once per level")
                .isEqualTo(FOLDER_Y + TAB_STRIP + TAB_STRIP);
    }

    @Test
    void aGroupStillGetsItsTitleHeightBack() {
        // The correction exists for this case and must survive: a Group draws its title above its
        // client area and its child's bounds do not include it.
        Shell shell = echoingShell();
        Group group = new Group(shell, SWT.NONE);
        group.setText("titled");
        group.setBounds(FOLDER_X, FOLDER_Y, 400, 300);

        Composite child = new Composite(group, SWT.NONE);
        child.setBounds(0, 0, 300, 200);

        Point mapped = child.toDisplay(0, 0);

        assertThat(mapped.y)
                .as("a Group's child sits below the title the Group draws, and nothing else "
                        + "reports that height")
                .isGreaterThan(FOLDER_Y);
    }
}
