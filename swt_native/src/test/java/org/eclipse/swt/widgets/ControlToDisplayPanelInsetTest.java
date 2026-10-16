package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * The client draws each child of the workbench's main sash area as a panel: inset by a gap on every
 * side and its content scaled into what is left. {@code toDisplay} has to land where that panel
 * paints, or a popup placed under a caret (content assist) covers the line it was opened for.
 *
 * <p>The expected numbers are the positions the client was measured painting at for this layout.
 */
class ControlToDisplayPanelInsetTest extends SerializeTestBase {

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

    private static Composite mainArea(Composite parent, int y, int height) {
        Composite main = new Composite(parent, SWT.NONE);
        main.setLayout(new SashLayout());
        main.setBounds(0, y, MAIN_W, height);
        return main;
    }

    private static CTabFolder partStack(Composite main) {
        CTabFolder folder = new CTabFolder(main, SWT.NONE);
        folder.setBounds(353, 0, 730, MAIN_H);
        return folder;
    }

    @Test
    void aPartStackMapsToWhereItsPanelIsPainted() {
        CTabFolder folder = partStack(mainArea(shell(), MAIN_Y, MAIN_H));

        Point mapped = folder.toDisplay(0, 51);

        assertThat(mapped.x).isEqualTo(358);
        assertThat(mapped.y)
                .as("the panel starts a gap below the main area and its content is scaled into the "
                        + "height left after both gaps")
                .isEqualTo(95);
    }

    @Test
    void aNestedMainAreaInsideAPanelAddsNoSecondGap() {
        Composite outer = mainArea(shell(), MAIN_Y, MAIN_H);
        new CTabFolder(outer, SWT.NONE).setBounds(0, 0, 0, 0);
        Composite inner = mainArea(outer, 0, MAIN_H);
        CTabFolder folder = new CTabFolder(inner, SWT.NONE);
        folder.setBounds(1087, 0, 313, MAIN_H);

        assertThat(folder.toDisplay(0, 0)).isEqualTo(new Point(1084, 45));
    }

    @Test
    void aMainAreaBelowAPlainCompositeInsideAPanelPanelsAgain() {
        // A plain Composite clears the panel marker for what it holds, so a main area further down
        // insets its own children once more.
        Composite outer = mainArea(shell(), MAIN_Y, MAIN_H);
        CTabFolder outerStack = new CTabFolder(outer, SWT.NONE);
        outerStack.setBounds(0, 0, MAIN_W, MAIN_H);
        Composite page = new Composite(outerStack, SWT.NONE);
        page.setBounds(0, 32, MAIN_W, 792);
        Composite inner = mainArea(page, 0, 792);
        CTabFolder folder = new CTabFolder(inner, SWT.NONE);
        folder.setBounds(0, 0, MAIN_W, 792);

        assertThat(folder.toDisplay(0, 0)).isEqualTo(new Point(10, 82));
    }

    @Test
    void toControlIsTheInverseOfToDisplay() {
        CTabFolder folder = partStack(mainArea(shell(), MAIN_Y, MAIN_H));

        Point local = folder.toControl(folder.toDisplay(200, 400));

        assertThat(local).isEqualTo(new Point(200, 400));
    }

    @Test
    void displayMapAgreesWithToControl() {
        CTabFolder folder = partStack(mainArea(shell(), MAIN_Y, MAIN_H));

        assertThat(DisplayHelper.mapOrigin(null, folder, 358, 440))
                .as("Display.map(null, control, p) is documented as control.toControl(p)")
                .isEqualTo(folder.toControl(358, 440))
                .isEqualTo(new Point(0, 400));
    }

    @Test
    void aMappedRectangleTakesThePanelScale() {
        CTabFolder folder = partStack(mainArea(shell(), MAIN_Y, MAIN_H));

        assertThat(DisplayHelper.mapRectangle(folder, null, 0, 0, 730, MAIN_H))
                .isEqualTo(new Rectangle(358, 45, 720, 814));
    }

    @Test
    void aPostedPointInThePanelGapDoesNotHitThePart() {
        Shell shell = shell();
        Composite main = mainArea(shell, MAIN_Y, MAIN_H);
        CTabFolder folder = partStack(main);
        Display display = shell.getDisplay();
        when(display.getActiveShell()).thenReturn(shell);
        when(display.map(any(Control.class), isNull(), any(Rectangle.class))).thenAnswer(call -> {
            Rectangle r = call.getArgument(2);
            return DisplayHelper.mapRectangle(call.getArgument(0), null, r.x, r.y, r.width, r.height);
        });
        when(shell.isVisible()).thenReturn(true);
        when(shell.getBounds()).thenReturn(new Rectangle(0, 0, MAIN_W, 900));
        when(shell.getChildren()).thenReturn(new Control[] {main});

        assertThat(DisplayInput.controlAt(display, 356, 43))
                .as("the gap around a panel is painted by the main area, not by the part")
                .isSameAs(main);
        assertThat(DisplayInput.controlAt(display, 400, 100)).isSameAs(folder);
    }

    @Test
    void aChildOfAPlainCompositeIsNotInset() {
        Composite plain = new Composite(shell(), SWT.NONE);
        plain.setBounds(0, MAIN_Y, MAIN_W, MAIN_H);
        CTabFolder folder = new CTabFolder(plain, SWT.NONE);
        folder.setBounds(353, 0, 730, MAIN_H);

        assertThat(folder.toDisplay(0, 51)).isEqualTo(new Point(353, MAIN_Y + 51));
    }
}
