package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * The client draws each child of the workbench's main sash area as a panel: inset by a gap on every
 * side and its content scaled into what is left. {@code toDisplay} has to land where that panel
 * paints, or a popup placed under a caret (content assist) covers the line it was opened for.
 */
class ControlToDisplayPanelInsetTest extends SerializeTestBase {

    private static final int GAP = 5;
    private static final int MAIN_Y = 40;
    private static final int MAIN_W = 1400;
    private static final int MAIN_H = 824;

    /** What the e4 SashLayout's class name looks like to the main-area detection. */
    private static final class SashLayout extends Layout {
        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            return new Point(0, 0);
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
        }

        @Override
        public String toString() {
            return "org.eclipse.e4.ui.workbench.renderers.swt.SashLayout@0";
        }
    }

    private static Shell echoingShell() {
        Shell shell = shell();
        when(shell.toDisplay(anyInt(), anyInt()))
                .thenAnswer(call -> new Point(call.getArgument(0), call.getArgument(1)));
        when(shell.toControl(anyInt(), anyInt()))
                .thenAnswer(call -> new Point(call.getArgument(0), call.getArgument(1)));
        return shell;
    }

    private static Composite mainArea(Composite parent, int y) {
        Composite main = new Composite(parent, SWT.NONE);
        main.setLayout(new SashLayout());
        main.setBounds(0, y, MAIN_W, MAIN_H);
        return main;
    }

    @Test
    void aPartStackMapsToWhereItsPanelIsPainted() {
        Composite main = mainArea(echoingShell(), MAIN_Y);
        CTabFolder folder = new CTabFolder(main, SWT.NONE);
        folder.setBounds(353, 0, 730, MAIN_H);

        Point mapped = folder.toDisplay(0, 51);

        assertThat(mapped.x).isEqualTo(353 + GAP);
        assertThat(mapped.y)
                .as("the panel starts a gap below the main area and its content is scaled into the "
                        + "height left after both gaps")
                .isEqualTo(Math.round(MAIN_Y + GAP + 51 * (MAIN_H - 2.0 * GAP) / MAIN_H));
    }

    @Test
    void aNestedMainAreaInsideAPanelAddsNoSecondGap() {
        Composite outer = mainArea(echoingShell(), MAIN_Y);
        new CTabFolder(outer, SWT.NONE).setBounds(0, 0, 0, 0);
        Composite inner = mainArea(outer, 0);
        CTabFolder folder = new CTabFolder(inner, SWT.NONE);
        folder.setBounds(1087, 0, 313, MAIN_H);

        Point mapped = folder.toDisplay(0, 0);

        assertThat(mapped.x).isEqualTo(Math.round(GAP + 1087 * (MAIN_W - 2.0 * GAP) / MAIN_W));
        assertThat(mapped.y).isEqualTo(MAIN_Y + GAP);
    }

    @Test
    void toControlIsTheInverseOfToDisplay() {
        Composite main = mainArea(echoingShell(), MAIN_Y);
        CTabFolder folder = new CTabFolder(main, SWT.NONE);
        folder.setBounds(353, 0, 730, MAIN_H);

        Point local = folder.toControl(folder.toDisplay(200, 400));

        assertThat(local).isEqualTo(new Point(200, 400));
    }

    @Test
    void aChildOfAPlainCompositeIsNotInset() {
        Composite plain = new Composite(echoingShell(), SWT.NONE);
        plain.setBounds(0, MAIN_Y, MAIN_W, MAIN_H);
        CTabFolder folder = new CTabFolder(plain, SWT.NONE);
        folder.setBounds(353, 0, 730, MAIN_H);

        assertThat(folder.toDisplay(0, 51)).isEqualTo(new Point(353, MAIN_Y + 51));
    }
}
